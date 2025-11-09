package org.apps.minisosmed.state

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object Navigate : UiEvent()
}
