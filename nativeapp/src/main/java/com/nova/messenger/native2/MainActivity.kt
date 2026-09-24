package com.nova.messenger.native2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.nova.messenger.native2.ui.NovaApp
import com.nova.messenger.native2.ui.NovaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NovaNotificationHelper.ensureChannel(this)
        requestNotificationPermission()

        setContent {
            NovaTheme {
                NovaApp(viewModel)
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val conversationId = intent
            ?.getLongExtra(EXTRA_CONVERSATION_ID, 0L)
            ?: 0L

        if (conversationId > 0) {
            viewModel.openConversationById(conversationId)
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }
    }

    override fun onStart() {
        super.onStart()
        isForeground = true
    }

    override fun onStop() {
        isForeground = false
        super.onStop()
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "nova_conversation_id"
        private const val REQUEST_NOTIFICATIONS = 3101

        @Volatile
        var isForeground: Boolean = false
    }
}
