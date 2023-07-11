package bangkit.robbyyehezkiel.androidintermediate.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constanta.preferenceName)

class UserPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val token = stringPreferencesKey(Constanta.AuthPreferences.UserToken.name)
    private val uid = stringPreferencesKey(Constanta.AuthPreferences.UserUID.name)
    private val name = stringPreferencesKey(Constanta.AuthPreferences.UserName.name)
    private val email = stringPreferencesKey(Constanta.AuthPreferences.UserEmail.name)
    private val lastLogin = stringPreferencesKey(Constanta.AuthPreferences.UserLastLogin.name)

    fun getToken(): Flow<String> = dataStore.data.map { it[token] ?: Constanta.preferenceDefaultValue }

    fun getId(): Flow<String> = dataStore.data.map { it[uid] ?: Constanta.preferenceDefaultValue }

    fun getName(): Flow<String> = dataStore.data.map { it[name] ?: Constanta.preferenceDefaultValue }

    fun getEmail(): Flow<String> = dataStore.data.map { it[email] ?: Constanta.preferenceDefaultValue }


    suspend fun saveSession(userToken: String, userUid: String, userName:String, userEmail: String) {
        dataStore.edit { preferences ->
            preferences[token] = userToken
            preferences[uid] = userUid
            preferences[name] = userName
            preferences[email] = userEmail
            preferences[lastLogin] = Helper.currentDateToString()
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null
        fun getPreferenceInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}