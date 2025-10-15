package com.erabigroup.htmltobitmapkmp

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.DefaultPrintLayoutResult
import android.print.DefaultPrintWriteResult
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

class AndroidPrintCallbackManager {
    suspend fun handle(
        adapter: PrintDocumentAdapter,
        attrs: PrintAttributes,
        descriptor: ParcelFileDescriptor,
        timeout: Long
    ) {
        val layoutDone = CompletableDeferred<Unit>()
        val writeDone = CompletableDeferred<Unit>()

        val layoutCallback = DefaultPrintLayoutResult(
            onCancel = { layoutDone.completeExceptionally(IllegalStateException("canceled")) },
            onFailed = { layoutDone.completeExceptionally(Exception(it)) },
            onFinish = { _, _ -> layoutDone.complete(Unit) }
        )

        adapter.onLayout(null, attrs, null, layoutCallback, null)

        withTimeout(timeout) { layoutDone.await() }

        val writeCallback = DefaultPrintWriteResult(
            onCancel = { writeDone.completeExceptionally(IllegalStateException("canceled")) },
            onFinish = { writeDone.complete(Unit) },
            onError = { writeDone.completeExceptionally(IllegalStateException(it)) }
        )

        adapter.onWrite(
            arrayOf(PageRange.ALL_PAGES),
            descriptor,
            CancellationSignal(),
            writeCallback
        )
        withTimeout(timeout) { writeDone.await() }
    }
}