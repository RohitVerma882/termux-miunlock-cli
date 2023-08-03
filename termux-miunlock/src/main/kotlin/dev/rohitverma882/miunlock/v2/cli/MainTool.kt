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
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable

@Command(
    name = "termux-miunlock",
    versionProvider = VersionProvider::class,
    footer = ["Copyright(c) 2023"],
    description = ["A program that can be used to retrieve the bootloader unlock token for @|bold Xiaomi|@ devices. (and unlock the bootloader) using @|bold Termux|@."],
    mixinStandardHelpOptions = true,
//    showEndOfOptionsDelimiterInUsageHelp = true,
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

    @Spec
    private lateinit var spec: CommandSpec

    private var passToken: String? = null
    private var userId: String? = null
    private var deviceId: String? = null

    @Parameters(
        paramLabel = "DATA",
        description = ["Install 'miunlock-account-v2.apk' from repo, login and copypaste the response."],
    )
    private fun setLoginData(data: String) {
        val jsonData = try {
            String(Hex.decodeHex(data), StandardCharsets.UTF_8)
        } catch (e: DecoderException) {
            throw ParameterException(
                spec.commandLine(),
                "Decode response data failed: ${e.message}"
            )
        }
        try {
            val json = JSONObject(jsonData)
            passToken = json.getString("passToken")
            userId = json.getString("userId")
            deviceId = json.getString("deviceId")
        } catch (e: JSONException) {
            throw ParameterException(spec.commandLine(), "Parse response data failed: ${e.message}")
        }
    }

    private val UNLOCK_TOKEN_CACHE: HashMap<String, String> = HashMap()

    override fun call(): Int {
        val keystore = XiaomiKeystore.getInstance()
        keystore.setCredentials(userId, passToken, deviceId)
        println("INFO: Logged in succesfully: $userId")

        val host = (Utils.hosts[region] ?: Utils.hosts["india"]!!)
        println("INFO: Using host: $host, region: $region")

        println("INFO: Starting unlock procedure")

//        dummy token and product
        val token = "bvoohI51kPc6EvH/sxlzxDhsjLM="
        val product = "wayne"

        println("INFO: First trial unlock token: $token")
        try {
            val info = UnlockCommonRequests.userInfo(host)
            if (!info.isNullOrBlank() && isDebug) {
                println("INFO: Unlock request user info: $info")
            }
            try {
                UnlockCommonRequests.agreeRequest(host)
            } catch (_: Exception) {
            }
            val alert = UnlockCommonRequests.deviceClear(host, product)
            if (!alert.isNullOrBlank() && isDebug) {
                println("INFO: Unlock request device clear: $alert")
            }
        } catch (e: Exception) {
            println("WARN: Pre-unlock requests failed: ${e.message}")
        }

        try {
            val unlockData = UnlockCommonRequests.ahaUnlock(host, token, product, "", "", "");
            if (!unlockData.isNullOrBlank() && isDebug) {
                println("INFO: Unlock request response: $unlockData")
            }
            val json = JSONObject(unlockData)
            val code = json.optInt("code", -100)
            val description = json.optString("descEN", "empty")
            val encryptedData = json.optString("encryptedData", null)
            if (code != 0 && encryptedData.isNullOrBlank()) {
                println(
                    "Failed to unlock your device, Xiaomi server returned error $code:\\nError description: ${
                        UnlockCommonRequests.getUnlockCodeMeaning(
                            code,
                            json
                        )
                    }\\nServer description: $description"
                )
            } else {
                UNLOCK_TOKEN_CACHE[token] = encryptedData
            }
            println(encryptedData)
        } catch (e: Exception) {
            println("FAIL: Internal error while parsing unlock data: ${e.message}")
            return 1
        }

        XiaomiKeystore.clear()
        return 0
    }
}