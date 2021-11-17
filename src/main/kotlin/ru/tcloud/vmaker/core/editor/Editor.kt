package ru.tcloud.vmaker.core.editor

import java.io.File

interface Editor {
    fun getDuration(file: File): Int
    fun makeFadeVideo(counter: Int, file: File, duration: Int): File
    fun encodeVideo(counter: Int, file: File): File
    fun concatenationMp4(workDir: File, list: List<File>): File
    fun doSilent(workDir: File, file: File, posfix: String = ""): File
    fun concatenationMp3(workDir: File, list: List<File>, tmpFiles: MutableSet<File>): File
    fun addAudioOnVideo(audio: File, video: File, resultDir: File): File
}