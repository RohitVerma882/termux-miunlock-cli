package dev.rohitverma882.miunlock.v2.cli

import dev.rohitverma882.miunlock.v2.utils.Utils
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiKeystore
import dev.rohitverma882.miunlock.v2.xiaomi.unlock.UnlockCommonRequests

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.json.JSONException
import org.json.JSONObject

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable

@Command(
    name = "termux-miunlock",
    versionProvider = VersionProvider::class,
    footer = ["Copyright(c) 2023"],
    description = ["A program that can be used to retrieve the bootloader unlock token for @|bold Xiaomi|@ devices. (and unlock the bootloader) using @|bold Termux|@."],
    mixinStandardHelpOptions = true,
    showEndOfOptionsDelimiterInUsageHelp = true,
    usageHelpAutoWidth = true,
    sortOptions = false,
    sortSynopsis = false,
    showDefaultValues = true,
//    separator = "::",
    requiredOptionMarker = '*',
    abbreviateSynopsis = true
)
class MainTool : Callable<Int> {
    @Option(
        names = ["--help"], usageHelp = true, description = ["Display a help message"]
    )
    private var help: Boolean = false

    @Option(
        names = ["--version"], versionHelp = true, description = ["Version information"]
    )
    private var version: Boolean = false

    @Option(
        names = ["--debug"], description = ["Output messages about what the tool is doing"]
    )
    private var isDebug: Boolean = false

    @Option(
        names = ["--region"],
        paramLabel = "REGION",
        completionCandidates = RegionCandidates::class,
        description = ["Tool server host regions: \${COMPLETION-CANDIDATES}"],
        defaultValue = "india",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private lateinit var region: String

    @Parameters(
        paramLabel = "DATA",
        description = ["Install 'miunlock-account-v2.apk' from repo, login and copypaste the response."],
    )
    private lateinit var loginData: String

    override fun call(): Int {
        val jsonData = try {
            String(Hex.decodeHex(loginData), StandardCharsets.UTF_8)
        } catch (e: DecoderException) {
            println("FAIL: Unable to decode response data: ${e.message}")
            return 1
        }

        val passToken: String?
        val userId: String?
        val deviceId: String?
        try {
            val json = JSONObject(jsonData)
            passToken = json.getString("passToken")
            userId = json.getString("userId")
            deviceId = json.getString("deviceId")
        } catch (e: JSONException) {
            println("FAIL: Unable to parse response data: ${e.message}")
            return 1
        }

        val keystore = XiaomiKeystore.getInstance()
        keystore.setCredentials(userId, passToken, deviceId)
        println("INFO: Logged in succesfully: $userId")

        val host = (Utils.hosts[region] ?: Utils.hosts["india"]!!)
        println("INFO: Using host '$host' for '$region'")

        println("INFO: Starting unlock procedure")
        val token = "bvoohI51kPc6EvH/sxlzxDhsjLM="
        val product = "wayne"
        println("INFO: First trial unlock token: $token")
        try {
            val info = UnlockCommonRequests.userInfo(host)
            if (info.isNullOrBlank() && isDebug) {
                println("INFO: Unlock request user info: $info")
            }
            try {
                UnlockCommonRequests.agreeRequest(host)
            } catch (_: Exception) {
            }
            val alert = UnlockCommonRequests.deviceClear(host, product)
            if (alert.isNullOrBlank() && isDebug) {
                println("INFO: Unlock request device clear: $alert")
            }
        } catch (e: Exception) {
            println("WARN: Pre-unlock requests failed: ${e.message}")
        }

        try {
            val unlockData = UnlockCommonRequests.ahaUnlock(host, token, product, "", "", "");
            println("INFO: Unlock request response: $unlockData");
        } catch (e: Exception) {
            println("FAIL: Internal error while parsing unlock data: ${e.message}")
        }

        XiaomiKeystore.clear()
        return 0
    }
}