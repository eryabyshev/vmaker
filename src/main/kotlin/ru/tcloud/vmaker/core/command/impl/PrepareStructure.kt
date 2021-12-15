package ru.tcloud.vmaker.core.command.impl

import org.springframework.stereotype.Service
import ru.tcloud.vmaker.core.DirStructure
import ru.tcloud.vmaker.core.command.Command
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.command.impl.PrepareStructure.Arguments.*
import ru.tcloud.vmaker.core.exception.VMakerException
import java.nio.file.Paths
import java.util.*

@Service
class PrepareStructure: Command {

    private enum class Arguments(val arg: String, val desc: String) {
        N("n", "set name for create new folder structure"),
        P("p", "create folder structure in target place, by default $defaultPath"),
        HELP("help", """"
            |             ${P.arg} - ${P.desc}
            |             """
            .trimMargin())
    }


    override fun getType() = CommandType.PREPARE_DIR

    override fun run(args: Map<String, String>) {
        val name = args[N.arg] ?: throw VMakerException("You need set -${N.arg} flag, use -${HELP.arg} for more info")
        val targetPath = args[P.arg] ?: defaultPath
        val parent = Paths.get(targetPath, name).toFile()
            .apply {
                this.mkdirs()
                println("${this.absolutePath} was created")
            }

        DirStructure.values().forEach {
            val dirStructureName = it.name.lowercase(Locale.getDefault())
            Paths.get(parent.absolutePath, dirStructureName).toFile().mkdir()
            println("$dirStructureName was created in ${parent.absolutePath}")
        }
    }

    companion object {
        const val defaultPath = "/tmp"
    }
}