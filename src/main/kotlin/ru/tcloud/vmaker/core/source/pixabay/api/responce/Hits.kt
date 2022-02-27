package ru.tcloud.vmaker.core.source.pixabay.api.responce

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Hits @JsonCreator constructor (

	val id : Long,
	val pageURL : String,
	val type : String,
	val tags : String,
	val duration : Int,
	@JsonProperty("picture_id")
	val pictureId : String,
	val videos : Videos,
	val views : Int,
	val downloads : Int,
	val likes : Int,
	val comments : Int,
	@JsonProperty("user_id")
	val userId : Int,
	val user : String,
	val userImageURL : String
)