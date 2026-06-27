package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.services.GeminiClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Scheduler", appName)
  }

  @Test
  fun testGeminiApiCallRobolectric() = runBlocking {
    println("=== STARTING ROBOLECTRIC GEMINI API CALL TEST ===")
    val envKey = System.getenv("GEMINI_API_KEY") ?: System.getenv("gemini_api_key") ?: ""
    println("System env GEMINI_API_KEY: ${if (envKey.isNotEmpty()) "FOUND (LENGTH: " + envKey.length + ")" else "NOT FOUND"}")
    
    if (envKey.isEmpty() || envKey == "MY_GEMINI_API_KEY") {
        println("Skipping real network calls in Robolectric unit test because GEMINI_API_KEY is not configured.")
        return@runBlocking
    }
    
    // We will call the API using a customized client or we can modify the BASE_URL or model name
    // Let's print what endpoint and key we are using first.
    println("calling with key: $envKey")
    
    // Let's try calling with different models. First, we will call with the current BASE_URL from GeminiClient.
    // Wait, let's write a small helper to make a direct API call to any model we choose.
    val responseMock = try {
        // Let's call the endpoints directly to see which one works!
        val client = okhttp3.OkHttpClient()
        
        fun testModel(modelName: String): String {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$envKey"
            val textPrompt = "Hello, reply with only the word SUCCESS if this works."
            val bodyStr = "{\"contents\":[{\"parts\":[{\"text\":\"$textPrompt\"}]}]}"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(bodyStr.toRequestBody(mediaType))
                .build()
            client.newCall(request).execute().use { response ->
                val code = response.code
                val body = response.body?.string() ?: ""
                return "$modelName -> Http Code: $code, Body: $body"
            }
        }
        
        val res35 = testModel("gemini-3.5-flash")
        val res15Custom = testModel("gemini-1.5-flash")
        
        "Model 3.5 Results:\n$res35\n\nModel 1.5 Results:\n$res15Custom"
    } catch (e: Exception) {
        "Exception: " + e.localizedMessage
    }
    
    println("=== API TESTS RESULTS ===")
    println(responseMock)
    println("=======================")
  }
}

