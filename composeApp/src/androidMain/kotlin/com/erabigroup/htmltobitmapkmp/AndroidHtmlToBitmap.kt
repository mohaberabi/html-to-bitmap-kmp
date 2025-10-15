package com.erabigroup.htmltobitmapkmp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.UUID
import androidx.core.graphics.createBitmap
import kotlin.math.roundToInt

class AndroidHtmlToBitmap(
    private val context: Context,
    private val callbackManager: AndroidPrintCallbackManager,
) : HtmlToImageBitmapConverter {
    companion object {
        private const val MIME_TYPE = "text/html"
        private const val ENCODING = "UTF-8"
        private const val PDF = "pdf"
    }

    override suspend fun convert(params: HtmlToBitmapParams): ImageBitmap? {
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            setBackgroundColor(Color.WHITE)
        }
        val loaded = CompletableDeferred<Unit>()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (!loaded.isCompleted) loaded.complete(Unit)
            }
        }
        webView.loadDataWithBaseURL(null, params.html, MIME_TYPE, ENCODING, null)
        loaded.await()
        val docName = "temp_print"
        val adapter = webView.createPrintDocumentAdapter(docName)
        val attrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution(PDF, PDF, 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        val pdfFile = File(context.cacheDir, "${docName}.${PDF}").apply { delete() }
        val descriptor = ParcelFileDescriptor.open(
            pdfFile,
            ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE or
                    ParcelFileDescriptor.MODE_READ_WRITE
        )
        callbackManager.handle(
            adapter = adapter,
            attrs = attrs,
            descriptor = descriptor,
            timeout = params.timeout
        )
        descriptor.close()
        webView.destroy()
        return createBitmap(pdfFile)
//        return createCombinedBitmaps(inPfd, params.width).asImageBitmap()
    }

    private fun createBitmap(
        pdfFile: File,
    ): ImageBitmap {
        val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)
        val page = renderer.openPage(0)
        val bitmap = createBitmap(page.width, page.height)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        fileDescriptor.close()
        return bitmap.asImageBitmap()
    }


    fun createCombinedBitmaps(
        pdfFile: File,
        targetWidthPx: Int,
        bgColor: Int = Color.WHITE
    ): Bitmap {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        try {
            var totalHeight = 0
            for (i in 0 until renderer.pageCount) {
                totalHeight += renderer.openPage(i).height
            }
            val out = createBitmap(targetWidthPx, totalHeight.coerceAtLeast(1))
            val outCanvas = Canvas(out)
            outCanvas.drawColor(bgColor)
            var yPosition = 0
            for (i in 0 until renderer.pageCount) {
                renderer.openPage(i).use { page ->
                    val pageBmp = createBitmap(targetWidthPx, page.height).also {
                        Canvas(it).drawColor(bgColor)
                    }
                    page.render(pageBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    outCanvas.drawBitmap(pageBmp, 0f, yPosition.toFloat(), null)
                    yPosition += page.height
                    pageBmp.recycle()
                }
            }
            return out
        } finally {
            renderer.close()
            pfd.close()
        }
    }
}
