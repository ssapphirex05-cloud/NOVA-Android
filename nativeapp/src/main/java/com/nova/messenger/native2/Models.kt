package com.nova.messenger.native2

enum class RootTab { CHATS, CONTACTS, PROFILE, SETTINGS }

data class NovaUser(
    val id: Long = 0,
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val statusText: String = "",
    val accent: String = "violet",
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val createdAt: String? = null,
    val lastSeen: String? = null,
    val birthDate: String = "",
    val online: Boolean = false,
    val system: Boolean = false,
    val systemRole: String = "",
    val relation: String? = null,
    val requestId: Long = 0,
    val conversationId: Long = 0
)

data class AuthResponse(val token: String = "", val user: NovaUser = NovaUser())
data class MeResponse(val user: NovaUser = NovaUser())

data class ProfileUpdateBody(
    val displayName: String,
    val bio: String,
    val statusText: String
)
data class ProfileUpdateResponse(
    val user: NovaUser = NovaUser(),
    val token: String = ""
)

data class LastMessage(
    val id: Long = 0,
    val body: String = "",
    val senderId: Long = 0,
    val senderName: String = "",
    val createdAt: String = ""
)

data class Conversation(
    val id: Long = 0,
    val type: String = "dm",
    val title: String = "",
    val peer: NovaUser? = null,
    val system: Boolean = false,
    val pinned: Boolean = false,
    val lastMessage: LastMessage? = null,
    val unread: Int = 0,
    val peerReadMessageId: Long = 0
)

data class ConversationsResponse(val conversations: List<Conversation> = emptyList())

data class MessageSender(
    val id: Long = 0,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null
)

data class MessageReply(
    val id: Long = 0,
    val body: String = "",
    val senderName: String = ""
)

data class MessageAttachment(
    val url: String = "",
    val name: String = "",
    val type: String = "",
    val size: Long = 0,
    val previewUrl: String? = null,
    val voice: Boolean = false,
    val durationMs: Long = 0,
    val waveform: List<Int> = emptyList()
)

data class ReactionGroup(
    val emoji: String = "",
    val count: Int = 0,
    val userIds: List<Long> = emptyList()
)

data class TypingUser(
    val id: Long = 0,
    val displayName: String = "",
    val mode: String = "typing"
)

data class NovaMessage(
    val id: Long = 0,
    val conversationId: Long = 0,
    val sender: MessageSender = MessageSender(),
    val body: String = "",
    val attachment: MessageAttachment? = null,
    val replyTo: MessageReply? = null,
    val createdAt: String = "",
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val reactions: List<ReactionGroup> = emptyList(),
    val readByPeer: Boolean = false
)

data class MessagesResponse(
    val messages: List<NovaMessage> = emptyList(),
    val hasMore: Boolean = false,
    val typingUsers: List<TypingUser> = emptyList(),
    val peerReadMessageId: Long = 0
)

data class MessageEnvelope(val message: NovaMessage = NovaMessage())
data class OkResponse(val ok: Boolean = true)
data class PushResponse(val ok: Boolean = true)

data class LoginBody(val username: String, val password: String)
data class RegisterBody(val username: String, val displayName: String, val password: String)
data class SendMessageBody(
    val body: String,
    val attachment: MessageAttachment? = null,
    val replyToId: Long? = null,
    val clientMessageId: String
)
data class ReadBody(val messageId: Long)
data class TypingBody(val active: Boolean, val mode: String = "typing")
data class PushBody(val token: String, val deviceId: String, val appVersion: String)
data class FriendRequestBody(val userId: Long)
data class FriendRespondBody(val requestId: Long, val action: String)
data class FriendRemoveBody(val userId: Long)
data class DmBody(val userId: Long)

data class FriendItem(
    val user: NovaUser = NovaUser(),
    val conversationId: Long = 0,
    val createdAt: String? = null
)

data class FriendRequestItem(
    val id: Long = 0,
    val user: NovaUser = NovaUser(),
    val createdAt: String? = null
)

data class FriendsResponse(
    val friends: List<FriendItem> = emptyList(),
    val incoming: List<FriendRequestItem> = emptyList(),
    val outgoing: List<FriendRequestItem> = emptyList(),
    val incomingCount: Int = 0
)

data class SearchUsersResponse(val users: List<NovaUser> = emptyList())
data class FriendRequestResult(val status: String = "", val conversationId: Long? = null)
data class FriendRespondResult(val ok: Boolean = true, val conversationId: Long? = null)
data class FriendRemoveResult(val status: String = "", val purged: Boolean = false)
data class DmResponse(val conversationId: Long = 0)
