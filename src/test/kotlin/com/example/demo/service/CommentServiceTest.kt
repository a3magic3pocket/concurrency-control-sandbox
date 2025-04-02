package com.example.demo.service

import com.example.demo.data.entity.Comment
import com.example.demo.data.repository.CommentRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.test.assertEquals

@SpringBootTest
class CommentServiceTest(
    @Autowired val commentService: CommentService,
    @Autowired val commentRepository: CommentRepository,
) {
    @Test
    fun testConcurrentIncrementWithOptimisticLock() {
        // 초기 데이터 설정
        val comment = Comment(numLikes = 10)
        val savedComment = commentRepository.save(comment)

        // 스레드 풀 생성
        val executor = Executors.newFixedThreadPool(2)
        val tasks = listOf(
            Callable {
                try {
                    commentService.likeCommentWithOptimisticLock(savedComment.id)
                    "success"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "failed"
                }
            },
            Callable {
                try {
                    commentService.likeCommentWithOptimisticLock(savedComment.id)
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
        futures.forEach { it.get() } // get() 메서드를 호출하여 결과를 기다림

        // 스레드 풀 종료
        executor.shutdown()

        // 결과 확인
        val updatedPost = commentRepository.findById(savedComment.id).orElseThrow { RuntimeException("comment not found") }
        println("Final numLikes: ${updatedPost.numLikes}")

        assertEquals(12, updatedPost.numLikes, "두 트랜잭션이 모두 동작하므로 POST.numLikes 는 10에서 2가 추가된 12이 됩니다")
    }
}