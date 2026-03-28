package com.example.chemlab.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.chemlab.data.api.dto.UserDTO
import com.google.gson.Gson

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("chemlab_auth", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun saveUser(user: UserDTO) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().apply {
            putString(KEY_USER, userJson)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getUser(): UserDTO? {
        val userJson = sharedPreferences.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UserDTO::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, null)
            putString(KEY_REFRESH_TOKEN, null)
            putString(KEY_USER, null)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
