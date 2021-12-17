package ru.tcloud.vmaker.core.source.pixels.api.response

import com.fasterxml.jackson.annotation.JsonProperty


data class VideoFiles (
	val id : Long,
	val quality : String,
	@JsonProperty("file_type")
	val fileType : String,
	val width : Int,
	val height : Int,
	val link : String
)