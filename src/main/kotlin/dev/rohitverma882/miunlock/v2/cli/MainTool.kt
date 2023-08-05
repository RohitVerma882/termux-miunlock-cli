package dev.rohitverma882.miunlock.v2.cli

import dev.rohitverma882.miunlock.v2.inet.CustomHttpException
import dev.rohitverma882.miunlock.v2.logging.impl.DefaultCliLogger
import dev.rohitverma882.miunlock.v2.utility.utils.Utils
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiKeystore
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiProcedureException
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
    description = ["A program that can be used to retrieve the bootloader unlock token for @|bold Xiaomi|@ devices. (and unlock the bootloader) using @|bold Termux|@."],
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true,
    showDefaultValues = true,
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

    private lateinit var host: String

    @Option(
        names = ["--region"],
        paramLabel = "REGION",
        completionCandidates = RegionCandidates::class,
        description = ["Tool server host regions: \${COMPLETION-CANDIDATES}"],
        defaultValue = "india",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private fun setRegionHost(region: String) {
        if (Utils.hosts.containsKey(region)) {
            host = (Utils.hosts[region] ?: Utils.hosts["india"]!!)
        } else {
            throw ParameterException(spec.commandLine(), "Invalid region value: $region")
        }
    }

    @Option(
        names = ["--token"], paramLabel = "TOKEN", description = ["Used to verify device token"],
        required = true
    )
    private lateinit var token: String

    @Option(
        names = ["--product"],
        paramLabel = "PRODUCT",
        description = ["Used to verify device product"],
        required = true
    )
    private lateinit var product: String

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
                spec.commandLine(), "Failed to decode response data: ${e.message}"
            )
        }
        try {
            val json = JSONObject(jsonData)
            passToken = json.getString("passToken")
            userId = json.getString("userId")
            deviceId = json.getString("deviceId")
        } catch (e: JSONException) {
            throw ParameterException(
                spec.commandLine(), "Failed to parse response data: ${e.message}"
            )
        }
    }

    private val unlockTokenCache: HashMap<String?, String> = HashMap()
    private val logger = DefaultCliLogger()

    override fun call(): Int {
        val keystore = XiaomiKeystore.getInstance()
        keystore.setCredentials(userId, passToken, deviceId)
        logger.info("Logged in succesfully: $userId")

        logger.info("Starting unlock procedure")
        try {
            logger.info("Checking account unlock availability")
            val info = UnlockCommonRequests.userInfo(host)
            if (!info.isNullOrBlank() && isDebug) {
                logger.info("Unlock request user info: $info")
            }
            try {
                UnlockCommonRequests.agreeRequest(host)
            } catch (_: Exception) {
            }
            logger.info("Checking device unlock capability")
            val alert = UnlockCommonRequests.deviceClear(host, product)
            if (!alert.isNullOrBlank() && isDebug) {
                logger.info("Unlock request device clear: $alert")
            }
        } catch (e: Exception) {
            logger.warn("Pre-unlock requests failed: ${e.message}")
        }
        logger.info("Unlock request token: $token, product: $product")
        try {
            logger.info("Requesting device unlock token")
            val unlockData = UnlockCommonRequests.ahaUnlock(host, token, product, "", "", "")
            if (unlockData.isNullOrBlank()) {
                logger.error("Failed to get the unlock data required. Null response from unlock common request")
                return 1
            }
            if (isDebug) {
                logger.info("Unlock request response: $unlockData")
            }
            val json = JSONObject(unlockData)
            val code = json.optInt("code", -100)
            val description = json.optString("descEN", "empty")
            val encryptData = json.optString("encryptData", null)
            if (code != 0 && encryptData.isNullOrBlank()) {
                val commonErr = UnlockCommonRequests.getUnlockCodeMeaning(
                    code, json
                )
                logger.error(
                    StringBuilder().append("Failed to unlock your device, Xiaomi server returned error ")
                        .append(code).append(":").append('\n').append(
                            "Error description: "
                        ).append(commonErr).append('\n').append("Server description: ")
                        .append(description).toString()
                )
                return 1
            }
            unlockTokenCache[token] = encryptData
            logger.info("Unlock device token: $encryptData")
        } catch (e: XiaomiProcedureException) {
            logger.error("Xiaomi procedure failed: ${e.message}")
            return 1
        } catch (e: CustomHttpException) {
            logger.error("Internet connection error: ${e.message}")
            return 1
        } catch (e: InterruptedException) {
            logger.error("InterruptedException: ${e.message}")
            return 1
        } catch (e: Exception) {
            logger.error("Internal error while parsing unlock data: ${e.message}")
            return 1
        }
        XiaomiKeystore.clear()
        return 0
    }
}