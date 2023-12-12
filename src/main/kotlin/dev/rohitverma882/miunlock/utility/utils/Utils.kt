package dev.rohitverma882.miunlock.utility.utils

class Utils {
    companion object {
        val hosts: Map<String, String> = mapOf(
            "india" to "https://in-unlock.update.intl.miui.com",
            "global" to "https://unlock.update.intl.miui.com",
            "china" to "https://unlock.update.miui.com",
            "russia" to "https://ru-unlock.update.intl.miui.com",
            "europe" to "https://eu-unlock.update.intl.miui.com"
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