package com.fashionsaas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fashionsaas.app.ui.navigation.FashionNavGraph
import com.fashionsaas.app.ui.theme.FashionSaaSTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de Fashion SaaS.
 * Punto de entrada de la aplicación, inicializa el tema y la navegación.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FashionSaaSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FashionNavGraph()
                }
            }
        }
    }
}