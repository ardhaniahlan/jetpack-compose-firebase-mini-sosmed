package org.apps.minisosmed.state

interface UiState {
    val isLoading: Boolean
    val success: String?
    val message: String?
}
