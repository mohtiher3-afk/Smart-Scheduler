package com.example.models

data class SessionResult(
    val dateString: String,      // e.g. "2026-05-24"
    val formattedDate: String,    // e.g. "24 مايو 2026"
    val dayNameArabic: String     // e.g. "الأحد"
)
