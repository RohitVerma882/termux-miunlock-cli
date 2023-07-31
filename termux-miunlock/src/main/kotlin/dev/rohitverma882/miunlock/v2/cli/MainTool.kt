package dev.rohitverma882.miunlock.v2.cli

import dev.rohitverma882.miunlock.v2.utils.Utils

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import java.util.concurrent.Callable
import java.nio.charset.StandardCharsets

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
    separator = "::",
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
    private var debug: Boolean = false

    @Option(
        names = ["--region"],
        paramLabel = "REGION",
        completionCandidates = RegionCandidates::class,
        description = ["Tool server host regions: \${COMPLETION-CANDIDATES}"],
        defaultValue = "india",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private lateinit var region: String

    @Option(
        names = ["--data"],
        paramLabel = "DATA",
        description = ["Install 'miunlock-account-v2.apk' from repo, login and copypaste the response."],
        required = true
    )
    private lateinit var loginData: String

    override fun call(): Int {
        val host = (Utils.hosts[region] ?: Utils.hosts["india"]!!)
        println("INFO: Using host '$host' for '$region'")

        val jsonData = try {
            String(Hex.decodeHex(loginData), StandardCharsets.UTF_8)
        } catch (e: DecoderException) {
            println("FAIL: Unable to decode data! add '--debug' to show more info.")
            if (debug) {
                e.printStackTrace()
            }
            return 1
        }

        println("INFO: $jsonData")
        return CommandLine.ExitCode.OK
    }
}