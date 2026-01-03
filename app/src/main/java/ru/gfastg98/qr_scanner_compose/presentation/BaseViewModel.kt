package ru.gfastg98.qr_scanner_compose.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<STATE, EVENT, ACTION>(initialState: STATE) : ViewModel() {
    protected val currentState = MutableStateFlow(initialState)
    val state = currentState.asStateFlow()

    protected val eventChannel = Channel<EVENT>()
    val event = eventChannel.receiveAsFlow()

    abstract fun onAction(action: ACTION)
    protected fun onEvent(event: EVENT) = viewModelScope.launch { eventChannel.send(event) }
}