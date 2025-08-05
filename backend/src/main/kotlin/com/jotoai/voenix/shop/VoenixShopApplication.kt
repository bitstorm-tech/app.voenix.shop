package com.jotoai.voenix.shop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulithic

@SpringBootApplication
@Modulithic(systemName = "Voenix Shop")
class VoenixShopApplication

fun main(args: Array<String>) {
    runApplication<VoenixShopApplication>(*args)
}
