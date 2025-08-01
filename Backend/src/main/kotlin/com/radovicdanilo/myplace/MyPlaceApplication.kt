package com.radovicdanilo.myplace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyPlaceApplication

fun main(args: Array<String>) {
    runApplication<MyPlaceApplication>(*args)
}
