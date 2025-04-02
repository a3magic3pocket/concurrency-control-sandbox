package com.example.demo.service

import com.example.demo.config.TaskConfig
import com.example.demo.config.TaskServiceName
import com.example.demo.config.TaskStatus
import com.example.demo.dto.Task
import com.example.demo.util.JsonUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.assertEquals

@SpringBootTest
@DirtiesContext
class TaskServiceTest(
    @Autowired private val redissonClient: RedissonClient,
    @Autowired private val postService: PostService,
    @Autowired private val taskService: TaskService,
) {

    @Test
    fun testRedissonConnection() {
        val key = "testKey17236172371673991"
        val testPhrase = "Hello, Redisson!"

        val bucket = redissonClient.getBucket<String>(key)
        bucket.set(testPhrase)

        val value = bucket.get()
        assertEquals(testPhrase, value)

        bucket.delete()
    }

    @AfterEach
    fun tearDown() {
        // Redis 에서 키 삭제
        redissonClient.getList<Task>(TaskConfig.TASK_QUEUE_KEY).clear() // taskQueue 비우기
        redissonClient.getAtomicLong(TaskConfig.TASK_ID_KEY).set(0) // taskId 초기화
        for (key in redissonClient.keys.getKeys()) {
            if (key.startsWith(TaskConfig.TASK_STATUS_PREFIX)) {
                redissonClient.getBucket<String>(key).delete()
            }
        }
    }

    @Test
    fun `getTaskStatus should return SUCCESS when status is SUCCESS`() {
        // Given
        val taskId = 1
        val statusBucket = redissonClient.getBucket<String>("${TaskConfig.TASK_STATUS_PREFIX}${taskId}")
        val expectedStatus = TaskStatus.INIT

        statusBucket.set(expectedStatus.toString())

        // When
        val outputStatus = taskService.getTaskStatus(1)

        // Then
        assertEquals(outputStatus, expectedStatus)
    }

    @Test
    fun testAddTask() {
        // Given
        val taskServiceName = TaskServiceName.LIKE_POST
        val args: HashMap<String, Any> = hashMapOf(
            "key1" to "value1",
            "key1" to 2,
        )

        // When
        taskService.addTask(taskServiceName, args)

        // Then
        val queue = taskService.getTaskQueue()
        assertEquals(1, queue.size)

        val task = queue[0]
        assertEquals(taskServiceName, task.serviceName)
        assertEquals(JsonUtil.mapToJson(args), task.args)
        assertEquals(args, JsonUtil.jsonToMap(task.args))

        val status = taskService.getTaskStatus(task.id)
        assertEquals(status, TaskStatus.INIT)
    }

    @Test
    fun testProcessQueue() {
        // Given
        val beforePosts = postService.list()
        val newPost = postService.create()
        val expectedNumLikes = 10
        val taskIdList: MutableList<Long> = mutableListOf()

        for (i in 0 until expectedNumLikes) {
            val taskId = taskService.addTask(
                taskServiceName = TaskServiceName.LIKE_POST, args = hashMapOf(
                    "postId" to newPost.id
                )
            )
            taskIdList.add(taskId)
        }

        // When
        // 스케쥴러가 작업할 수 있도록 5초간 대기
        Thread.sleep(5000)

        // Then
        val afterPosts = postService.list()
        val afterPost = postService.retrieve(newPost.id)

        assertEquals(beforePosts.size + 1, afterPosts.size, "새로운 post 이 생성되지 않았습니다")
        assertEquals(afterPost.numLikes, expectedNumLikes, "새 post.quantity 가 기대값과 다릅니다")

        for (taskId in taskIdList) {
            val afterStatus = taskService.getTaskStatus(taskId)
            assertEquals(afterStatus, TaskStatus.SUCCESS, "$taskId task, 처리 실패")
        }
    }
}