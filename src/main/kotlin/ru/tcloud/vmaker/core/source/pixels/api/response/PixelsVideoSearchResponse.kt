package ru.tcloud.vmaker.core.source.pixels.api.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PixelsVideoSearchResponse (
	val page : Int,
	@JsonProperty("per_page")
	val perPage : Int,
	val videos : List<Videos>,
	@JsonProperty("total_results")
	val totalResults : Int,
	@JsonProperty("next_page")
	val nextPage : String,
	val url : String
)