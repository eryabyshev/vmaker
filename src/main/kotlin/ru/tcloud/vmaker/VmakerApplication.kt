package ru.tcloud.vmaker

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VmakerApplication: CommandLineRunner {

	override fun run(vararg args: String?) {
		TODO("Not yet implemented")
	}

}

fun main(args: Array<String>) {
	runApplication<VmakerApplication>(*args)
}
