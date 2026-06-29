package com.fashionsaas.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.fashionsaas.app.BuildConfig
import com.fashionsaas.app.data.local.FashionDatabase
import com.fashionsaas.app.data.local.dao.CartDao
import com.fashionsaas.app.data.local.dao.ProductDao
import com.fashionsaas.app.data.remote.api.ApiService
import com.fashionsaas.app.data.remote.api.AuthInterceptor
import com.fashionsaas.app.data.remote.api.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Extensión para crear el DataStore de preferencias. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fashion_prefs")

/**
 * Módulo principal de inyección de dependencias con Hilt.
 * Provee instancias singleton de Room, Retrofit, DataStore y DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provee la instancia singleton del DataStore de preferencias.
     *
     * @param context contexto de la aplicación
     * @return instancia del DataStore
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provee la instancia singleton de la base de datos Room.
     *
     * @param context contexto de la aplicación
     * @return instancia de FashionDatabase
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FashionDatabase {
        return Room.databaseBuilder(
            context,
            FashionDatabase::class.java,
            FashionDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provee el DAO de productos desde la base de datos.
     *
     * @param database instancia de FashionDatabase
     * @return instancia de ProductDao
     */
    @Provides
    @Singleton
    fun provideProductDao(database: FashionDatabase): ProductDao {
        return database.productDao()
    }

    /**
     * Provee el DAO del carrito desde la base de datos.
     *
     * @param database instancia de FashionDatabase
     * @return instancia de CartDao
     */
    @Provides
    @Singleton
    fun provideCartDao(database: FashionDatabase): CartDao {
        return database.cartDao()
    }

    /**
     * Provee el TokenAuthenticator para renovación automática de JWT.
     *
     * @param dataStore DataStore para leer y guardar tokens
     * @return instancia de TokenAuthenticator
     */
    @Provides
    @Singleton
    fun provideTokenAuthenticator(dataStore: DataStore<Preferences>): TokenAuthenticator {
        return TokenAuthenticator(dataStore)
    }

    /**
     * Provee el cliente OkHttp con interceptores de autenticación,
     * renovación automática de token y logging.
     *
     * @param dataStore          DataStore para obtener el token JWT
     * @param tokenAuthenticator authenticator para renovar tokens expirados
     * @return instancia de OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        dataStore: DataStore<Preferences>,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val tokenKey = stringPreferencesKey("access_token")

        return OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(AuthInterceptor {
                var token: String? = null
                val job = GlobalScope.launch {
                    dataStore.data.collect { prefs ->
                        token = prefs[tokenKey]
                    }
                }
                Thread.sleep(100)
                job.cancel()
                token
            })
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provee la instancia singleton de Retrofit configurada con la URL base de Django.
     *
     * @param okHttpClient cliente OkHttp configurado
     * @return instancia de Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${BuildConfig.DJANGO_BASE_URL}/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provee la implementación de ApiService generada por Retrofit.
     *
     * @param retrofit instancia de Retrofit
     * @return implementación de ApiService
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}