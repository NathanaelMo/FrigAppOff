package com.monnier.frigapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Extension sur Context pour accéder au DataStore (singleton garanti par la lib). */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * Repository de persistance locale des données d'authentification.
 *
 * Stocke de façon sécurisée (DataStore Preferences) :
 * - Le JWT token
 * - L'ID, prénom et email de l'utilisateur connecté
 *
 * Ces données survivent aux redémarrages de l'app (auto-login).
 */
class TokenRepository(private val context: Context) {

    // ─── Clés DataStore ──────────────────────────────────────────────────────

    companion object {
        private val KEY_TOKEN      = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID    = stringPreferencesKey("user_id")
        private val KEY_USER_NAME  = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    // ─── Lecture en flux réactif ──────────────────────────────────────────────

    /** Flow du token — s'émet à chaque changement. Utile pour observer l'état de connexion. */
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    /** Flow du prénom de l'utilisateur connecté. */
    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    // ─── Lecture ponctuelle (suspend) ────────────────────────────────────────

    /** Retourne le token JWT stocké, ou null si absent. */
    suspend fun getToken(): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.first()

    /** Retourne l'ID utilisateur stocké, ou null si absent. */
    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    /** Retourne le prénom de l'utilisateur, ou null si absent. */
    suspend fun getUserName(): String? =
        context.dataStore.data.map { it[KEY_USER_NAME] }.first()

    /** Retourne l'email de l'utilisateur, ou null si absent. */
    suspend fun getUserEmail(): String? =
        context.dataStore.data.map { it[KEY_USER_EMAIL] }.first()

    // ─── Écriture ────────────────────────────────────────────────────────────

    /**
     * Sauvegarde le token et les infos utilisateur après un login réussi.
     *
     * @param token     JWT retourné par l'API
     * @param userId    UUID de l'utilisateur
     * @param userName  Prénom de l'utilisateur
     * @param userEmail Email de l'utilisateur
     */
    suspend fun saveAuthData(
        token: String,
        userId: String,
        userName: String,
        userEmail: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]      = token
            prefs[KEY_USER_ID]    = userId
            prefs[KEY_USER_NAME]  = userName
            prefs[KEY_USER_EMAIL] = userEmail
        }
    }

    /**
     * Supprime toutes les données d'auth (appelé au logout).
     */
    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
        }
    }
}
