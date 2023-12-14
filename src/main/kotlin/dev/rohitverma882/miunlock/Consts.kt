package dev.rohitverma882.miunlock

import dev.rohitverma882.miunlock.utility.utils.Utils.buildHmacKey

object Consts {
    val HOSTS: Map<String, String> = mapOf(
        "india" to "https://in-unlock.update.intl.miui.com",
        "global" to "https://unlock.update.intl.miui.com",
        "china" to "https://unlock.update.miui.com",
        "russia" to "https://ru-unlock.update.intl.miui.com",
        "europe" to "https://eu-unlock.update.intl.miui.com"
    )

    const val SERVICE_NAME = "unlockApi"
    const val URL_FIRST =
        "https://account.xiaomi.com/pass/serviceLogin?sid=${SERVICE_NAME}&_json=true&passive=true&hidden=false"

    const val SID = "miui_unlocktool_client"
    const val CLIENT_VERSION = "5.5.224.55"
    const val NONCEV2 = "/api/v2/nonce"
    const val USERINFOV3 = "/api/v3/unlock/userinfo"
    const val DEVICECLEARV3 = "/api/v2/unlock/device/clear"
    const val AHAUNLOCKV3 = "/api/v3/ahaUnlock"

    @JvmStatic
    val UNLOCK_HMAC_KEY =
        buildHmacKey("327442656f45794a54756e6d57554771376251483241626e306b324e686875724f61714266797843754c56676e3441566a3773776361776535337544556e6f")
    const val DEFAULT_IV = "0102030405060708"
}