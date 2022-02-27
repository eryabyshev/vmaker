package ru.tcloud.vmaker.core.source.pixabay.api.responce

data class PixabayVideoSearchResponse (
	val total : Int,
	val totalHits : Int,
	val hits : List<Hits>
)