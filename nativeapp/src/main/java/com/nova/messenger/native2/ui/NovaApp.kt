package com.nova.messenger.native2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nova.messenger.native2.NovaViewModel
import com.nova.messenger.native2.RootTab

@Composable
fun NovaApp(viewModel: NovaViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaPalette.Bg)
            .systemBarsPadding()
    ) {
        when {
            state.booting -> CircularProgressIndicator(
                color = NovaPalette.Accent2,
                modifier = Modifier.align(Alignment.Center)
            )

            state.user == null -> AuthScreen(
                busy = state.busy,
                onLogin = viewModel::login,
                onRegister = viewModel::register
            )

            state.selectedConversation != null -> ChatScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onBack = viewModel::closeConversation,
                onSend = { text -> viewModel.sendMessage(text) },
                onAttachment = viewModel::uploadAndSend,
                onEditMessage = viewModel::editMessage,
                onDeleteMessage = viewModel::deleteMessage,
                onReactMessage = viewModel::reactMessage,
                onDraftChanged = viewModel::draftChanged,
                onNavigateRoot = viewModel::navigateRoot,
                wallpaperId = viewModel::wallpaperId,
                wallpaperDim = viewModel::wallpaperDim,
                wallpaperAtmosphere = viewModel::wallpaperAtmosphere,
                onSaveLocalWallpaper = viewModel::saveLocalWallpaper,
                onProposeWallpaper = viewModel::proposeWallpaper,
                onResetWallpaper = viewModel::resetWallpaper,
                onRespondWallpaper = viewModel::respondWallpaper,
                onRemoveFriend = viewModel::removeFriend
            )

            state.tab == RootTab.CHATS -> ChatsScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onOpen = viewModel::selectConversation,
                onTab = viewModel::setTab
            )

            state.tab == RootTab.CONTACTS -> ContactsScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onSearch = viewModel::updateContactSearch,
                onRequest = viewModel::requestFriend,
                onRespond = viewModel::respondFriend,
                onOpenFriend = viewModel::openFriend,
                onTab = viewModel::setTab
            )

            state.tab == RootTab.PROFILE -> ProfileScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onSave = viewModel::updateProfile,
                onLogout = viewModel::logout,
                onTab = viewModel::setTab
            )

            else -> SettingsScreen(
                state = state,
                mediaUrl = viewModel::mediaUrl,
                onCompactChanged = viewModel::setCompact,
                onBubbleSizeChanged = viewModel::setBubbleSize,
                onBubbleThemeChanged = viewModel::setBubbleTheme,
                onTab = viewModel::setTab
            )
        }

        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
