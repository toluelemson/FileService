package com.example.fileservice.controller.exception

import com.example.fileservice.library.log.Logger
import com.example.fileservice.rest.ErrorMessage
import com.example.fileservice.library.log.LogItem
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.validation.ConstraintViolationException
import java.io.IOException

@RestControllerAdvice
class GlobalExceptionHandler(private val logger: Logger) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorMessage> {
        val errors = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ErrorMessage(errors, "VALIDATION_ERROR"))
    }


    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorMessage> {
        val errors = ex.constraintViolations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
        return ResponseEntity.badRequest().body(ErrorMessage(errors, "CONSTRAINT_VIOLATION"))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupportedException(): ResponseEntity<ErrorMessage> {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ErrorMessage("Method not allowed", "METHOD_NOT_ALLOWED"))
    }

    @ExceptionHandler(IOException::class)
    fun handleIOException(ex: IOException): ResponseEntity<ErrorMessage> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorMessage("Failed to upload file: ${ex.message}", "IO_EXCEPTION"))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleResourceNotFoundException(ex: NotFoundException): ResponseEntity<ErrorMessage> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorMessage(ex.message ?: "Resource not found", "NOT_FOUND"))
    }

    @ExceptionHandler(IllegalArgumentException::class, InternalException::class)
    fun handleBadRequestExceptions(ex: Exception): ResponseEntity<ErrorMessage> {
        val status = if (ex is IllegalArgumentException) HttpStatus.BAD_REQUEST else HttpStatus.SERVICE_UNAVAILABLE
        val code = if (ex is IllegalArgumentException) "BAD_REQUEST" else "INTERNAL_SERVER_ERROR"

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            val logItem = LogItem("Critical exception occurred: ${ex.message}")
            logger.crit(logItem)
        }

        return ResponseEntity.status(status).body(ErrorMessage(ex.message ?: "Unexpected error", code))
    }
}
