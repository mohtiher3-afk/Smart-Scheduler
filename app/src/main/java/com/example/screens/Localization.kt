package com.example.screens

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "ar" }

class Loc(val lang: String) {
    // General
    val appTitle = if (lang == "ar") "Smart Scheduler" else "Smart Scheduler"
    val appSubtitle = if (lang == "ar") "تنظيم متكامل للمحاضرات، تنبيهات زووم، وحوسبة التواريخ بدقة وسهولة" else "Comprehensive course organization, Zoom alerts, and precise date calculation"
    val active = if (lang == "ar") "نشط" else "Active"
    val inactive = if (lang == "ar") "غير نشط" else "Inactive"
    val completed = if (lang == "ar") "مكتمل" else "Completed"
    val notCompleted = if (lang == "ar") "غير مكتمل" else "Not Completed"
    val yes = if (lang == "ar") "نعم" else "Yes"
    val no = if (lang == "ar") "لا" else "No"
    val error = if (lang == "ar") "خطأ" else "Error"
    val success = if (lang == "ar") "نجاح" else "Success"
    val unknown = if (lang == "ar") "غير معروف" else "Unknown"

    // Tabs
    val tabSchedule = if (lang == "ar") "جدول الدورات" else "Courses"
    val tabDashboard = if (lang == "ar") "لوحة البيانات" else "Dashboard"
    val tabCalculator = if (lang == "ar") "الحاسبة الذكية" else "Smart Calculator"
    val tabAlerts = if (lang == "ar") "التنبيهات" else "Alerts"
    val tabSmartScheduler = if (lang == "ar") "المجدول الذكي" else "Smart Scheduler"

    // Buttons
    val addCourse = if (lang == "ar") "إضافة دورة" else "Add Course"
    val editCourse = if (lang == "ar") "تعديل" else "Edit"
    val delete = if (lang == "ar") "حذف" else "Delete"
    val cancel = if (lang == "ar") "إلغاء" else "Cancel"
    val save = if (lang == "ar") "حفظ" else "Save"
    val edit = if (lang == "ar") "تعديل" else "Edit"
    val select = if (lang == "ar") "تحديد" else "Select"
    val send = if (lang == "ar") "إرسال" else "Send"
    val clear = if (lang == "ar") "مسح" else "Clear"

    // ScheduleTab
    val lastLecture = if (lang == "ar") "آخر محاضرة منجزة:" else "Last completed lecture:"
    val targetLecturesCount = if (lang == "ar") "العدد المستهدف:" else "Target lectures:"
    val zoomLink = if (lang == "ar") "حساب زووم:" else "Zoom Link:"
    val copyZoom = if (lang == "ar") "نسخ الرابط" else "Copy Link"
    val copied = if (lang == "ar") "تم النسخ" else "Copied"
    val joinZoom = if (lang == "ar") "الانضمام عبر Zoom" else "Join Zoom"
    val studyAlerts = if (lang == "ar") "تنبيهات المحاضرات" else "Lecture Alerts"
    val refreshAlarms = if (lang == "ar") "تحديث التنبيهات" else "Refresh Alerts"
    val exportCSV = if (lang == "ar") "تصدير جدول CSV" else "Export CSV"
    val searchPlaceholder = if (lang == "ar") "البحث عن دورة..." else "Search courses..."
    val allCoursesFilter = if (lang == "ar") "الكل" else "All"
    val activeFilter = if (lang == "ar") "النشطة" else "Active"
    val inactiveFilter = if (lang == "ar") "غير النشطة" else "Inactive"
    val emptyCoursesList = if (lang == "ar") "لا توجد دورات دراسية مضافة حالياً!" else "No courses added yet!"
    val emptyCoursesDesc = if (lang == "ar") "انقر على زر \"إضافة دورة\" في الأسفل أو استخدم \"المجدول الذكي\" لبدء تنظيم محاضراتك فوراً." else "Click \"Add Course\" below or use \"Smart Scheduler\" to organize your lectures immediately."
    val testAlarm = if (lang == "ar") "تنبيه تجريبي" else "Test Alarm"
    val testAlarmDesc = if (lang == "ar") "سيصلك تنبيه فوري بعد 5 ثوانٍ" else "You will receive a notification in 5 seconds"

    // DashboardTab
    val progressHeader = if (lang == "ar") "إحصائيات الإنجاز والتعلم" else "Learning & Completion Stats"
    val overallProgress = if (lang == "ar") "الإنجاز العام" else "Overall Progress"
    val completedCourses = if (lang == "ar") "دورات مكتملة" else "Completed Courses"
    val completedLectures = if (lang == "ar") "المحاضرات" else "Lectures"
    val activityLog = if (lang == "ar") "سجل النشاط والتفاصيل المباشرة" else "Activity Log & Direct Details"
    val courseProgress = if (lang == "ar") "تقدم المساقات الدراسية" else "Course Progress"
    val startCalculating = if (lang == "ar") "ابدأ بالحساب" else "Calculate"
    val progressDetail = if (lang == "ar") "محاضرة مكتملة" else "completed lecture"
    val progressOf = if (lang == "ar") "من" else "of"
    val recentActivity = if (lang == "ar") "النشاط الأخير" else "Recent Activity"
    val progressChartTitle = if (lang == "ar") "مخطط تقدّم المساقات الدراسية" else "Course Progress Chart"

    // CalculatorTab
    val selectCourseToCalc = if (lang == "ar") "اختر الدورة المراد حساب لقاءاتها:" else "Select course to calculate sessions:"
    val selectTimePeriod = if (lang == "ar") "حدد الفترة الزمنية:" else "Select date range:"
    val fromDate = if (lang == "ar") "من تاريخ" else "From Date"
    val toDate = if (lang == "ar") "إلى تاريخ" else "To Date"
    val expectedLecturesCount = if (lang == "ar") "عدد اللقاءات المتوقعة خلال هذه الفترة:" else "Expected lectures during this period:"
    val lectureTerm = if (lang == "ar") "محاضرة" else "lecture(s)"
    val scheduledLecturesList = if (lang == "ar") "قائمة اللقاءات المجدولة" else "Scheduled Lectures List"
    val reminderStatus = if (lang == "ar") "حالة التنبيه" else "Alert Status"
    val activeAlarm = if (lang == "ar") "جرس نشط" else "Active Alarm"
    val activateAlarm = if (lang == "ar") "تفعيل الجرس" else "Activate Alarm"
    val lectureIndex = if (lang == "ar") "اللقاء" else "Session"
    val lectureCompletedLabel = if (lang == "ar") "هل أنجزت هذا اللقاء؟" else "Have you completed this session?"

    // RemindersTab
    val noReminders = if (lang == "ar") "لا توجد تنبيهات نشطة حالياً" else "No active alerts scheduled"
    val noRemindersDesc = if (lang == "ar") "انقر على رمز الجرس بجانب مواعيد المحاضرات (أو جدول الحاسبة الذكية) لتفعيل التنبيهات والأجراس الذكية تلقائياً!" else "Click the bell icon next to any lecture session in the Smart Calculator to schedule dynamic alerts!"
    val scheduledRemindersTitle = if (lang == "ar") "التنبيهات المجدولة" else "Scheduled Alerts"
    val broadcastDate = if (lang == "ar") "تاريخ البث المباشر:" else "Broadcast Date:"
    val cancelReminder = if (lang == "ar") "إلغاء التنبيه" else "Cancel Alert"

    // SmartSchedulerTab
    val aiTitle = if (lang == "ar") "المجدول الذكي بالذكاء الاصطناعي (Gemini)" else "Gemini AI Smart Scheduler"
    val aiDesc = if (lang == "ar") "اكتب مواعيد دورتك التدريبية كما وصلتك وسيقوم الذكاء الاصطناعي بتحليلها وبناء مساق دراسي منظم تلقائياً!" else "Enter your course details or schedule text, and our AI will automatically parse and build a structured course schedule!"
    val aiHint = if (lang == "ar") "مثال: أريد إضافة دورة جديدة اسمها 'برمجة أندرويد' تبث أيام الأحد والأربعاء الساعة 6:15 مساءً ولدينا 15 محاضرة." else "Example: I have a course named 'Android Dev' on Sundays and Wednesdays at 6:15 PM, 15 lectures total."
    val parseBtn = if (lang == "ar") "تحليل الجدولة بالذكاء الاصطناعي" else "Analyze Schedule with AI"
    val parsing = if (lang == "ar") "جاري التحليل ومعالجة البيانات..." else "Analyzing schedule..."
    val parsedCoursePreviewTitle = if (lang == "ar") "معاينة الدورة المستخرجة" else "Extracted Course Preview"
    val parsedCourseName = if (lang == "ar") "اسم الدورة:" else "Course Name:"
    val parsedCourseDays = if (lang == "ar") "أيام البث:" else "Broadcast Days:"
    val parsedCourseTime = if (lang == "ar") "التوقيت:" else "Time:"
    val parsedCourseZoom = if (lang == "ar") "رابط زووم:" else "Zoom Link:"
    val parsedCourseTarget = if (lang == "ar") "عدد المحاضرات:" else "Total Lectures:"
    val addParsedCourse = if (lang == "ar") "إضافة هذه الدورة للجدول" else "Add This Course to Schedule"
    val discardParsed = if (lang == "ar") "تجاهل" else "Discard"
    val aiConsultTitle = if (lang == "ar") "الاستشارات الأكاديمية والخطط الدراسية" else "Academic Guidance & Study Plans"
    val aiConsultDesc = if (lang == "ar") "اسأل Gemini للحصول على خطة مراجعة مخصصة لجدولك الحالي، أو استشره حول طرق تنظيم وقتك!" else "Ask Gemini for study recommendations, revision plans, or dynamic tips tailored to your active courses!"
    val aiConsultHint = if (lang == "ar") "مثال: كيف أنظم وقتي لمراجعة دورة البرمجة؟" else "Example: How can I structure my weekly review for Android Dev?"
    val chatPlaceholder = if (lang == "ar") "اكتب استشارتك هنا..." else "Type your request here..."
    val emptyChatHistory = if (lang == "ar") "لم تبدأ أي محادثة بعد. استشر مساعدك الذكي الآن!" else "No messages yet. Ask your academic assistant!"

    // AddEditCourseDialog
    val addCourseTitle = if (lang == "ar") "إضافة دورة دراسية جديدة" else "Add New Course"
    val editCourseTitle = if (lang == "ar") "تعديل المساق الدراسي" else "Edit Course"
    val courseNameLabel = if (lang == "ar") "اسم الدورة التدريبية" else "Course Name"
    val courseNameHint = if (lang == "ar") "أدخل اسم الدورة..." else "Enter course name..."
    val chooseDays = if (lang == "ar") "اختر أيام المحاضرات:" else "Select Lecture Days:"
    val startTimeLabel = if (lang == "ar") "وقت البدء" else "Start Time"
    val endTimeLabel = if (lang == "ar") "وقت الانتهاء" else "End Time"
    val timeHint = if (lang == "ar") "مثال: 06:15 م" else "e.g., 06:15 PM"
    val zoomLinkOptional = if (lang == "ar") "رابط زووم للقاء (اختياري)" else "Zoom link / account (optional)"
    val zoomLinkHint = if (lang == "ar") "رابط الانضمام..." else "https://zoom.us/..."
    val targetCountLabel = if (lang == "ar") "العدد المستهدف من المحاضرات" else "Target Number of Lectures"
    val statusLabel = if (lang == "ar") "حالة الدورة (نشطة وتتلقى تنبيهات)" else "Course Status (active for alerts)"
    val leadTimeLabel = if (lang == "ar") "توقيت التنبيه المسبق" else "Pre-alert lead time"
    val leadTimeSuffix = if (lang == "ar") "دقيقة قبل المحاضرة" else "minutes before lecture"
    val themeColorLabel = if (lang == "ar") "لون السمة المميز للدورة:" else "Course Theme Color:"
    val fillAllRequired = if (lang == "ar") "يرجى تعبئة جميع الحقول المطلوبة واختيار يوم واحد على الأقل!" else "Please fill all required fields and select at least one day!"

    // Days translation
    val sun = if (lang == "ar") "الأحد" else "Sunday"
    val mon = if (lang == "ar") "الاثنين" else "Monday"
    val tue = if (lang == "ar") "الثلاثاء" else "Tuesday"
    val wed = if (lang == "ar") "الأربعاء" else "Wednesday"
    val thu = if (lang == "ar") "الخميس" else "Thursday"
    val fri = if (lang == "ar") "الجمعة" else "Friday"
    val sat = if (lang == "ar") "السبت" else "Saturday"

    fun getDayName(dayIndex: Int): String {
        return when (dayIndex) {
            0 -> sun
            1 -> mon
            2 -> tue
            3 -> wed
            4 -> thu
            5 -> fri
            6 -> sat
            else -> ""
        }
    }

    // Settings Panel Titles / Options
    val settingsTitle = if (lang == "ar") "إعدادات التطبيق" else "App Settings"
    val selectLanguage = if (lang == "ar") "لغة التطبيق" else "App Language"
    val selectTheme = if (lang == "ar") "مظهر التطبيق" else "App Theme"
    val darkTheme = if (lang == "ar") "داكن" else "Dark"
    val lightTheme = if (lang == "ar") "فاتح" else "Light"
    val systemTheme = if (lang == "ar") "تلقائي (النظام)" else "System (Auto)"
    val enableDynamicColor = if (lang == "ar") "ألوان ديناميكية (Material You)" else "Dynamic Colors (Material You)"
    val dynamicColorDesc = if (lang == "ar") "تطبيق ألوان متناسقة من خلفية الشاشة" else "Apply color accents derived from wallpaper"
    val settingsSaved = if (lang == "ar") "تم حفظ الإعدادات" else "Settings saved"
    val changeLanguageTo = if (lang == "ar") "English" else "العربية"
    val close = if (lang == "ar") "إغلاق" else "Close"
}

fun translateDaysToArabic(daysStr: String, currentLang: String): String {
    if (currentLang == "ar") {
        // Already in Arabic or should be Arabic anyway. Let's make sure it handles any mapping
        return daysStr
    }
    // Map English days/indices or translate from Arabic to English
    val daysList = daysStr.split("،", ",", " ").map { it.trim() }.filter { it.isNotEmpty() }
    val translated = daysList.map { day ->
        when (day) {
            "الأحد", "الاحد" -> "Sunday"
            "الاثنين" -> "Monday"
            "الثلاثاء" -> "Tuesday"
            "الأربعاء", "الاربعاء" -> "Wednesday"
            "الخميس" -> "Thursday"
            "الجمعة" -> "Friday"
            "السبت" -> "Saturday"
            else -> day
        }
    }
    return translated.joinToString(", ")
}
