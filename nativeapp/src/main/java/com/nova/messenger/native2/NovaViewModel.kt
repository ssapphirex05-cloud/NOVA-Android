package com.nova.messenger.native2

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NovaUiState(
    val booting: Boolean = true,
    val busy: Boolean = false,
    val user: NovaUser? = null,
    val conversations: List<Conversation> = emptyList(),
    val selectedConversation: Conversation? = null,
    val messages: List<NovaMessage> = emptyList(),
    val typingUsers: List<TypingUser> = emptyList(),
    val friends: FriendsResponse = FriendsResponse(),
    val contactSearch: String = "",
    val searchUsers: List<NovaUser> = emptyList(),
    val tab: RootTab = RootTab.CHATS,
    val error: String? = null
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NovaRepository(application)
    private val mutable = MutableStateFlow(NovaUiState())
    val state: StateFlow<NovaUiState> = mutable.asStateFlow()

    private var pollingJob: Job? = null
    private var contactSearchJob: Job? = null
    private var typingOffJob: Job? = null
    private var lastTypingPing = 0L
    private var lastMarkedReadId = 0L
    private var pendingConversationId: Long? = null

    init {
        boot()
    }

    private fun boot() {
        viewModelScope.launch {
            if (repository.session.token.isNullOrBlank()) {
                mutable.value = NovaUiState(booting = false)
                return@launch
            }

            runCatching { repository.me() }
                .onSuccess { user ->
                    mutable.value = mutable.value.copy(booting = false, user = user)
                    refreshConversations()
                    refreshFriends()
                    startPolling()
                    syncPush()
                }
                .onFailure {
                    repository.clearSession()
                    mutable.value = NovaUiState(booting = false)
                }
        }
    }

    fun login(username: String, password: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.login(username, password) }
                .onSuccess { result ->
                    mutable.value = mutable.value.copy(busy = false, user = result.user)
                    refreshConversations()
                    refreshFriends()
                    startPolling()
                    syncPush()
                }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
                }
        }
    }

    fun register(username: String, displayName: String, password: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.register(username, displayName, password) }
                .onSuccess { result ->
                    mutable.value = mutable.value.copy(busy = false, user = result.user)
                    refreshConversations()
                    refreshFriends()
                    startPolling()
                    syncPush()
                }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { repository.unregisterPush() }
            pollingJob?.cancel()
            repository.clearSession()
            mutable.value = NovaUiState(booting = false)
        }
    }

    fun dismissError() {
        mutable.value = mutable.value.copy(error = null)
    }

    fun setTab(tab: RootTab) {
        mutable.value = mutable.value.copy(tab = tab)
        if (tab == RootTab.CONTACTS) refreshFriends()
    }

    fun navigateRoot(tab: RootTab) {
        val conversationId = mutable.value.selectedConversation?.id
        if (conversationId != null) {
            viewModelScope.launch { runCatching { repository.setTyping(conversationId, false) } }
        }
        mutable.value = mutable.value.copy(
            tab = tab,
            selectedConversation = null,
            messages = emptyList(),
            typingUsers = emptyList()
        )
        if (tab == RootTab.CONTACTS) refreshFriends()
    }

    fun selectConversation(conversation: Conversation) {
        lastMarkedReadId = 0L
        mutable.value = mutable.value.copy(
            tab = RootTab.CHATS,
            selectedConversation = conversation,
            messages = emptyList(),
            typingUsers = emptyList()
        )
        viewModelScope.launch { refreshMessages() }
    }

    fun openConversationById(id: Long) {
        if (id <= 0) return
        pendingConversationId = id
        val conversation = mutable.value.conversations.firstOrNull { it.id == id }
        if (conversation != null) {
            pendingConversationId = null
            selectConversation(conversation)
        } else {
            viewModelScope.launch {
                refreshConversationsNow()
                mutable.value.conversations.firstOrNull { it.id == id }?.let {
                    pendingConversationId = null
                    selectConversation(it)
                }
            }
        }
    }

    fun closeConversation() {
        navigateRoot(RootTab.CHATS)
    }

    fun sendMessage(text: String, replyToId: Long? = null) {
        val conversation = mutable.value.selectedConversation ?: return
        val clean = text.trim()
        if (clean.isEmpty() || mutable.value.busy) return

        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.sendMessage(conversation.id, clean, null, replyToId) }
                .onSuccess {
                    runCatching { repository.setTyping(conversation.id, false) }
                    mutable.value = mutable.value.copy(busy = false)
                    refreshMessages()
                    refreshConversations()
                }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
                }
        }
    }

    fun uploadAndSend(uri: Uri) {
        val conversation = mutable.value.selectedConversation ?: return
        if (mutable.value.busy) return

        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching {
                val attachment = repository.upload(uri)
                repository.sendMessage(conversation.id, "", attachment, null)
            }.onSuccess {
                mutable.value = mutable.value.copy(busy = false)
                refreshMessages()
                refreshConversations()
            }.onFailure { error ->
                mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
            }
        }
    }

    fun editMessage(message: NovaMessage, newText: String) {
        val clean = newText.trim()
        if (clean.isEmpty() && message.attachment == null) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.editMessage(message.id, clean) }
                .onSuccess { updated ->
                    replaceMessage(updated)
                    mutable.value = mutable.value.copy(busy = false)
                    refreshConversations()
                }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
                }
        }
    }

    fun deleteMessage(message: NovaMessage) {
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.deleteMessage(message.id) }
                .onSuccess { updated ->
                    replaceMessage(updated)
                    mutable.value = mutable.value.copy(busy = false)
                    refreshConversations()
                }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(error))
                }
        }
    }

    fun reactMessage(message: NovaMessage, emoji: String) {
        viewModelScope.launch {
            runCatching { repository.reactMessage(message.id, emoji) }
                .onSuccess { replaceMessage(it) }
                .onFailure { error ->
                    mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(error))
                }
        }
    }

    private fun replaceMessage(updated: NovaMessage) {
        mutable.value = mutable.value.copy(
            messages = mutable.value.messages.map {
                if (it.id == updated.id) updated else it
            }
        )
    }

    fun draftChanged(hasText: Boolean) {
        val conversationId = mutable.value.selectedConversation?.id ?: return
        typingOffJob?.cancel()

        val now = SystemClock.elapsedRealtime()
        if (hasText && now - lastTypingPing >= 1400L) {
            lastTypingPing = now
            viewModelScope.launch {
                runCatching { repository.setTyping(conversationId, true, "typing") }
            }
        }

        typingOffJob = viewModelScope.launch {
            delay(2600)
            runCatching { repository.setTyping(conversationId, false, "typing") }
        }
    }

    fun updateContactSearch(query: String) {
        mutable.value = mutable.value.copy(contactSearch = query)
        contactSearchJob?.cancel()

        if (query.trim().length < 2) {
            mutable.value = mutable.value.copy(searchUsers = emptyList())
            return
        }

        contactSearchJob = viewModelScope.launch {
            delay(350)
            runCatching { repository.searchUsers(query.trim()) }
                .onSuccess { mutable.value = mutable.value.copy(searchUsers = it) }
                .onFailure { mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(it)) }
        }
    }

    fun requestFriend(user: NovaUser) {
        viewModelScope.launch {
            runCatching { repository.requestFriend(user.id) }
                .onSuccess { result ->
                    refreshFriends()
                    updateContactSearch(mutable.value.contactSearch)
                    result.conversationId?.let { openConversationById(it) }
                }
                .onFailure { mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(it)) }
        }
    }

    fun respondFriend(request: FriendRequestItem, action: String) {
        viewModelScope.launch {
            runCatching { repository.respondFriend(request.id, action) }
                .onSuccess { result ->
                    refreshFriends()
                    refreshConversations()
                    if (action == "accept") result.conversationId?.let { openConversationById(it) }
                }
                .onFailure { mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(it)) }
        }
    }

    fun removeFriend(user: NovaUser) {
        viewModelScope.launch {
            runCatching { repository.removeFriend(user.id) }
                .onSuccess {
                    refreshFriends()
                    refreshConversations()
                }
                .onFailure { mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(it)) }
        }
    }

    fun openFriend(user: NovaUser, conversationId: Long) {
        if (conversationId > 0) {
            openConversationById(conversationId)
            return
        }

        viewModelScope.launch {
            runCatching { repository.openDm(user.id) }
                .onSuccess { id ->
                    refreshConversationsNow()
                    openConversationById(id)
                }
                .onFailure { mutable.value = mutable.value.copy(error = NovaRepository.errorMessage(it)) }
        }
    }

    fun updateProfile(displayName: String, bio: String, statusText: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            runCatching { repository.updateProfile(displayName, bio, statusText) }
                .onSuccess { user ->
                    mutable.value = mutable.value.copy(busy = false, user = user)
                    refreshConversations()
                    refreshFriends()
                }
                .onFailure { mutable.value = mutable.value.copy(busy = false, error = NovaRepository.errorMessage(it)) }
        }
    }

    private fun refreshConversations() {
        viewModelScope.launch { refreshConversationsNow() }
    }

    private suspend fun refreshConversationsNow() {
        runCatching { repository.conversations() }
            .onSuccess { list ->
                val currentId = mutable.value.selectedConversation?.id
                val selected = currentId?.let { id -> list.firstOrNull { it.id == id } }
                    ?: mutable.value.selectedConversation

                mutable.value = mutable.value.copy(
                    conversations = list,
                    selectedConversation = selected
                )

                pendingConversationId?.let { id ->
                    list.firstOrNull { it.id == id }?.let {
                        pendingConversationId = null
                        selectConversation(it)
                    }
                }
            }
    }

    private suspend fun refreshMessages() {
        val conversation = mutable.value.selectedConversation ?: return

        runCatching { repository.messages(conversation.id) }
            .onSuccess { response ->
                if (mutable.value.selectedConversation?.id != conversation.id) return@onSuccess
                mutable.value = mutable.value.copy(
                    messages = response.messages,
                    typingUsers = response.typingUsers
                )

                val lastId = response.messages.lastOrNull()?.id ?: 0L
                if (lastId > lastMarkedReadId) {
                    lastMarkedReadId = lastId
                    runCatching { repository.markRead(conversation.id, lastId) }
                }
            }
    }

    private fun refreshFriends() {
        viewModelScope.launch {
            runCatching { repository.friends() }
                .onSuccess { mutable.value = mutable.value.copy(friends = it) }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var tick = 0
            while (isActive && !repository.session.token.isNullOrBlank()) {
                delay(1800)

                runCatching { repository.conversations() }
                    .onSuccess { list ->
                        val currentId = mutable.value.selectedConversation?.id
                        val selected = currentId?.let { id -> list.firstOrNull { it.id == id } }
                            ?: mutable.value.selectedConversation
                        mutable.value = mutable.value.copy(conversations = list, selectedConversation = selected)
                    }

                if (mutable.value.selectedConversation != null) refreshMessages()

                tick++
                if (tick % 8 == 0) runCatching { repository.heartbeat() }
                if (tick % 6 == 0 && mutable.value.tab == RootTab.CONTACTS) {
                    runCatching { repository.friends() }
                        .onSuccess { mutable.value = mutable.value.copy(friends = it) }
                }
            }
        }
    }

    private fun syncPush() {
        if (!BuildConfig.FIREBASE_CONFIGURED) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { registerPushToken(it) }
    }

    fun registerPushToken(token: String) {
        if (token.isBlank() || repository.session.token.isNullOrBlank()) return
        viewModelScope.launch { runCatching { repository.registerPush(token) } }
    }

    fun mediaUrl(path: String?): String? = repository.absoluteMediaUrl(path)
}
