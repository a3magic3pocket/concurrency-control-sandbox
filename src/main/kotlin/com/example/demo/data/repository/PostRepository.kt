package com.example.demo.data.repository

import com.example.demo.data.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long>
