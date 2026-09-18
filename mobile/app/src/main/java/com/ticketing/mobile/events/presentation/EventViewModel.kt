package com.ticketing.mobile.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.events.domain.model.EventItem
import com.ticketing.mobile.events.domain.repository.IEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListUiState(
    val events: List<EventItem> = emptyList(),
    val filteredEvents: List<EventItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "Tất cả",
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class EventViewModel(
    private val repository: IEventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getEvents()) {
                is NetworkResult.Success -> {
                    val events = result.data
                    _uiState.update { current ->
                        current.copy(
                            events = events,
                            filteredEvents = filterEvents(events, current.selectedCategory, current.searchQuery),
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không thể tải sự kiện") }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                filteredEvents = filterEvents(current.events, category, current.searchQuery)
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredEvents = filterEvents(current.events, current.selectedCategory, query)
            )
        }
    }

    private fun filterEvents(events: List<EventItem>, category: String, query: String): List<EventItem> {
        return events.filter { item ->
            val matchesCategory = category.equals("Tất cả", ignoreCase = true) || item.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.venue.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
