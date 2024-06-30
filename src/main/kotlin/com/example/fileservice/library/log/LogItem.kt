package com.example.fileservice.library.log

import java.time.LocalDateTime

open class LogItem constructor(
    val message: String
) {
    private val dateTime: LocalDateTime = LocalDateTime.now()

    private var correlationId: String? = null
    var type: String? = null

    override fun toString(): String {
        return "[$dateTime] [$correlationId] $message"
    }
}
