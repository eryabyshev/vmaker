package ru.tcloud.vmaker.core

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.command.CommandRouter
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.command.opToType
import ru.tcloud.vmaker.core.exception.VMakerException

@Component
class CommandParser(private val commandRouter: CommandRouter) {

    fun parseAndRun(str: String) {
        val commandType = getCommand(str)
        val args = getArgs(str)
        commandRouter.work(commandType, args)
    }

    fun getArgs(str: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        str.split("-").drop(1)
            .forEach {
                val split = it.split(" ")
                result[split[0]] = split.drop(1).joinToString(" ")
            }
        return result.onEach {
            it.value.trim()
        }
    }

    private fun getCommand(str: String): CommandType {
        return str.split(" ").let {
            if (it.isEmpty() || it.size == 1) {
                throw VMakerException("Command can't be empty")
            }
            opToType[it[0]]?: throw VMakerException("Unknown command ${it[0]}")
        }
    }
}