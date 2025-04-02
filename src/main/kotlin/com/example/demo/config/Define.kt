package com.example.demo.config

object TaskConfig {
    const val TASK_ID_KEY = "taskId"
    const val TASK_QUEUE_KEY = "taskQueue"
    const val TASK_STATUS_PREFIX = "taskStatus:"
    const val TASK_LOCK_KEY = "taskLock"
    const val TASK_TTL = 300L // 5min
}

enum class TaskServiceName {
    LIKE_POST
}

enum class TaskStatus {
    INIT,
    SUCCESS,
}