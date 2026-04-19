package com.sport.timer

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class Phase { IDLE, PREP, WORK, REST, DONE }

data class TimerState(
    val workTimeSeconds: Int = 60,
    val restTimeSeconds: Int = 30,
    val repetitions: Int = 5,
    val phase: Phase = Phase.IDLE,
    val currentSeconds: Int = 0,
    val currentRep: Int = 0,
    val isRunning: Boolean = false
)

class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state

    private var timerJob: Job? = null
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    fun setWorkTime(seconds: Int) {
        if (_state.value.phase == Phase.IDLE) {
            _state.value = _state.value.copy(workTimeSeconds = seconds)
        }
    }

    fun setRestTime(seconds: Int) {
        if (_state.value.phase == Phase.IDLE) {
            _state.value = _state.value.copy(restTimeSeconds = seconds)
        }
    }

    fun setRepetitions(count: Int) {
        if (_state.value.phase == Phase.IDLE) {
            _state.value = _state.value.copy(repetitions = count)
        }
    }

    fun toggleStartPause() {
        if (_state.value.isRunning) {
            pause()
        } else {
            when (_state.value.phase) {
                Phase.IDLE, Phase.DONE -> startFresh()
                else -> resumeTimer()
            }
        }
    }

    fun reset() {
        timerJob?.cancel()
        _state.value = TimerState(
            workTimeSeconds = _state.value.workTimeSeconds,
            restTimeSeconds = _state.value.restTimeSeconds,
            repetitions = _state.value.repetitions
        )
    }

    private fun startFresh() {
        _state.value = _state.value.copy(currentRep = 1, isRunning = true)
        launchPhase(Phase.PREP, 5)
    }

    private fun pause() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isRunning = false)
    }

    private fun resumeTimer() {
        _state.value = _state.value.copy(isRunning = true)
        runCountdown()
    }

    private fun launchPhase(phase: Phase, seconds: Int) {
        _state.value = _state.value.copy(phase = phase, currentSeconds = seconds, isRunning = true)
        runCountdown()
    }

    private fun runCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.currentSeconds > 0) {
                if (_state.value.currentSeconds <= 5) beep()
                delay(1000)
                _state.value = _state.value.copy(currentSeconds = _state.value.currentSeconds - 1)
            }
            onPhaseComplete()
        }
    }

    private fun onPhaseComplete() {
        val s = _state.value
        when (s.phase) {
            Phase.PREP -> launchPhase(Phase.WORK, s.workTimeSeconds)
            Phase.WORK -> {
                if (s.restTimeSeconds > 0) launchPhase(Phase.REST, s.restTimeSeconds)
                else onRepComplete()
            }
            Phase.REST -> onRepComplete()
            else -> {}
        }
    }

    private fun onRepComplete() {
        val s = _state.value
        if (s.currentRep < s.repetitions) {
            _state.value = s.copy(currentRep = s.currentRep + 1)
            launchPhase(Phase.WORK, s.workTimeSeconds)
        } else {
            _state.value = s.copy(phase = Phase.DONE, isRunning = false, currentSeconds = 0)
            beepFinal()
        }
    }

    private fun beep() {
        try { toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 180) } catch (_: Exception) {}
    }

    private fun beepFinal() {
        try { toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000) } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        toneGen.release()
    }
}
