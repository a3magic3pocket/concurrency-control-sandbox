package com.example.demo.service

import com.example.demo.config.TaskConfig
import com.example.demo.config.TaskServiceName
import com.example.demo.config.TaskStatus
import com.example.demo.dto.Task
import com.example.demo.util.JsonUtil
import org.redisson.api.RList
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class TaskService(
    private val redissonClient: RedissonClient,
    private val postService: PostService,
) {

    fun getTaskQueue(): RList<Task> {
        return redissonClient.getList(TaskConfig.TASK_QUEUE_KEY)
    }

    fun hasTasks(): Boolean {
        val queue = getTaskQueue()
        return queue.size > 0
    }

    fun getTaskStatus(taskId: Long): TaskStatus {
        val key = "${TaskConfig.TASK_STATUS_PREFIX}${taskId}"
        val bucket = redissonClient.getBucket<String>(key)
        val status = bucket.get()
        return try {
            TaskStatus.valueOf(status)
        } catch (e: Exception) {
            throw RuntimeException("잘못된 taskStatus, taskId:${taskId}")
        }
    }

    private fun setTaskStatus(taskId: Long, taskStatus: TaskStatus) {
        val statusBucket = redissonClient.getBucket<String>("${TaskConfig.TASK_STATUS_PREFIX}${taskId}")
        statusBucket.set(taskStatus.toString(), Duration.ofSeconds(TaskConfig.TASK_TTL))
    }

    fun addTask(taskServiceName: TaskServiceName, args: HashMap<String, Any>): Long {
        // Redis 에서 ID 생성
        val taskId = redissonClient.getAtomicLong(TaskConfig.TASK_ID_KEY).incrementAndGet()

        val jsonString = JsonUtil.mapToJson(args)
        val task = Task(id = taskId, serviceName = taskServiceName, args = jsonString)

        val queue = getTaskQueue()
        queue.add(task)

        // 상태 초기화
        setTaskStatus(taskId, TaskStatus.INIT)

        return taskId
    }

    @Transactional(rollbackFor = [Exception::class])
    fun processQueue() {
        val batchSize = 20

        val queue = getTaskQueue()
        val tasksToProcess = queue.take(batchSize)

        for (task in tasksToProcess) {
            if (task.serviceName == TaskServiceName.LIKE_POST) {
                val args = JsonUtil.jsonToMap(task.args)
                val parsedPostId = args["postId"]
                val postId: Long = when {
                    (parsedPostId is String) -> {
                        parsedPostId.toLongOrNull() ?: throw RuntimeException("postId is not long")
                    }

                    (parsedPostId is Number) -> {
                        parsedPostId.toLong()
                    }

                    else -> throw throw RuntimeException("wrong postId")
                }

                postService.likePostWithoutLock(postId)
                setTaskStatus(task.id, TaskStatus.SUCCESS)
            } else {
                throw RuntimeException("unknown service error")
            }
            queue.remove(task)
        }

    }
}