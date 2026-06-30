package com.fashionsaas.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.fashionsaas.app.BuildConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que gestiona la autenticación con Google mediante Firebase Auth
 * y Credential Manager, el método moderno recomendado por Google para Android.
 */
@Singleton
class GoogleAuthRepository @Inject constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Inicia el flujo de autenticación con Google usando Credential Manager.
     * Obtiene el ID token de Google y lo intercambia por credenciales de Firebase.
     *
     * @param context contexto de la actividad que invoca el flujo
     * @return Result con el ID token de Firebase si la autenticación es exitosa
     */
    suspend fun signInWithGoogle(context: Context): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val firebaseIdToken = authResult.user?.getIdToken(false)?.await()?.token

            if (firebaseIdToken != null) {
                Result.success(firebaseIdToken)
            } else {
                Result.failure(Exception("No se pudo obtener el token de Firebase."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar sesión con Google: ${e.message}"))
        }
    }
}