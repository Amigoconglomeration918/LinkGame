// LeaderboardRepository.kt
package com.example.linkgame.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.linkgame.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "leaderboard")
private val entriesKey = stringPreferencesKey("leaderboard_entries")
private val json = Json { ignoreUnknownKeys = true }

object LeaderboardRepository {
    fun getAllEntries(context: Context): Flow<List<LeaderboardEntry>> {
        return context.dataStore.data.map { preferences ->
            val jsonStr = preferences[entriesKey] ?: "[]"
            json.decodeFromString(jsonStr)
        }
    }

    suspend fun addEntry(context: Context, entry: LeaderboardEntry) {
        context.dataStore.edit { preferences ->
            val current = preferences[entriesKey]?.let {
                json.decodeFromString<List<LeaderboardEntry>>(it)
            } ?: emptyList()
            val newList = (current + entry).sortedWith(
                compareByDescending<LeaderboardEntry> { it.score }
                    .thenBy { it.timeSeconds }
            )
            preferences[entriesKey] = json.encodeToString(newList)
        }
    }

    suspend fun deleteEntry(context: Context, id: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[entriesKey]?.let {
                json.decodeFromString<List<LeaderboardEntry>>(it)
            } ?: emptyList()
            val newList = current.filter { it.id != id }
            preferences[entriesKey] = json.encodeToString(newList)
        }
    }

    // 新增：清空所有排行榜记录
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[entriesKey] = "[]"
        }
    }
}