package com.example.demo.service

import com.example.demo.util.JsonUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JsonUtilTest {

    @Test
    fun testMapToJson() {
        // Given
        val map: HashMap<String, Any> = hashMapOf(
            "key1" to "value1",
            "key2" to 2,
            "key3" to listOf("item1", "item2")
        )

        // When
        val jsonString = JsonUtil.mapToJson(map)

        // Then
        val expectedJson = """{"key1":"value1","key2":2,"key3":["item1","item2"]}"""
        assertEquals(expectedJson, jsonString)
    }

    @Test
    fun testJsonToMap() {
        // Given
        val jsonString = """{"key1":"value1","key2":2,"key3":["item1","item2"]}"""

        // When
        val map = JsonUtil.jsonToMap(jsonString)

        // Then
        val expectedMap: HashMap<String, Any> = hashMapOf(
            "key1" to "value1",
            "key2" to 2,
            "key3" to listOf("item1", "item2")
        )
        assertEquals(expectedMap, map)
    }
}