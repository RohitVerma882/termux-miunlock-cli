package dev.rohitverma882.miunlock.v2.cli

import dev.rohitverma882.miunlock.v2.xiaomi.unlock.UnlockException.Code
import dev.rohitverma882.miunlock.v2.inet.CustomHttpException
import dev.rohitverma882.miunlock.v2.logging.impl.DefaultCliLogger
import dev.rohitverma882.miunlock.v2.utility.utils.Utils
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiKeystore
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiProcedureException
import dev.rohitverma882.miunlock.v2.xiaomi.unlock.UnlockCommonRequests
import dev.rohitverma882.miunlock.v2.xiaomi.unlock.UnlockException

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
    sortOptions = false,
    sortSynopsis = false,
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

    private val unlockTokenCache: HashMap<String, String> = HashMap()
    private val logger = DefaultCliLogger()

    override fun call(): Int {
        val keystore = XiaomiKeystore.getInstance()
        keystore.setCredentials(userId, passToken, deviceId)
        logger.info("Logged in succesfully: $userId")

        logger.info("Starting unlock procedure")
        val token = "bvoohI51kPc6EvH/sxlzxDhsjLM="
//        val token: String? = FastbootCommons.getvar("token", device.getSerial())
//        Thread.sleep(400)
//        if (token == null) {
//            throw UnlockException(
//                "Failed to get the device unlock token",
//                UnlockException.Code.INFO_RETRIVE_FAILED,
//                FastbootCommons.getLastError(device.getSerial())
//            )
//        }
        if (isDebug) {
            logger.info("First trial unlock token: $token")
        }
        val product = "wayne"
//        product: String? = FastbootCommons.getvar("product", device.getSerial())
//        Thread.sleep(600)
//        if (product == null) {
//            throw UnlockException(
//                "Failed to get fastboot variable product: ",
//                UnlockException.Code.INFO_RETRIVE_FAILED,
//                FastbootCommons.getLastError(device.getSerial())
//            )
//        }
//        token = FastbootCommons.getvar("token", device.getSerial())
//        if (token == null) {
//            throw UnlockException(
//                "Failed to get the device unlock token",
//                Code.INFO_RETRIVE_FAILED,
//                FastbootCommons.getLastError(device.getSerial())
//            )
//        }
//        Log.info("Unlock request token: $token")
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
        logger.warn(
            StringBuilder().append("You're about to unlock your device.").append('\n')
                .append("This tool will request the unlock token from Xiaomi server.").append('\n')
                .append("If your Xiaomi account has been bound to the device for enough time, the server is going to provide the unlock token and this tool will proceed with the unlock procedure.")
                .toString()
        )
        try {
//            token = FastbootCommons.getvar("token", device.getSerial())
//            if (token == null) {
//                throw UnlockException(
//                    "Failed to get the device unlock token",
//                    Code.INFO_RETRIVE_FAILED,
//                    FastbootCommons.getLastError(device.getSerial())
//                )
//            }
//            logger.info("Unlock request token: $token")

            logger.info("Requesting device unlock token")
            val unlockData = UnlockCommonRequests.ahaUnlock(host, token, product, "", "", "")
            if (unlockData.isNullOrBlank()) {
                throw UnlockException(
                    "Failed to get the unlock data required",
                    Code.INFO_RETRIVE_FAILED,
                    "Null response from unlock common request"
                )
            }
            if (isDebug) {
                logger.info("Unlock request response: $unlockData")
            }
            val json = JSONObject(unlockData)
            val code = json.optInt("code", -100)
            val description = json.optString("descEN", "empty")
            val encryptedData = json.optString("encryptedData", null)
            if (code != 0 && encryptedData.isNullOrBlank()) {
                logger.error(
                    StringBuilder().append("Failed to unlock your device, Xiaomi server returned error ")
                        .append(code).append(":").append('\n').append(
                            "Error description: "
                        ).append(
                            UnlockCommonRequests.getUnlockCodeMeaning(
                                code, json
                            )
                        ).append('\n').append("Server description: ").append(description).toString()
                )
                throw UnlockException(
                    "The unlocking was aborted",
                    Code.ABORTED
                )
            } else {
                unlockTokenCache[token] = encryptedData
                if (isDebug) {
                    logger.info("Device unlock token: $encryptedData")
                }
            }
//            logger.info("Unlocking device using fastboot")
//            val unlocked = FastbootCommons.oemUnlock(device.getSerial(), encryptData)
//            if (unlocked) {
//                throw UnlockException(
//                    "Failed to unlock the device, fastboot exit with status non zero or internal error",
//                    Code.UNLOCK_ERROR,
//                    "Last error: " + FastbootCommons.getLastError(device.getSerial())
//                )
//            }
        } catch (e: XiaomiProcedureException) {
            throw UnlockException(e)
        } catch (e: CustomHttpException) {
            throw UnlockException(e)
        } catch (e: UnlockException) {
//            logger.error(e.message ?: e.stackTraceToString())
            throw e
        } catch (e: Exception) {
            logger.error("Internal error while parsing unlock data: ${e.message}")
            return 1
        }
        XiaomiKeystore.clear()
        return 0
    }
}