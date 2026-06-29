package com.example

import com.example.feature.alerts.presentation.AlertsViewModel
import com.example.feature.analytics.presentation.AnalyticsViewModel
import com.example.feature.calculator.presentation.CalculatorViewModel
import com.example.feature.calendar.presentation.CalendarViewModel
import com.example.feature.courses.presentation.CoursesViewModel
import com.example.feature.dashboard.presentation.DashboardViewModel
import com.example.feature.exams.presentation.ExamsViewModel
import com.example.feature.files.presentation.FilesViewModel
import com.example.feature.more.presentation.MoreViewModel
import com.example.feature.notes.presentation.NotesViewModel
import com.example.feature.profile.presentation.ProfileViewModel
import com.example.feature.schedule.presentation.ScheduleViewModel
import com.example.feature.smartscheduler.presentation.SmartSchedulerViewModel
import com.example.feature.studyhub.presentation.StudyHubViewModel
import com.example.feature.synccenter.presentation.SyncCenterViewModel
import com.example.feature.tasks.presentation.TasksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { DashboardViewModel() }
    viewModel { ScheduleViewModel() }
    viewModel { CalendarViewModel() }
    viewModel { StudyHubViewModel() }
    viewModel { NotesViewModel() }
    viewModel { AlertsViewModel() }
    viewModel { AnalyticsViewModel() }
    viewModel { CalculatorViewModel() }
    viewModel { FilesViewModel() }
    viewModel { SmartSchedulerViewModel() }
    viewModel { TasksViewModel() }
    viewModel { CoursesViewModel() }
    viewModel { ExamsViewModel() }
    viewModel { MoreViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { SyncCenterViewModel() }
}
