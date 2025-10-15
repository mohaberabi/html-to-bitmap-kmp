//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.pdf.PdfRenderer
//import android.os.Build
//import android.os.Bundle
//import android.os.CancellationSignal
//import android.os.ParcelFileDescriptor
//import android.print.PageRange
//import android.print.PrintAttributes
//import android.print.PrintDocumentAdapter
//import android.print.PrintDocumentInfo
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import kotlinx.coroutines.*
//import java.io.File
//
///** HTML -> PDF bytes (no system print UI) */
//suspend fun htmlToPdfBytesNoUi(
//    context: Context,
//    html: String,
//    attrs: PrintAttributes = PrintAttributes.Builder()
//        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
//        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
//        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
//        .build()
//): ByteArray = withContext(Dispatchers.Main) {
//    val appCtx = context.applicationContext
//    val webView = WebView(appCtx).apply {
//        settings.javaScriptEnabled = true
//    }
//
//    try {
//        // 1) Load the HTML
//        val loaded = CompletableDeferred<Unit>()
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView?, url: String?) {
//                if (!loaded.isCompleted) loaded.complete(Unit)
//            }
//        }
//        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
//        withTimeout(15_000) { loaded.await() }
//
//        // 2) Create adapter
//        val adapter = webView.createPrintDocumentAdapter("html-job")
//
//        // 3) Prepare output file
//        val outFile = File(appCtx.cacheDir, "html_job_${System.currentTimeMillis()}.pdf")
//        val pfd = ParcelFileDescriptor.open(
//            outFile,
//            ParcelFileDescriptor.MODE_CREATE or
//                    ParcelFileDescriptor.MODE_TRUNCATE or
//                    ParcelFileDescriptor.MODE_READ_WRITE
//        )
//
//        // 4) Drive onLayout with the proper overload
//        val layoutDone = CompletableDeferred<Unit>()
//        val layoutCb = object : PrintDocumentAdapter.LayoutResultCallback() {
//            override fun onLayoutFinished(
//                info: PrintDocumentInfo?,
//                changed: Boolean
//            ) {
//                if (!layoutDone.isCompleted) layoutDone.complete(Unit)
//            }
//
//            override fun onLayoutFailed(error: CharSequence?) {
//                if (!layoutDone.isCompleted) layoutDone.completeExceptionally(
//                    IllegalStateException("Layout failed: $error")
//                )
//            }
//
//            override fun onLayoutCancelled() {
//                if (!layoutDone.isCompleted) layoutDone.completeExceptionally(
//                    CancellationException("Layout cancelled")
//                )
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= 23) {
//            adapter.onLayout(
//                /* oldAttributes */ null,
//                /* newAttributes */ attrs,
//                /* cancellation */ CancellationSignal(),
//                /* callback     */ layoutCb,
//                /* extras       */ Bundle()
//            )
//        } else {
//            @Suppress("DEPRECATION")
//            adapter.onLayout(
//                /* oldAttributes */ null,
//                /* newAttributes */ attrs,
//                /* cancellation */ CancellationSignal(),
//                /* callback     */ layoutCb
//            )
//        }
//        withTimeout(15_000) { layoutDone.await() }
//
//        // 5) Drive onWrite to our file
//        val writeDone = CompletableDeferred<Unit>()
//        adapter.onWrite(
//            arrayOf(PageRange.ALL_PAGES),
//            pfd,
//            CancellationSignal(),
//            object : PrintDocumentAdapter.WriteResultCallback() {
//                override fun onWriteFinished(pages: Array<PageRange>) {
//                    if (!writeDone.isCompleted) writeDone.complete(Unit)
//                }
//
//                override fun onWriteFailed(error: CharSequence?) {
//                    if (!writeDone.isCompleted) writeDone.completeExceptionally(
//                        IllegalStateException("Write failed: $error")
//                    )
//                }
//
//                override fun onWriteCancelled() {
//                    if (!writeDone.isCompleted) writeDone.completeExceptionally(
//                        CancellationException("Write cancelled")
//                    )
//                }
//            }
//        )
//        withTimeout(15_000) { writeDone.await() }
//
//        pfd.close()
//        val bytes = outFile.readBytes()
//        outFile.delete()
//        bytes
//    } finally {
//        runCatching { webView.destroy() }
//    }
//}
//
///** Optional: PDF bytes -> first page Bitmap (useful for Epson) */
//suspend fun pdfFirstPageToBitmap(context: Context, pdfBytes: ByteArray): Bitmap =
//    withContext(Dispatchers.IO) {
//        val tmp = File(context.cacheDir, "render_${System.currentTimeMillis()}.pdf")
//        tmp.writeBytes(pdfBytes)
//        val pfd = ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY)
//        val renderer = PdfRenderer(pfd)
//        val page = renderer.openPage(0)
//        val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
//        page.close(); renderer.close(); pfd.close()
//        tmp.delete()
//        bmp
//    }
//
