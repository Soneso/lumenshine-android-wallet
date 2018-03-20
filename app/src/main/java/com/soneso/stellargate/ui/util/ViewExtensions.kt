package com.soneso.stellargate.ui.util

import android.util.Log
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.soneso.stellargate.ui.accounts.AccountsFragment
import java.util.*
import kotlin.math.min

/**
 * Extensions.
 * Created by cristi.paval on 3/20/18.
 */

fun ImageView.displayQrCode(text: String) {
    val sizeAsPixels = min(layoutParams.height, layoutParams.width)
    scaleType = ImageView.ScaleType.CENTER_INSIDE

    val multiFormatWriter = MultiFormatWriter()
    try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1

        val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, sizeAsPixels, sizeAsPixels, hints)
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)

        setImageBitmap(bitmap)

    } catch (e: WriterException) {
        Log.e(AccountsFragment.TAG, e.javaClass.simpleName, e)
    }
}