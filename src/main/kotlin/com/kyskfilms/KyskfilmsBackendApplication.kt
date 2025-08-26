package com.kyskfilms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class KyskFilmsApplication

fun main(args: Array<String>) {
    runApplication<KyskFilmsApplication>(*args)
}