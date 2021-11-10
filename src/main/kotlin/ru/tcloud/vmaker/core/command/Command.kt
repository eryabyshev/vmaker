package ru.tcloud.vmaker.core.command

interface Command {

    fun getType(): CommandType

    fun run(args: Map<String, String>)
}