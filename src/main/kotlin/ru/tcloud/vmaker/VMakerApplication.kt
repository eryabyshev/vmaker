package ru.tcloud.vmaker

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.tcloud.vmaker.core.CommandReader

@SpringBootApplication
class VMakerApplication(private val commandReader: CommandReader): CommandLineRunner {

	override fun run(vararg args: String?) {
		commandReader.run()
	}

}

fun main(args: Array<String>) {
	runApplication<VMakerApplication>(*args)
}
