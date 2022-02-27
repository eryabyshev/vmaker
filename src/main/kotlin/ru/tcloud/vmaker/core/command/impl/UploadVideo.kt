package ru.tcloud.vmaker.core.command.impl

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import ru.tcloud.vmaker.core.MediaType
import ru.tcloud.vmaker.core.command.Command
import ru.tcloud.vmaker.core.command.CommandType
import ru.tcloud.vmaker.core.command.impl.UploadVideo.Arguments.*
import ru.tcloud.vmaker.core.exception.VMakerException
import ru.tcloud.vmaker.core.source.MediaSource
import ru.tcloud.vmaker.core.source.Source
import ru.tcloud.vmaker.core.support.CircleCollection
import java.util.*

//uv -p /Users/res1709/Documents/my/vmaker/src/test/data/tst -t sun,water -c 3

@Component
class UploadVideo(sources: List<Source>): Command {

    private val sourcesRouter = sources.associateBy { it.getSourceType() }

    private enum class Arguments(val arg: String, val desc: String) {
        P("p", "path to project"),
        T("t", "list of search tags, use ',' for set several tags"),
        S("s", "source for search by default: $defaultSrc"),
        C("c", "count of downloaded video, by default $defaultVideoCount"),
        HELP("help", """"
            |             ${P.arg} - ${P.desc}
            |             ${T.arg} - ${T.desc}
            |             ${S.arg} - ${S.desc}
            |             
            |             """
            .trimMargin())
    }


    override fun run(args: Map<String, String>) {
        if (args.size == 1 && args.containsKey(HELP.arg)) {
            println(HELP.desc)
            return
        }
        val path = (args[P.arg] ?: throw VMakerException("Set -${P.arg} argument for ${P.desc}")).trim() + "/$videoDirName"
        val tags = (args[T.arg] ?: throw VMakerException("Set -${T.arg} argument for ${T.desc}"))
            .split(",").onEach { it.trim() }
        val src = (args[S.arg] ?: defaultSrc)
            .split(",")
            .map { MediaSource.valueOf(it.trim().uppercase(Locale.getDefault())) }
            .let { CircleCollection(it) }
        val counter = (args[C.arg] ?: defaultVideoCount.toString()).trim().toInt()

        runBlocking {
            repeat(counter) {
                val next = src.next()
                sourcesRouter[next]?.findVideo(path, *tags.toTypedArray())
                    .apply { println("In source ${next.name} was not found proper video") }
            }
        }
    }

    override fun getType() = CommandType.UPLOAD_VIDEO

    companion object {
        private val defaultSrc = listOf(MediaSource.PIXABAY, MediaSource.PEXELS).joinToString(",") { it.name }
        private val videoDirName = MediaType.VIDEO.name.lowercase(Locale.getDefault())
        private const val defaultVideoCount = 10
    }
}