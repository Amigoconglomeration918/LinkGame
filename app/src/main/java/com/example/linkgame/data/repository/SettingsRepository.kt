package com.example.linkgame.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsRepository {
    private val BGM_ENABLED_KEY = booleanPreferencesKey("bgm_enabled")
    private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    private val BGM_TYPE_KEY = stringPreferencesKey("bgm_type")   // 新增

    fun isBgmEnabled(context: Context): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[BGM_ENABLED_KEY] ?: true
        }
    }

    fun isSoundEnabled(context: Context): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: true
        }
    }

    suspend fun setBgmEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[BGM_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    // 新增：获取背景音乐类型
    fun getBgmType(context: Context): Flow<String> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[BGM_TYPE_KEY] ?: "classic"   // 默认经典
        }
    }

    // 新增：保存背景音乐类型
    suspend fun setBgmType(context: Context, type: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[BGM_TYPE_KEY] = type
        }
    }
}