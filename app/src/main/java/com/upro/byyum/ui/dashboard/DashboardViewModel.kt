package com.upro.byyum.ui.dashboard

import androidx.lifecycle.*
import com.upro.byyum.data.MonitorRepository

class DashboardViewModel(private val repository: MonitorRepository) : ViewModel() {
    val monitors = repository.allMonitors
    val healthyCount = repository.healthyCount
    val downCount = repository.downCount
    val errorCount = repository.errorCount
    val totalCount = repository.totalCount
}

class DashboardViewModelFactory(private val repository: MonitorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
