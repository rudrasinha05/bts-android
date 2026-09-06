package in.babatiffin.bts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import in.babatiffin.bts.core.design.BtsTheme
import in.babatiffin.bts.navigation.BtsNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BtsTheme {
                BtsNavGraph()
            }
        }
    }
}
