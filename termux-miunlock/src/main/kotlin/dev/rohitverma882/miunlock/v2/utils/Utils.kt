package dev.rohitverma882.miunlock.v2.utils

class Utils {
    companion object {
        val hosts: Map<String, String> = mapOf(
            "india" to "in-unlock.update.intl.miui.com",
            "global" to "unlock.update.intl.miui.com",
            "china" to "unlock.update.miui.com",
            "russia" to "ru-unlock.update.intl.miui.com",
            "europe" to "eu-unlock.update.intl.miui.com"
        )

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
}