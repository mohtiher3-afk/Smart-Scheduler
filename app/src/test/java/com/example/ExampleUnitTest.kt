package com.example

import com.example.services.GeminiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGeminiApiCall() = runBlocking {
        println("=== STARTING GEMINI API CALL TEST ===")
        val key = com.example.BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            println("Skipping real API call since API key is empty/placeholder. Verifying error handling.")
            val response = GeminiClient.generateContent("Test")
            assertNotNull(response)
            assertTrue(response!!.contains("Error: API key is not configured"))
        } else {
            val response = GeminiClient.generateContent("Hi, respond with the word 'SUCCESS' if you read this.")
            println("=== GEMINI RESPONSE ===")
            println(response)
            println("=======================")
            assertNotNull(response)
        }
    }
}


