package com.example.demo.controller

import com.example.demo.data.entity.Comment
import com.example.demo.service.CommentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(private val commentService: CommentService) {

    @GetMapping(path = ["/comments"])
    fun list(): List<Comment> {
        return commentService.list()
    }

    @PostMapping(path = ["/comment"])
    fun create(): Comment {
        return commentService.create()
    }

    @PostMapping(path = ["/comment/{commentId}/like"])
    fun likeComment(
        @PathVariable commentId: Long
    ) {
        return commentService.likeCommentWithOptimisticLock(commentId)
    }
}