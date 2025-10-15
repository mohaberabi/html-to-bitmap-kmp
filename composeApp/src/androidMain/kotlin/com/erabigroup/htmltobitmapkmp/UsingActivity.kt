package com.erabigroup.htmltobitmapkmp

//private suspend fun htmlToBitmapAndroid(
//    activity: Activity,
//    html: String,
//    widthPx: Int,
//    timeoutMs: Long = 10_000,
//    backgroundColor: Int = 0xFFFFFFFF.toInt()
//): Bitmap = withContext(Dispatchers.Main) {
//    WebView.enableSlowWholeDocumentDraw()
//    val root = activity.findViewById<ViewGroup>(android.R.id.content)
//    val container = FrameLayout(activity).apply { alpha = 0f }
//    root.addView(container)
//    val webView = WebView(activity).apply {
//        isVerticalScrollBarEnabled = false
//        isHorizontalScrollBarEnabled = false
//        settings.displayZoomControls = false
//        settings.builtInZoomControls = false
//        settings.javaScriptEnabled = true
//        setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
//        setBackgroundColor(backgroundColor)
//        layoutParams = FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
//    }
//    container.addView(webView)
//
//    try {
//        val painted = CompletableDeferred<Unit>()
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageCommitVisible(view: WebView?, url: String?) {
//                if (!painted.isCompleted) painted.complete(Unit)
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                if (!painted.isCompleted) painted.complete(Unit)
//            }
//        }
//
//        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
//
//        withTimeout(timeoutMs) {
//            painted.await()
//            val vto = webView.viewTreeObserver
//            val cont = CompletableDeferred<Unit>()
//            val listener = object : ViewTreeObserver.OnPreDrawListener {
//                override fun onPreDraw(): Boolean {
//                    if (vto.isAlive) vto.removeOnPreDrawListener(this)
//                    if (!cont.isCompleted) cont.complete(Unit)
//                    return true
//                }
//            }
//            vto.addOnPreDrawListener(listener)
//            cont.await()
//        }
//
//        webView.measure(
//            View.MeasureSpec.makeMeasureSpec(
//                widthPx,
//                View.MeasureSpec.EXACTLY
//            ),
//            View.MeasureSpec.makeMeasureSpec(
//                0,
//                View.MeasureSpec.EXACTLY
//            )
//        )
//
//        val density = activity.resources.displayMetrics.density
//        val contentHeightPx = (webView.contentHeight * density).toInt()
//        val measuredHeightPx = webView.measuredHeight
//        val heightPx = max(1, max(contentHeightPx, measuredHeightPx))
//        webView.layout(0, 0, widthPx, heightPx)
//        val bitmap = createBitmap(widthPx, heightPx)
//        val canvas = android.graphics.Canvas(bitmap)
//        canvas.drawColor(backgroundColor)
//        webView.draw(canvas)
//        bitmap
//    } finally {
//        try {
//            container.removeView(webView)
//        } catch (_: Throwable) {
//        }
//        try {
//            root.removeView(container)
//        } catch (_: Throwable) {
//        }
//        try {
//            webView.destroy()
//        } catch (_: Throwable) {
//        }
//    }
//}