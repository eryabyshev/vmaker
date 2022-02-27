package ru.tcloud.vmaker.core.command.impl

import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.command.Command
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.command.impl.PrepareStructureCommand.Arguments.*
import ru.tcloud.vmaker.core.exception.VMakerException
import java.nio.file.Paths
import java.util.*

//pd -n tst -p /Users/res1709/Documents/my/vmaker/src/test/data

@Component
class PrepareStructureCommand : Command {

    private enum class Arguments(val arg: String, val desc: String) {
        N("n", "set name for create new folder structure"),
        P("p", "create folder structure in target place, by default $defaultPath"),
        HELP(
            "help", """"
            |             ${P.arg} - ${P.desc}
            |             """
                .trimMargin()
        )
    }

    override fun getType() = CommandType.PREPARE_DIR

    override fun run(args: Map<String, String>) {
        if (args.size == 1 && args.containsKey(HELP.arg)) {
            println(HELP.desc)
            return
        }
        val name =
            (args[N.arg] ?: throw VMakerException("You need set -${N.arg} flag, use -${HELP.arg} for more info")).trim()
        val targetPath = (args[P.arg] ?: defaultPath).trim()
        val parent = Paths.get(targetPath, name).toFile()
            .apply {
                this.mkdirs()
                if (!this.exists()) {
                    throw VMakerException("Can't create dir ${this.absolutePath}")
                }
                println("${this.absolutePath} was created")
            }

        MediaType.values().forEach {
            val dirStructureName = it.name.lowercase(Locale.getDefault())
            val file = Paths.get(parent.absolutePath, dirStructureName).toFile()
            file.mkdir()
            if (!file.exists()) {
                throw VMakerException("Can't create dir ${file.absolutePath}")
            }
            println("$dirStructureName was created in ${parent.absolutePath}")
        }
    }

    companion object {
        const val defaultPath = "/tmp"
    }
}