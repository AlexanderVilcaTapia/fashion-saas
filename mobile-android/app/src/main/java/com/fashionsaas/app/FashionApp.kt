package com.fashionsaas.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application principal de Fashion SaaS.
 * Inicializa Hilt para la inyección de dependencias en toda la app.
 */
@HiltAndroidApp
class FashionApp : Application()