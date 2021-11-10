package ru.tcloud.vmaker.core.command

enum class CommandType(val op: String) {
    CREATE_FROM_DIR("cfd")
}

val opToType = hashMapOf("cfd" to CommandType.CREATE_FROM_DIR)