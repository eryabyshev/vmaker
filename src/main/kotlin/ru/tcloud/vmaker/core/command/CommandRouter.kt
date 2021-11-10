package ru.tcloud.vmaker.core.command

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.exception.VMakerException

@Component
class CommandRouter(all: List<Command>) {
    private val mapper: Map<CommandType, Command> = all.associateBy { it.getType() }

    fun work(type: CommandType, args: Map<String, String>){
        val command = mapper[type] ?: throw VMakerException("Unknown command ${type}")
        command.run(args)
    }
}