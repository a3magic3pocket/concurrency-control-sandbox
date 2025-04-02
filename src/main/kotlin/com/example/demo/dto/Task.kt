package com.example.demo.dto

import com.example.demo.config.TaskServiceName

data class Task(
    val id: Long,
    val serviceName: TaskServiceName,
    val args: String,
)