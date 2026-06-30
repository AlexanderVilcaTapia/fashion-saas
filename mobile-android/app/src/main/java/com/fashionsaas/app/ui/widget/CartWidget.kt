package com.fashionsaas.app.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fashionsaas.app.MainActivity
import com.fashionsaas.app.data.local.FashionDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/**
 * Punto de entrada Hilt para acceder al CartDao desde el widget,
 * ya que los widgets no soportan inyección de dependencias directa con @AndroidEntryPoint.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CartWidgetEntryPoint {
    fun database(): FashionDatabase
}

/**
 * Widget de resumen del carrito para la pantalla de inicio del dispositivo.
 * Muestra la cantidad de items y el total acumulado en el carrito local.
 * Al tocar el widget, abre la aplicación directamente.
 */
class CartWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CartWidgetEntryPoint::class.java
        )
        val cartDao = entryPoint.database().cartDao()
        val items = cartDao.getAllCartItems().first()
        val itemCount = items.sumOf { it.quantity }
        val total = items.sumOf { it.productPrice * it.quantity }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF4F46E5))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text(
                    text = "Fashion SaaS",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = "🛒 $itemCount items",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "S/. ${String.format("%.2f", total)}",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

/**
 * Receiver del sistema Android para el widget del carrito.
 * Requerido para registrar el widget en el manifest.
 */
class CartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CartWidget()
}