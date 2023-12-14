package dev.rohitverma882.miunlock.utility.utils

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Utils {
    fun buildHmacKey(hex: String): ByteArray? {
        try {
            return Hex.decodeHex(hex.toCharArray())
        } catch (ignored: DecoderException) {
        }
        return null
    }

    @JvmStatic
    fun urlEncode(data: String?): String? {
        return try {
            URLEncoder.encode(data, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            null
        }
    }

    @JvmStatic
    fun findJsonStart(data: String): String? {
        val d = data.toCharArray()
        for (i in d.indices) {
            if (d[i] == '{') {
                return data.substring(i)
            }
        }
        return null
    }
}