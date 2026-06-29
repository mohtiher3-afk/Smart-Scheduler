package com.example.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

object GeminiClient {
    private const val TAG = "GeminiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        responseJsonSchema: String? = null
    ): String? {
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview", "gemini-3.1-pro-preview")
        var lastError = ""
        
        for (model in modelsToTry) {
            Log.d(TAG, "Trying model: $model")
            val result = tryModel(model, prompt, systemInstruction, responseJsonSchema)
            if (result != null) {
                if (result.startsWith("Error:")) {
                    Log.w(TAG, "Model $model failed with error: $result. Attempting fallback retry...")
                    lastError = result
                    continue
                }
                Log.i(TAG, "Successfully generated content using model: $model")
                return result
            }
        }
        
        return lastError.ifEmpty { "Error: All models in the fallback chain failed." }
    }

    private suspend fun tryModel(
        modelName: String,
        prompt: String,
        systemInstruction: String? = null,
        responseJsonSchema: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or is the default placeholder")
            return@withContext "Error: API key is not configured. Please configure GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        try {
            // Build the main contents structure
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Build system instruction if provided
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val systemPartsArray = JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemInstructionObj.put("parts", systemPartsArray)
                requestJson.put("systemInstruction", systemInstructionObj)
            }

            // Build generation config if needed
            val generationConfigJson = JSONObject()
            var hasConfig = false

            if (responseJsonSchema != null) {
                generationConfigJson.put("responseMimeType", "application/json")
                generationConfigJson.put("responseSchema", JSONObject(responseJsonSchema))
                hasConfig = true
            }

            if (hasConfig) {
                requestJson.put("generationConfig", generationConfigJson)
            }

            val requestBodyString = requestJson.toString()
            Log.d(TAG, "Request Body for $modelName: $requestBodyString")

            val request = Request.Builder()
                .url(url)
                .post(requestBodyString.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d(TAG, "Model $modelName Response Code: ${response.code}, Body: $responseBody")

                if (!response.isSuccessful) {
                    return@withContext "Error: API call failed with code ${response.code}. ${responseBody ?: ""}"
                }

                if (responseBody == null) {
                    return@withContext "Error: Empty response body returned from Gemini API."
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                return@withContext "Error: No text content found in Gemini response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in query with $modelName", e)
            return@withContext "Error: ${e.localizedMessage ?: e.message}"
        }
    }
}
