package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object DiagnosticLogger {

    data class LogEntry(
        val timestamp: String,
        val level: String, // INFO, WARN, ERROR
        val tag: String,
        val message: String
    )

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    init {
        log("INFO", "DiagnosticLogger", "تم تهيئة نظام المراقبة والأخطاء المحلّي (Observability Layer) بنجاح.")
    }

    fun log(level: String, tag: String, message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val entry = LogEntry(
            timestamp = sdf.format(Date()),
            level = level,
            tag = tag,
            message = message
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry) // Add at start for latest-first viewing
        // Limit to last 50 entries
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
        log("INFO", "DiagnosticLogger", "تم مسح سجلّ التشخيص البرمجي.")
    }
}
