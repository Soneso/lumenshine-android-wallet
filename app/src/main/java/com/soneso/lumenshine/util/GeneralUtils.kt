package com.soneso.lumenshine.util

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context

object GeneralUtils {
    private const val EMPTY_STRING: String = ""

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("Copied Text", text)
        clipboard.primaryClip = clip
    }

    fun pasteFromClipboard(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val pasteData: String

        val shouldTakeFromClipboard = when {
            !clipboard.hasPrimaryClip() -> {
                false
            }
            !(clipboard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)) -> {
                false //setting to false since the clipboard does not have a plain text copied to it
            }
            else -> {
                true //setting to true since the clipboard has a plain text copied to it
            }
        }

        val item = clipboard.primaryClip?.getItemAt(0)

        if (!shouldTakeFromClipboard) {
            return EMPTY_STRING
        }

        pasteData = item?.text.toString()

        return if (!pasteData.isEmpty()) {
            pasteData
        } else {
            EMPTY_STRING
        }
    }
}