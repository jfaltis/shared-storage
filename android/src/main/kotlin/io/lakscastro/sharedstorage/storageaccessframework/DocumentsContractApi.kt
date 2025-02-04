package io.lakscastro.sharedstorage.storageaccessframework

import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.lakscastro.sharedstorage.ROOT_CHANNEL
import io.lakscastro.sharedstorage.SharedStoragePlugin
import io.lakscastro.sharedstorage.plugin.API_21
import io.lakscastro.sharedstorage.plugin.ActivityListener
import io.lakscastro.sharedstorage.plugin.Listenable
import io.lakscastro.sharedstorage.plugin.notSupported
import io.lakscastro.sharedstorage.storageaccessframework.lib.GET_DOCUMENT_THUMBNAIL
import io.lakscastro.sharedstorage.storageaccessframework.lib.bitmapToBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DocumentsContractApi(private val plugin: SharedStoragePlugin) :
    MethodChannel.MethodCallHandler, Listenable, ActivityListener {
  private var channel: MethodChannel? = null

  companion object {
    private const val CHANNEL = "documentscontract"
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      GET_DOCUMENT_THUMBNAIL -> {
        if (Build.VERSION.SDK_INT >= API_21) {
          val uri = Uri.parse(call.argument("uri"))
          val width = call.argument<Int>("width")!!
          val height = call.argument<Int>("height")!!

          val bitmap =
              DocumentsContract.getDocumentThumbnail(
                  plugin.context.contentResolver,
                  uri,
                  Point(width, height),
                  null
              )

          CoroutineScope(Dispatchers.Default).launch {
            if (bitmap != null) {
              val base64 = bitmapToBase64(bitmap)

              val data =
                  mapOf(
                    "base64" to base64,
                    "uri" to "$uri",
                    "width" to bitmap.width,
                    "height" to bitmap.height,
                    "byteCount" to bitmap.byteCount,
                    "density" to bitmap.density
                  )

              launch(Dispatchers.Main) { result.success(data) }
            }
          }
        } else {
          result.notSupported(call.method, API_21)
        }
      }
    }
  }

  override fun startListening(binaryMessenger: BinaryMessenger) {
    if (channel != null) stopListening()

    channel = MethodChannel(binaryMessenger, "$ROOT_CHANNEL/$CHANNEL")
    channel?.setMethodCallHandler(this)
  }

  override fun stopListening() {
    if (channel == null) return

    channel?.setMethodCallHandler(null)
    channel = null
  }

  override fun startListeningToActivity() {
    /** Implement if needed */
  }

  override fun stopListeningToActivity() {
    /** Implement if needed */
  }
}
