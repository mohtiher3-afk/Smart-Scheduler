package com.example.models

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val courseId: String? = null
)
