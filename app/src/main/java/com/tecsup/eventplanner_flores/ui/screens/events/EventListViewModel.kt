package com.tecsup.eventplanner_flores.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.eventplanner_flores.data.model.Event
import com.tecsup.eventplanner_flores.data.repository.AuthRepository
import com.tecsup.eventplanner_flores.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val eventToDelete: Event? = null,
    val isLoggingOut: Boolean = false
)

class EventListViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                eventRepository.getEventsRealTime().collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar eventos: ${e.message}"
                )
            }
        }
    }

    fun showDeleteDialog(event: Event) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            eventToDelete = event
        )
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            eventToDelete = null
        )
    }

    fun deleteEvent() {
        val event = _uiState.value.eventToDelete ?: return

        viewModelScope.launch {
            eventRepository.deleteEvent(event.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showDeleteDialog = false,
                        eventToDelete = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Error al eliminar: ${exception.message}",
                        showDeleteDialog = false,
                        eventToDelete = null
                    )
                }
        }
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true)
        // Solo hacer logout, la navegación se maneja en la UI
        authRepository.logout()
    }
}