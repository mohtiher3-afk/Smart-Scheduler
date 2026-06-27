package com.example.services

import com.example.models.Course

object ConflictDetector {

    data class ConflictResult(
        val firstCourse: Course,
        val secondCourse: Course,
        val conflictingDays: List<String>,
        val timeSpan: String
    )

    /**
     * Checks if a new or updated course conflicts with any of the existing active courses.
     */
    fun findConflicts(newCourse: Course, existingCourses: List<Course>): List<ConflictResult> {
        if (newCourse.status != "نشط") return emptyList()
        
        val conflicts = mutableListOf<ConflictResult>()
        val newDays = parseDaysToSet(newCourse.days)
        val newSpan = parseTimeToMinutesSpan(newCourse.timeStart, newCourse.timeEnd) ?: return emptyList()

        for (course in existingCourses) {
            if (course.id == newCourse.id || course.status != "نشط") continue
            
            val courseDays = parseDaysToSet(course.days)
            val commonDays = newDays.intersect(courseDays)
            
            if (commonDays.isNotEmpty()) {
                val otherSpan = parseTimeToMinutesSpan(course.timeStart, course.timeEnd) ?: continue
                if (spansOverlap(newSpan, otherSpan)) {
                    val daysText = commonDays.toList()
                    conflicts.add(
                        ConflictResult(
                            firstCourse = newCourse,
                            secondCourse = course,
                            conflictingDays = daysText,
                            timeSpan = "${course.timeStart} - ${course.timeEnd}"
                        )
                    )
                }
            }
        }
        return conflicts
    }

    private fun spansOverlap(span1: Pair<Int, Int>, span2: Pair<Int, Int>): Boolean {
        return span1.first < span2.second && span2.first < span1.second
    }

    private fun parseDaysToSet(daysStr: String): Set<String> {
        val clean = daysStr.replace("،", " ").replace(",", " ")
        val set = mutableSetOf<String>()
        if (clean.contains("الأحد") || clean.contains("الاحد")) set.add("الأحد")
        if (clean.contains("الاثنين") || clean.contains("الإثنين") || clean.contains("الِإثنين")) set.add("الاثنين")
        if (clean.contains("الثلاثاء")) set.add("الثلاثاء")
        if (clean.contains("الأربعاء") || clean.contains("الاربعاء")) set.add("الأربعاء")
        if (clean.contains("الخميس")) set.add("الخميس")
        if (clean.contains("الجمعة")) set.add("الجمعة")
        if (clean.contains("السبت")) set.add("السبت")
        return set
    }

    private fun parseTimeToMinutesSpan(startStr: String, endStr: String): Pair<Int, Int>? {
        val startMins = parseTimeToMinutes(startStr) ?: return null
        val endMins = parseTimeToMinutes(endStr) ?: return null
        return Pair(startMins, endMins)
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        return try {
            val clean = timeStr.trim()
            val isPm = clean.contains("م") || clean.lowercase().contains("pm")
            val numbersOnly = clean.replace(Regex("[^0-9:]"), "")
            val parts = numbersOnly.split(":")
            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            if (isPm && hours < 12) hours += 12
            else if (!isPm && hours == 12) hours = 0

            hours * 60 + minutes
        } catch (e: Exception) {
            null
        }
    }
}
