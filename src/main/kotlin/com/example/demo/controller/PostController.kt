package com.example.demo.controller

import com.example.demo.config.TaskServiceName
import com.example.demo.data.entity.Post
import com.example.demo.service.PostService
import com.example.demo.service.TaskService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(
    private val postService: PostService,
    private val taskService: TaskService
) {

    @GetMapping(path = ["/posts"])
    fun list(): List<Post> {
        return postService.list()
    }

    @PostMapping(path = ["/post"])
    fun create(): Post {
        return postService.create()
    }

    @PostMapping(path = ["/post/{postId}/like"])
    fun likePost(
        @PathVariable postId: Long
    ) {
        return postService.likePostWithPessimisticLock(postId)
    }

    @PostMapping(path = ["/post/{postId}/like/enqueue"])
    fun enqueueLikePost(
        @PathVariable postId: Long
    ): Long {
        val taskId = taskService.addTask(
            taskServiceName = TaskServiceName.LIKE_POST, args = hashMapOf(
                "postId" to postId
            )
        )

        Thread.sleep(500)

        // 큐에 등록되었는지 확인
        taskService.getTaskStatus(taskId)

        return taskId
    }
}