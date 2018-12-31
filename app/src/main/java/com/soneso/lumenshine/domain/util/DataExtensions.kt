package com.soneso.lumenshine.domain.util

import android.util.Log
import com.soneso.lumenshine.domain.usecases.UserSecurityHelper
import org.bouncycastle.util.encoders.Base64
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.*

fun CharSequence.toCharArray(): CharArray {
    val array = CharArray(length)
    asSequence().forEachIndexed { index, c ->
        array[index] = c
    }
    return array
}

@Suppress("unused")
fun CharArray.toByteArray(): ByteArray {
    val charBuffer = CharBuffer.wrap(this)
    val byteBuffer = Charset.forName("UTF-8").encode(charBuffer)
    return Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit())
}

fun CharSequence.padToBlocks(blockSize: Int): CharSequence {
    val resultSize = if (length % blockSize != 0) {
        val blockCount = length / blockSize + 1
        blockSize * blockCount
    } else {
        length
    }
    return padEnd(resultSize)
}

fun CharSequence.toByteArray(): ByteArray {
    return toString().toByteArray(charset("UTF-8"))
}

fun logLongString(string: String) {

    val maxLogSize = 1000
    for (i in 0..string.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > string.length) string.length else end
        Log.v(UserSecurityHelper.TAG, string.substring(start, end))
    }
}

fun ByteArray.base64String(): String {
    return Base64.toBase64String(this)
}

enum class SortType {
    ASC, DESC
}