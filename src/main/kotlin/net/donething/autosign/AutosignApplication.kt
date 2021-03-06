package net.donething.autosign

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AutosignApplication

fun main(args: Array<String>) {
    SpringApplication.run(AutosignApplication::class.java, *args)
}
