package android.print


class DefaultPrintLayoutResult(
    private val onCancel: () -> Unit,
    private val onFailed: (error: String) -> Unit,
    private val onFinish: (info: PrintDocumentInfo?, changed: Boolean) -> Unit,
) : PrintDocumentAdapter.LayoutResultCallback() {
    override fun onLayoutCancelled() {
        onCancel()
    }

    override fun onLayoutFailed(error: CharSequence?) {
        onFailed(error.toString())
    }

    override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
        onFinish(info, changed)
    }
}