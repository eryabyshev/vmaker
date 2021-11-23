package ru.tcloud.vmaker.core.editor

import java.io.File

interface Editor {
    fun getDuration(file: File): Int
    fun makeFadeVideo(name: String, file: File, duration: Int): File
    fun encodeVideo(name: String, file: File): File
    fun concatenationMp4(workDir: File, list: List<File>): File
    fun doSilent(workDir: File, file: File, posfix: String = ""): File
    fun concatenationMp3(workDir: File, list: List<File>): File
    fun addAudioOnVideo(audio: File, video: File, resultDir: File): File
    fun imageToVideo(file: File, name: String, duration: Int): File
    fun jpegToPng(file: File): File
    fun encodeVideoFromImage(name: String, file: File): File
}