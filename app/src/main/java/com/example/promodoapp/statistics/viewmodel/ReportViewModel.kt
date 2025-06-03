package com.example.promodoapp.statistics.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: "dxq0oNkT3MX9VuLWAIqrXN9X6tF2"

    // Trạng thái ngày và tháng được chọn
    private val _selectedDate: MutableState<String> = mutableStateOf(
        SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Calendar.getInstance().time)
    )
    val selectedDate: MutableState<String> = _selectedDate

    private val _selectedMonth: MutableState<String> = mutableStateOf(
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH).format(Calendar.getInstance().time)
    )
    val selectedMonth: MutableState<String> = _selectedMonth

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

    // Danh sách ngày có phiên học để tô vàng
    private val _highlightedDays: MutableState<List<Int>> = mutableStateOf(emptyList())
    val highlightedDays: MutableState<List<Int>> = _highlightedDays

    init {
        loadDailyStats()
        loadMonthlyStats()
        loadHighlightedDays()
    }

    // Cập nhật ngày được chọn
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadDailyStats()
    }

    // Cập nhật tháng được chọn
    fun setSelectedMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
        loadMonthlyStats()
        loadHighlightedDays()
    }

    // Tải dữ liệu thống kê hàng ngày
    private fun loadDailyStats() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val year = _selectedMonth.value.split("/")[0]
            val fullDate = "$date/$year"

            db.collection("users").document(userId)
                .collection("sessions_new")
                .whereEqualTo("completed", true)
                .whereEqualTo("date", fullDate)
                .get()
                .addOnSuccessListener { documents ->
                    println("loadDailyStats: Found ${documents.size()} documents for date $fullDate")
                    for (doc in documents) {
                        println("Document: date=${doc.getString("date")}, duration=${doc.getLong("duration")}")
                    }
                    _focusSessions.value = documents.size()
                    _totalTime.value = documents.mapNotNull { it.getLong("duration")?.times(60 * 1000) }.sum()
                }
                .addOnFailureListener { exception ->
                    println("Error loading daily stats: ${exception.message}")
                }
        }
    }

    // Tải dữ liệu thống kê hàng tháng
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            val yearMonth = _selectedMonth.value.split("/")
            val year = yearMonth[0]
            val month = yearMonth[1]

            db.collection("users").document(userId)
                .collection("sessions_new")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener { documents ->
                    val daysSet = mutableSetOf<String>()
                    var sessionsCount = 0
                    var totalDuration = 0L

                    for (doc in documents) {
                        val docDate = doc.getString("date") ?: continue
                        val docYearMonth = docDate.split("/").let { "${it[2]}/${it[1]}" }
                        if (docYearMonth == "$year/$month") {
                            daysSet.add(docDate)
                            sessionsCount++
                            totalDuration += (doc.getLong("duration") ?: 0L) * 60 * 1000
                        }
                    }

                    _monthlyFocusDays.value = daysSet.size
                    _monthlyFocusSessions.value = sessionsCount
                    _monthlyTotalTime.value = totalDuration
                    println("loadMonthlyStats: days=${daysSet.size}, sessions=$sessionsCount, totalTime=$totalDuration")
                }
                .addOnFailureListener { exception ->
                    println("Error loading monthly stats: ${exception.message}")
                }
        }
    }

    // Tải danh sách ngày có phiên học
    private fun loadHighlightedDays() {
        viewModelScope.launch {
            val yearMonth = _selectedMonth.value.split("/")
            val year = yearMonth[0]
            val month = yearMonth[1]

            db.collection("users").document(userId)
                .collection("sessions_new")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener { documents ->
                    val days = documents.mapNotNull { doc ->
                        val docDate = doc.getString("date") ?: return@mapNotNull null
                        val docYearMonth = docDate.split("/").let { "${it[2]}/${it[1]}" }
                        if (docYearMonth == "$year/$month") {
                            docDate.split("/")[0].toIntOrNull()
                        } else {
                            null
                        }
                    }.distinct()
                    _highlightedDays.value = days
                    println("loadHighlightedDays: days=$days")
                }
                .addOnFailureListener { exception ->
                    println("Error loading highlighted days: ${exception.message}")
                }
        }
    }
}