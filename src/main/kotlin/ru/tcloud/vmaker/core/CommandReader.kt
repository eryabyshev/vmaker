package ru.tcloud.vmaker.core

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.exception.VMakerException
import java.util.*

@Component
class CommandReader(private val commandParser: CommandParser) {

    private val input = Scanner(System.`in`)

    fun run() {
        while (true) {
            try {
                print("> ")
                input.nextLine().apply { commandParser.parseAndRun(this) }
                println()
            }catch(e: Exception) {
                println(e.message)
            }finally {
                run()
            }
        }
    }

}