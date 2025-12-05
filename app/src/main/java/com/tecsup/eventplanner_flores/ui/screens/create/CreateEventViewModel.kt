package com.tecsup.eventplanner_flores.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.eventplanner_flores.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateEventUiState(
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class CreateEventViewModel : ViewModel() {
    private val eventRepository = EventRepository()

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title, error = null)
    }

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(date = date, error = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }

    fun createEvent() {
        val title = _uiState.value.title.trim()
        val date = _uiState.value.date.trim()
        val description = _uiState.value.description.trim()

        if (title.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "El título es obligatorio")
            return
        }

        if (date.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "La fecha es obligatoria")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            eventRepository.createEvent(title, date, description)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al crear evento: ${exception.message}"
                    )
                }
        }
    }
}