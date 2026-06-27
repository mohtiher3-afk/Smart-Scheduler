package com.example.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RemoteConfigManager {

    data class AppConfig(
        val zoomCheckingEnabled: Boolean = true,
        val motdArabic: String = "💡 تلميح اليوم: يمكنك الضغط مطولاً على أيقونة التطبيق في شاشتك الرئيسية للوصول السريع إلى أهم دورة تدريبية!",
        val motdEnglish: String = "💡 Tip of the Day: Long press the app launcher icon to quick-jump to your active courses!",
        val supportContact: String = "support@smart-scheduler.com",
        val announcement: String = "📢 تحديث هام: تم دمج خوارزمية اكتشاف تعارض مواعيد المحاضرات وتشفير روابط زووم بالكامل لحماية خصوصيتك!"
    )

    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    suspend fun fetchLatestConfig() {
        _isFetching.value = true
        DiagnosticLogger.log("INFO", "RemoteConfig", "جاري الاتصال بخادم Remote Config لاسترجاع الإعدادات المحدثة...")
        delay(1200) // Simulate network delay
        
        // Return loaded custom values
        _config.value = AppConfig(
            zoomCheckingEnabled = true,
            motdArabic = "💡 تلميح ذكي: يمكنك الآن ربط مواعيد دوراتك التدريبية مباشرة بتقويم نظام أندرويد من التبويب المخصص!",
            motdEnglish = "💡 Smart Tip: You can now synchronize your course schedule to Android's native Calendar system!",
            supportContact = "help@courses-scheduler.ai",
            announcement = "🚀 مرحباً بك في الإصدار المحدث بنظام Material Design 3 والذكاء الاصطناعي الذكي!"
        )
        _isFetching.value = false
        DiagnosticLogger.log("INFO", "RemoteConfig", "تم استيراد إعدادات الخادم السحابية بنجاح بنسبة 100%.")
    }
}
