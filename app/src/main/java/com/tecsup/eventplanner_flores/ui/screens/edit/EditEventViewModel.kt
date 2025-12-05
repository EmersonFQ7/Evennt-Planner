package com.tecsup.eventplanner_flores.ui.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.eventplanner_flores.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditEventUiState(
    val eventId: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isLoadingData: Boolean = true,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class EditEventViewModel : ViewModel() {
    private val eventRepository = EventRepository()

    private val _uiState = MutableStateFlow(EditEventUiState())
    val uiState: StateFlow<EditEventUiState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String) {
        _uiState.value = _uiState.value.copy(eventId = eventId)

        viewModelScope.launch {
            eventRepository.getEventsRealTime().collect { events ->
                val event = events.find { it.id == eventId }
                if (event != null) {
                    _uiState.value = _uiState.value.copy(
                        title = event.title,
                        date = event.date,
                        description = event.description,
                        isLoadingData = false
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title, error = null)
    }

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(date = date, error = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }

    fun updateEvent() {
        val title = _uiState.value.title.trim()
        val date = _uiState.value.date.trim()
        val description = _uiState.value.description.trim()
        val eventId = _uiState.value.eventId

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

            eventRepository.updateEvent(eventId, title, date, description)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al actualizar evento: ${exception.message}"
                    )
                }
        }
    }
}