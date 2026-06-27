package com.example.models

import java.util.UUID

data class InAppToastData(
    val id: String = UUID.randomUUID().toString(),
    val courseName: String,
    val zoomLink: String,
    val timeStr: String,
    val durationMillis: Long = 12000L, // Delightful Material 3 Expressive duration (12 seconds)
    val isTest: Boolean = false
)
