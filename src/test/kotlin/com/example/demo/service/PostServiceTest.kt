package com.example.demo.service

import com.example.demo.data.entity.Post
import com.example.demo.data.repository.PostRepository
import jakarta.persistence.EntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.test.assertEquals

@SpringBootTest
class PostServiceTest(
    @Autowired val entityManager: EntityManager,
    @Autowired val postService: PostService,
    @Autowired val postRepository: PostRepository
) {

    @Test
    fun testConcurrentLikeWithoutLock() {
        // 초기 데이터 설정
        val post = Post(numLikes = 10)
        val savedPost = postRepository.save(post)

        // 스레드 풀 생성
        val executor = Executors.newFixedThreadPool(2)
        val tasks = listOf(
            Callable {
                try {
                    postService.likePostWithoutLock(savedPost.id)
                    "success"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "failed"
                }
            },
            Callable {
                try {
                    postService.likePostWithoutLock(savedPost.id)
                    "success"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "failed"
                }
            }
        )

        // Future 리스트에 작업 제출
        val futures: List<Future<*>> = tasks.map { executor.submit(it) }

        // 모든 작업 완료 대기
        val results = futures.map { it.get() } // get() 메서드를 호출하여 결과를 기다림

        // 스레드 풀 종료
        executor.shutdown()

        // 결과 확인
        val updatedPost = postRepository.findById(savedPost.id).orElseThrow { RuntimeException("post not found") }
        println("Final numLikes: ${updatedPost.numLikes}")

        assertEquals(11, updatedPost.numLikes, "두 트랜잭션 중 하나의 트랜잭션 결과가 덮어쓰기 되므로 POST.numLikes 는 10에서 1이 추가된 11이 됩니다")

        assert(results.count { it == "success" } == 2)
    }

    @Test
    fun testConcurrentIncrementWithPessimisticLock() {
        // 초기 데이터 설정
        val post = Post(numLikes = 10)
        val savedPost = postRepository.save(post)

        entityManager.clear()

        // 스레드 풀 생성
        val executor = Executors.newFixedThreadPool(2)
        val tasks = listOf(
            Callable {
                try {
                    postService.likePostWithPessimisticLock(savedPost.id)
                    "success"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "failed"
                }
            },
            Callable {
                try {
                    postService.likePostWithPessimisticLock(savedPost.id)
                    "success"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "failed"
                }
            }
        )

        // Future 리스트에 작업 제출
        val futures: List<Future<*>> = tasks.map { executor.submit(it) }

        // 모든 작업 완료 대기
        val results = futures.map { it.get() } // get() 메서드를 호출하여 결과를 기다림

        // 스레드 풀 종료
        executor.shutdown()

        // 결과 확인
        val updatedPost = postRepository.findById(savedPost.id).orElseThrow { RuntimeException("post not found") }
        println("Final numLikes: ${updatedPost.numLikes}")

        assertEquals(12, updatedPost.numLikes, "두 트랜잭션이 모두 동작하므로 POST.numLikes 는 10에서 2가 추가된 12이 됩니다")

        assert(results.all { it == "success" })
    }
}