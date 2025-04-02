package com.example.demo.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object JsonUtil {
    private val objectMapper = jacksonObjectMapper()

    fun mapToJson(map: HashMap<String, Any>): String {
        return objectMapper.writeValueAsString(map)
    }

    fun jsonToMap(json: String): HashMap<String, Any> {
        return objectMapper.readValue(json)
    }
}