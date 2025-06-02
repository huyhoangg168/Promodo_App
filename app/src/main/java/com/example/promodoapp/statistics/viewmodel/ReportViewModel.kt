package com.example.promodoapp.statistics.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// com.example.promodoapp.data.repository.SessionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ReportViewModel : ViewModel() {
    //private val sessionRepository = SessionRepository()

    // Trạng thái ngày và tháng được chọn
    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedDate: MutableState<String> = mutableStateOf(
        LocalDate.now().toString().substring(8, 10) + "/" + LocalDate.now().toString().substring(5, 7)
    )
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedDate: MutableState<String> = _selectedDate

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedMonth: MutableState<YearMonth> = mutableStateOf(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedMonth: MutableState<YearMonth> = _selectedMonth

    // Thống kê hàng ngày
    private val _focusSessions: MutableState<Int> = mutableStateOf(0)
    val focusSessions: MutableState<Int> = _focusSessions

    private val _totalTime: MutableState<Long> = mutableStateOf(0L)
    val totalTime: MutableState<Long> = _totalTime

    // Thống kê hàng tháng
    private val _monthlyFocusDays: MutableState<Int> = mutableStateOf(0)
    val monthlyFocusDays: MutableState<Int> = _monthlyFocusDays

    private val _monthlyFocusSessions: MutableState<Int> = mutableStateOf(0)
    val monthlyFocusSessions: MutableState<Int> = _monthlyFocusSessions

    private val _monthlyTotalTime: MutableState<Long> = mutableStateOf(0L)
    val monthlyTotalTime: MutableState<Long> = _monthlyTotalTime

    init {
        loadDailyStats()
        loadMonthlyStats()
    }

    // Cập nhật ngày được chọn
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadDailyStats()
    }

    // Cập nhật tháng được chọn
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadMonthlyStats()
    }

    // Tải dữ liệu thống kê hàng ngày
    private fun loadDailyStats() {
        viewModelScope.launch {
//            val (sessions, time) = sessionRepository.getDailyStats(_selectedDate.value)
//            _focusSessions.value = sessions
//            _totalTime.value = time
        }
    }

    // Tải dữ liệu thống kê hàng tháng
    private fun loadMonthlyStats() {
        viewModelScope.launch {
//            val (focusDays, focusSessions, totalTime) = sessionRepository.getMonthlyStats(_selectedMonth.value)
//            _monthlyFocusDays.value = focusDays
//            _monthlyFocusSessions.value = focusSessions
//            _monthlyTotalTime.value = totalTime
        }
    }
}