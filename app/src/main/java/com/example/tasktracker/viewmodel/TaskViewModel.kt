package com.example.tasktracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.db.TaskDao
import com.example.tasktracker.db.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks

    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    fun insertTask(task: TaskEntity) = viewModelScope.launch {
        dao.insertTask(task)
        _tasks.value = dao.getAllTasks()
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        dao.deleteTask(task)
        _tasks.value = dao.getAllTasks()
    }
}