package com.erabigroup.htmltobitmapkmp

import androidx.compose.ui.graphics.ImageBitmap


interface HtmlToImageBitmapConverter {
    suspend fun convert(
        params: HtmlToBitmapParams
    ): ImageBitmap?
}