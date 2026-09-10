package com.babatiffin.bts

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.babatiffin.bts.core.design.BtsTheme
import com.babatiffin.bts.navigation.BtsNavGraph
import com.babatiffin.bts.data.backend.SupabaseProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.babatiffin.bts.core.payment.RazorpayCoordinator
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SupabaseProvider.client?.handleDeeplinks(intent)
        Checkout.preload(applicationContext)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDark) }

            BtsTheme(darkTheme = darkTheme) {
                BtsNavGraph(
                    isDarkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseProvider.client?.handleDeeplinks(intent)
    }

    override fun onPaymentSuccess(paymentId: String?, data: PaymentData?) = RazorpayCoordinator.success(data)
    override fun onPaymentError(code: Int, message: String?, data: PaymentData?) = RazorpayCoordinator.failure(message)
}
