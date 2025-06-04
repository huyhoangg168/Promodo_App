package com.example.promodoapp.timer.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promodoapp.model.Session
import com.example.promodoapp.repository.AuthRepository
import com.example.promodoapp.repository.UserRepository
import com.example.promodoapp.timer.ui.Mode
import com.example.promodoapp.timer.ui.TimerState
import com.example.promodoapp.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    // Trạng thái đồng hồ
    private val _timerState: MutableState<TimerState> = mutableStateOf(TimerState.Idle)
    val timerState: MutableState<TimerState> = _timerState

    // Chế độ thời gian (Pomodoro hoặc Custom)
    private val _mode: MutableState<Mode> = mutableStateOf(Mode.Pomodoro)
    val mode: MutableState<Mode> = _mode

    // Thời gian học và nghỉ (theo phút)
    private val _workTime: MutableState<Int> = mutableStateOf(25)
    val workTime: MutableState<Int> = _workTime

    private val _breakTime: MutableState<Int> = mutableStateOf(5)
    val breakTime: MutableState<Int> = _breakTime

    // Thời gian hiện tại (theo giây)
    private val _currentTime: MutableState<Int> = mutableStateOf(_workTime.value * 60)
    val currentTime: MutableState<Int> = _currentTime

    // Đang ở giai đoạn học hay nghỉ
    private val _isWorkPhase: MutableState<Boolean> = mutableStateOf(true)
    val isWorkPhase: MutableState<Boolean> = _isWorkPhase

    // Số xu
    private val _coins: MutableState<Int> = mutableStateOf(100)
    val coins: MutableState<Int> = _coins

    // Trạng thái video hiện tại (study hoặc chill)
    private val _currentVideo: MutableState<VideoType> = mutableStateOf(VideoType.Study)
    val currentVideo: MutableState<VideoType> = _currentVideo

    // Trạng thái thông báo giai đoạn (để thông báo cho UI)
    private val _phaseChangeEvent: MutableState<PhaseChangeEvent?> = mutableStateOf(null)
    val phaseChangeEvent: MutableState<PhaseChangeEvent?> = _phaseChangeEvent

    // Biến để lưu thời gian bắt đầu của phiên hiện tại
    private var currentSessionStartTime: Long = 0

    // Job để đếm giờ
    private var timerJob: Job? = null

    // Biến để kiểm tra chu kỳ hoàn thành
    private var cycleCompleted = false

    //Biến quote
    private val _quote: MutableState<String> = mutableStateOf("Stay focused and keep going!")
    val quote: MutableState<String> = _quote

    // Khởi tạo: Tải số xu từ Firestore
    init {
        loadUserCoins()
    }

    // Tải số xu từ Firestore
    private fun loadUserCoins() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val user = userRepository.getUser(currentUser.uid)
                    if (user != null) {
                        _coins.value = user.coins
                        Log.d("MainViewModel", "Loaded coins from Firestore: ${user.coins}")
                        _quote.value = user.quote
                    } else {
                        // Nếu người dùng chưa có tài liệu, khởi tạo với 0 xu
                        val newUser = currentUser.copy(coins = 0)
                        userRepository.saveUser(newUser)
                        _coins.value = 0
                        Log.d("MainViewModel", "Initialized new user with 0 coins")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to load coins: ${e.message}")
                    _coins.value = 0 // Giá trị mặc định nếu lỗi
                }
            }
        } else {
            Log.w("MainViewModel", "No user logged in, coins set to 0")
            _coins.value = 0
        }
    }

    // Bắt đầu đếm giờ
    fun startTimer() {
        if (_timerState.value != TimerState.Running) {
            _timerState.value = TimerState.Running
            currentSessionStartTime = System.currentTimeMillis()
            startCountdown()
        }
    }

    // Tạm dừng đếm giờ
    fun pauseTimer() {
        if (_timerState.value == TimerState.Running) {
            _timerState.value = TimerState.Paused
            timerJob?.cancel()
        }
    }

    // Đặt lại thời gian
    fun resetTimer() {
        timerJob?.cancel()
        _isWorkPhase.value = true
        _currentVideo.value = VideoType.Study
        _currentTime.value = _workTime.value * 60
        cycleCompleted = false
        currentSessionStartTime = 0
    }

    // Hủy đếm giờ
    fun cancelTimer() {
        // Lưu phiên bị hủy
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null && currentSessionStartTime > 0) {
            //Tính thời gian đã học từ lúc ấn start
            val timeSpentInSeconds = when {
                _isWorkPhase.value -> _workTime.value * 60 - _currentTime.value
                else -> _breakTime.value * 60 - _currentTime.value
            }
            val timeSpentInMinutes = (timeSpentInSeconds / 60).coerceAtLeast(1)

            val session = Session(
                userId = currentUser.uid,
                type = if (_mode.value == Mode.Pomodoro) "pomodoro" else "custom",
                duration = timeSpentInMinutes,
                completed = false
            )
            viewModelScope.launch {
                try {
                    userRepository.saveSession(session)
                    Log.d("MainViewModel", "Saved canceled session: ${session.type}")
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to save canceled session: ${e.message}")
                }
            }
        }

        timerJob?.cancel()
        _timerState.value = TimerState.Idle
        _isWorkPhase.value = true
        _currentVideo.value = VideoType.Study
        _currentTime.value = _workTime.value * 60
        cycleCompleted = false
        currentSessionStartTime = 0
    }

    // Cập nhật chế độ (Pomodoro hoặc Custom)
    fun setMode(newMode: Mode, newWorkTime: Int? = null, newBreakTime: Int? = null) {
        updateMode(newMode, newWorkTime, newBreakTime)
        resetTimer()
    }

    // Hàm đếm giờ
    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_currentTime.value > 0  && _timerState.value == TimerState.Running) {
                delay(1000)
                _currentTime.value -= 1
                if (_currentTime.value == 0) {
                    switchPhase()
                }
            }
        }
    }

    // Hàm chuyển giai đoạn (học → nghỉ hoặc nghỉ → học)
    private fun switchPhase() {
        // Lưu phiên vừa hoàn thành
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null && currentSessionStartTime > 0) {
            val session = Session(
                userId = currentUser.uid,
                type = if (_mode.value == Mode.Pomodoro) "pomodoro" else "custom",
                duration = if (_isWorkPhase.value) _workTime.value else _breakTime.value,
                completed = true
            )
            viewModelScope.launch {
                try {
                    userRepository.saveSession(session)
                    Log.d("MainViewModel", "Saved session: ${session.type}")
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to save session: ${e.message}")
                }
            }
        }

        //Chuyển chế giữa chế độ 
        _isWorkPhase.value = !_isWorkPhase.value
        if (!_isWorkPhase.value) {
            cycleCompleted = true
            _currentVideo.value = VideoType.Chill // Chuyển sang video chilling
            Log.d("MainViewModel", "Switched to Break phase. Video: ${_currentVideo.value}")
            // Phát tín hiệu chuyển giai đoạn
            _phaseChangeEvent.value = PhaseChangeEvent.WorkToBreak
        } else {
            _currentVideo.value = VideoType.Study // Chuyển sang video học bài
            Log.d("MainViewModel", "Switched to Work phase. Video: ${_currentVideo.value}")
            if (cycleCompleted) {
                // Phát tín hiệu chuyển giai đoạn
                _phaseChangeEvent.value = PhaseChangeEvent.BreakToWork
                // Thưởng xu khi quay lại giai đoạn học sau một chu kỳ
                rewardCoins()
                cycleCompleted = false
            }
        }
        _currentTime.value = if (_isWorkPhase.value) _workTime.value * 60 else _breakTime.value * 60
        _timerState.value = TimerState.Paused // Dừng lại, đợi người dùng nhấn Play
        currentSessionStartTime = System.currentTimeMillis()
    }

    // Hàm thưởng xu
    private fun rewardCoins() {
        _coins.value += 10 // Thưởng 10 xu mỗi chu kỳ
        Log.d("MainViewModel", "Rewarded 10 coins. Total coins: ${_coins.value}")

        // Cập nhật số xu lên Firestore
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val updatedUser = currentUser.copy(coins = _coins.value)
                    userRepository.updateUser(updatedUser)
                    Log.d("MainViewModel", "Updated coins to Firestore: ${_coins.value}")
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to update coins to Firestore: ${e.message}")
                    // TODO: Có thể thêm logic thử lại hoặc thông báo lỗi cho người dùng
                }
            }
        } else {
            Log.w("MainViewModel", "No user logged in, cannot update coins to Firestore")
        }
    }

    // Hàm cập nhật chế độ
    private fun updateMode(newMode: Mode, newWorkTime: Int? = null, newBreakTime: Int? = null) {
        _mode.value = newMode
        if (newMode == Mode.Pomodoro) {
            _workTime.value = 25
            _breakTime.value = 5
        } else {
            _workTime.value = newWorkTime ?: _workTime.value
            _breakTime.value = newBreakTime ?: _breakTime.value
        }
        Log.d("MainViewModel", "Mode updated to: ${_mode.value}, Work time: ${_workTime.value}, Break time: ${_breakTime.value}")
    }

    // Hàm đăng xuất
    fun logout() {
        authRepository.logout()
    }

    // Reset sự kiện chuyển giai đoạn sau khi UI xử lý
    fun resetPhaseChangeEvent() {
        _phaseChangeEvent.value = null
    }

    //Hàm cập nhật quote
    fun updateQuote(newQuote: String) {
        _quote.value = newQuote
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val updatedUser = currentUser.copy(coins = _coins.value, quote = newQuote)
                    userRepository.updateUser(updatedUser)
                    Log.d("MainViewModel", "Quote updated to Firestore: $newQuote")
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to update quote: ${e.message}")
                }
            }
        }
    }
}

// Enum để biểu thị sự kiện chuyển giai đoạn
enum class PhaseChangeEvent {
    WorkToBreak, BreakToWork
}

enum class VideoType{
    Study, Chill
}