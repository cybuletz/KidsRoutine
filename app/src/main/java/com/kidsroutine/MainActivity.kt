package com.kidsroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidsroutine.core.common.designsystem.theme.KidsRoutineTheme
import com.kidsroutine.navigation.KidsRoutineNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidsRoutineTheme {
                KidsRoutineNavGraph()
            }
        }
    }
}
