package android.print


class DefaultPrintWriteResult(
    private val onError: (error: String?) -> Unit,
    private val onFinish: () -> Unit,
    private val onCancel: () -> Unit,
) : PrintDocumentAdapter.WriteResultCallback() {
    override fun onWriteCancelled() {
        onCancel()
    }

    override fun onWriteFinished(pages: Array<out PageRange?>?) {
        onFinish()
    }

    override fun onWriteFailed(error: CharSequence?) {
        onError(error.toString())
    }
}