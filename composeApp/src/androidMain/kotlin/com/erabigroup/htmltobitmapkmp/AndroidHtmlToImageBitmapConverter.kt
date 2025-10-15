//package com.erabigroup.htmltobitmapkmp
//
//import android.app.Activity
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewTreeObserver
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import android.widget.FrameLayout
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.asImageBitmap
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.withTimeout
//import kotlin.math.max
//import androidx.core.graphics.createBitmap
//
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import android.os.SystemClock
//import android.view.Choreographer
//import android.webkit.WebChromeClient
//import androidx.compose.runtime.snapshotFlow
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.withTimeoutOrNull
//import kotlin.coroutines.resume
//
//
//class AndroidHtmlToImageBitmapConverter(
//    private val context: Context,
//) : HtmlToImageBitmapConverter {
//    override suspend fun convert(
//        params: HtmlToBitmapParams,
//    ): ImageBitmap? {
//
//        return htmlToBitmapContextOnly3(
//            context = context,
//            html = params.html,
//            widthPx = params.width,
//        ).asImageBitmap()
//    }
//}
//
//
//suspend fun htmlToBitmapContextCoroutine(
//    context: Context,
//    html: String,
//    widthPx: Int,
//    measureDelayMs: Long = 50L,
//    timeoutMs: Long = 15_000L,
//): Bitmap = withContext(Dispatchers.Main) {
//    val webView = WebView(context.applicationContext).apply {
//        settings.javaScriptEnabled = true
//        settings.builtInZoomControls = false
//        settings.displayZoomControls = false
//        isVerticalScrollBarEnabled = false
//        isHorizontalScrollBarEnabled = false
//        layoutParams = ViewGroup.LayoutParams(
//            widthPx,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//    }
//
//    val loaded = CompletableDeferred<Unit>()
//    webView.webViewClient = object : WebViewClient() {
//        override fun onPageFinished(view: WebView?, url: String?) {
//            if (!loaded.isCompleted) loaded.complete(Unit)
//        }
//
//    }
//    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
//    try {
//        println("height-1---${webView.contentHeight}")
//        withTimeout(timeoutMs) { loaded.await() }
//        println("height-2---${webView.contentHeight}")
//        delay(measureDelayMs)
//        println("height-3---${webView.contentHeight}")
//        webView.measure(
//            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//        )
//        webView.layout(0, 0, widthPx, webView.measuredHeight)
//        println("height-4---${webView.contentHeight}")
//        val density = context.resources.displayMetrics.density
//        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(1)
//        val interimHeight = max(contentHeightPx, webView.measuredHeight).coerceAtLeast(1)
//        println("height-5---${webView.contentHeight}")
//        webView.measure(
//            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(interimHeight, View.MeasureSpec.EXACTLY)
//        )
//        webView.layout(0, 0, widthPx, interimHeight)
//        delay(measureDelayMs)
//        val bitmap = createBitmap(widthPx, interimHeight)
//        val canvas = android.graphics.Canvas(bitmap)
//        webView.draw(canvas)
//        bitmap
//    } finally {
//        runCatching { webView.stopLoading() }
//        runCatching { webView.destroy() }
//    }
//}
//
//
//suspend fun htmlToBitmapContextCoroutine2(
//    context: Context,
//    html: String,
//    widthPx: Int,
//): Bitmap? = withContext(Dispatchers.Main.immediate) {
//    val webView = WebView(context.applicationContext).apply {
//        settings.javaScriptEnabled = true
//        settings.builtInZoomControls = false
//        settings.displayZoomControls = false
//        isVerticalScrollBarEnabled = false
//        isHorizontalScrollBarEnabled = false
//        layoutParams = ViewGroup.LayoutParams(
//            widthPx,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//    }
//
//    val loaded = CompletableDeferred<Unit>()
//    webView.webViewClient = object : WebViewClient() {
//        override fun onPageFinished(view: WebView?, url: String?) {
//            if (!loaded.isCompleted) loaded.complete(Unit)
//        }
//    }
//
//    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
//    withTimeoutOrNull(15_000L) { loaded.await() } ?: return@withContext null
//    var height = webView.contentHeight
//    val result = CompletableDeferred<Int>()
//    while (isActive) {
//        if (height > 0) {
//            result.complete(height)
//            break
//        } else {
//            delay(5L)
//            height = webView.contentHeight
//        }
//    }
//
//    val actualHeight = webView.contentHeight
//    webView.measure(
//        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
//        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//    )
//    webView.layout(0, 0, widthPx, webView.measuredHeight)
//    val density = context.resources.displayMetrics.density
//    val contentHeightPx = (actualHeight * density).toInt().coerceAtLeast(1)
//    val interimHeight = max(contentHeightPx, webView.measuredHeight).coerceAtLeast(1)
//    webView.measure(
//        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
//        View.MeasureSpec.makeMeasureSpec(interimHeight, View.MeasureSpec.UNSPECIFIED)
//    )
//    webView.layout(0, 0, widthPx, interimHeight)
//    delay(100L)
//    val bitmap = createBitmap(widthPx, interimHeight)
//    val canvas = Canvas(bitmap)
//    webView.draw(canvas)
//    bitmap
//}
//
//
