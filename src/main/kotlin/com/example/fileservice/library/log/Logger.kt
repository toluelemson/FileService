package com.example.fileservice.library.log

import org.springframework.stereotype.Component

@Component
class Logger {

    fun crit(logItem: LogItem) {
        write("crit", logItem)
    }

    private fun write(logLevel: String, logItem: LogItem) {
        println("$logLevel: $logItem")
    }
}
