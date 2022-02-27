package ru.tcloud.vmaker.core.source.pixels.api.response

import com.fasterxml.jackson.annotation.JsonProperty

data class Videos (
	val id : Int,
	val width : Int,
	val height : Int,
	val duration : Int,
	@JsonProperty("full_res")
	val fullRes : String?,
	val tags : List<String>,
	val url : String,
	val image : String,
	@JsonProperty("avg_color")
	val avgColor : String?,
	val user : User,
	@JsonProperty("video_files")
	val videoFiles : List<VideoFiles>,
	@JsonProperty("video_pictures")
	val videoPictures : List<VideoPictures>
)