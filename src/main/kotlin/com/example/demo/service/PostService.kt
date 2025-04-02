package com.example.demo.service

import com.example.demo.data.entity.Post
import com.example.demo.data.repository.PostRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
    private val entityManager: EntityManager,
) {
    fun retrieve(postId: Long): Post {
        return postRepository.findById(postId).orElseThrow { RuntimeException("없는 post 입니다. postId: $postId") }
    }

    fun list(): List<Post> {
        return postRepository.findAll()
    }

    fun create(): Post {
        return postRepository.save(Post())
    }

    @Transactional(
        isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, timeout = 5,
        rollbackFor = [Exception::class]
    )
    fun likePostWithoutLock(postId: Long) {
        likePostWithoutTransaction(postId)
    }

    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED,
        timeout = 5,
        rollbackFor = [Exception::class]
    )
    fun likePostWithPessimisticLock(postId: Long) {
        val post = entityManager.find(Post::class.java, postId, LockModeType.PESSIMISTIC_WRITE)

        post.numLikes += 1

        postRepository.save(post)
    }

    fun likePostWithoutTransaction(postId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { RuntimeException("post not found") }

        post.numLikes += 1
        postRepository.save(post)
    }
}