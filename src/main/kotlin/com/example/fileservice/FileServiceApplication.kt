package com.example.fileservice

import jakarta.annotation.PostConstruct
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.util.TimeZone

@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.example.fileservice.data.repository"])
open class FileServiceApplication : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(FileServiceApplication::class.java)
    }

    @PostConstruct
    fun setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FileServiceApplication::class.java, *args)
        }
    }
}
