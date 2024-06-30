package com.example.fileservice.controller.exception

class InternalException(
    override val message: String, cause: Throwable
) : RuntimeException(message, cause)
