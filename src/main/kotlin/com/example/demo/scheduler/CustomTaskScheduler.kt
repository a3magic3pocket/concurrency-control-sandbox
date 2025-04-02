package com.example.demo.scheduler

import com.example.demo.service.TaskService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CustomTaskScheduler(
    private val taskService: TaskService
) {

    @Scheduled(fixedDelay = 100)
    fun scheduleTask() {
        if (taskService.hasTasks()) {
            taskService.processQueue()
        }
    }
}
