package com.ticketing.mobile.core_mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing Unidirectional Data Flow (MVI Pattern).
 *
 * @param State The immutable UI state type.
 * @param Intent The user action / intent type.
 * @param Effect The single-event side effect type.
 */
abstract class BaseViewModel<State : UiState, Intent : UiIntent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effect: Channel<Effect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * Entry point for processing user intents dispatched from UI.
     */
    abstract fun handleIntent(intent: Intent)

    /**
     * Atomically mutate the current UI state.
     */
    protected fun setState(reducer: State.() -> State) {
        _uiState.update { currentState -> currentState.reducer() }
    }

    /**
     * Dispatch a one-time side effect to the UI.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Helper to read current snapshot state.
     */
    val currentState: State
        get() = _uiState.value
}
