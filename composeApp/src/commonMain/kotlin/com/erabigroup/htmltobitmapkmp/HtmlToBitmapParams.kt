package com.erabigroup.htmltobitmapkmp

const val DEFAULT_TIME_OUT = 10_000L

data class HtmlToBitmapParams(
    val width: Int,
    val html: String,
    val timeout: Long = DEFAULT_TIME_OUT
)
