package com.peimama.renzi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.peimama.renzi.app.PeiMamaApp
import com.peimama.renzi.ui.theme.PeiMamaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as MamaReadApplication).appContainer

        setContent {
            PeiMamaTheme {
                PeiMamaApp(container = container)
            }
        }
    }
}
