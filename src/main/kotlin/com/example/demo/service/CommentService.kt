package com.example.demo.service

import com.example.demo.data.entity.Comment
import com.example.demo.data.repository.CommentRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
) {
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED,
        timeout = 5,
        rollbackFor = [Exception::class]
    )
    fun list(): List<Comment> {
        return commentRepository.findAll()
    }

    fun create(): Comment {
        return commentRepository.save(Comment())
    }

    @Retryable(value = [OptimisticLockingFailureException::class], maxAttempts = 30, backoff = Backoff(delay = 100))
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED,
        timeout = 20,
        rollbackFor = [Exception::class]
    )
    fun likeCommentWithOptimisticLock(commentId: Long) {
        val comment =
            commentRepository.findById(commentId)
                .orElseThrow { IllegalArgumentException("comment not found") }
        comment.numLikes += 1
        commentRepository.save(comment)
    }
}