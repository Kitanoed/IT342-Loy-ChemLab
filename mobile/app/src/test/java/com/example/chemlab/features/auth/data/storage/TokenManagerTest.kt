package com.example.chemlab.features.auth.data.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.chemlab.features.auth.data.dto.UserDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TokenManagerTest {
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("chemlab_auth", Context.MODE_PRIVATE).edit().clear().commit()
        tokenManager = TokenManager(context)
    }

    @Test
    fun saveTokensAndUserPersistsAuthState() {
        tokenManager.saveTokens("access-token", "refresh-token")
        tokenManager.saveUser(
            UserDTO(
                id = 7,
                email = "student@example.com",
                username = "student1",
                firstName = "Stu",
                lastName = "Dent",
                createdAt = "2026-05-09T00:00:00"
            )
        )

        assertTrue(tokenManager.isLoggedIn())
        assertEquals("access-token", tokenManager.getAccessToken())
        assertEquals("refresh-token", tokenManager.getRefreshToken())
        assertEquals("student@example.com", tokenManager.getUser()?.email)
    }

    @Test
    fun logoutClearsStoredState() {
        tokenManager.saveTokens("access-token", "refresh-token")
        tokenManager.logout()

        assertFalse(tokenManager.isLoggedIn())
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertNull(tokenManager.getUser())
    }
}