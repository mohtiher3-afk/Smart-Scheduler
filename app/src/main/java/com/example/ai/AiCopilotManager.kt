package com.example.ai

import com.example.models.Course
import com.example.services.GeminiClient
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log

object AiCopilotManager {
    private const val TAG = "AiCopilotManager"

    suspend fun getDashboardRecommendations(courses: List<Course>, currentLang: String): List<AiRecommendation> {
        val context = courses.joinToString("\n") { "${it.name}: ${it.days} at ${it.timeStart}" }
        val prompt = """
            Based on the following student courses, suggest 2 immediate AI recommendations for today.
            Courses:
            $context
            
            Return ONLY a JSON array of objects with fields: title, description, actionLabel, actionType.
            actionType can be "START_STUDY", "OPTIMIZE_SCHEDULE", or "REVIEW_NOTES".
            Language: $currentLang
        """.trimIndent()

        val response = GeminiClient.generateContent(prompt) ?: "[]"
        return try {
            val jsonArray = JSONArray(response.substringAfter("[").substringBeforeLast("]") + "]")
            val list = mutableListOf<AiRecommendation>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(AiRecommendation(
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    actionLabel = obj.getString("actionLabel"),
                    actionType = obj.getString("actionType")
                ))
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing dashboard recommendations", e)
            emptyList()
        }
    }

    suspend fun optimizeSchedule(courses: List<Course>, currentLang: String): ScheduleOptimization? {
        val context = courses.joinToString("\n") { "${it.name}: ${it.days} at ${it.timeStart}" }
        val prompt = """
            Analyze this schedule for conflicts or heavy days. 
            Courses:
            $context
            
            Suggest moves if needed. Return ONLY a JSON object with fields: reason, suggestions (array of objects with taskName, fromDay, toDay, confidence).
            Language: $currentLang
        """.trimIndent()

        val response = GeminiClient.generateContent(prompt) ?: return null
        return try {
            val obj = JSONObject(response.substringAfter("{").substringBeforeLast("}") + "}")
            val suggestionsArray = obj.getJSONArray("suggestions")
            val suggestions = mutableListOf<MoveSuggestion>()
            for (i in 0 until suggestionsArray.length()) {
                val sObj = suggestionsArray.getJSONObject(i)
                suggestions.add(MoveSuggestion(
                    taskName = sObj.getString("taskName"),
                    fromDay = sObj.getString("fromDay"),
                    toDay = sObj.getString("toDay"),
                    confidence = sObj.getDouble("confidence").toFloat()
                ))
            }
            ScheduleOptimization(obj.getString("reason"), suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing schedule optimization", e)
            null
        }
    }

    suspend fun getStudyCoachFeedback(stats: String, currentLang: String): StudyCoachFeedback? {
        val prompt = """
            Based on these study stats: $stats
            Provide study coach feedback. Return ONLY a JSON object with fields: score (0-100), focusLevel, weakSubject, recommendation.
            Language: $currentLang
        """.trimIndent()

        val response = GeminiClient.generateContent(prompt) ?: return null
        return try {
            val obj = JSONObject(response.substringAfter("{").substringBeforeLast("}") + "}")
            StudyCoachFeedback(
                score = obj.getInt("score"),
                focusLevel = obj.getString("focusLevel"),
                weakSubject = obj.getString("weakSubject"),
                recommendation = obj.getString("recommendation")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing coach feedback", e)
            null
        }
    }
}
