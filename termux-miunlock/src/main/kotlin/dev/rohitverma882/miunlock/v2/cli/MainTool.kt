package dev.rohitverma882.miunlock.v2.cli

import dev.rohitverma882.miunlock.v2.utils.Utils

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

@Command(
    name = "termux-miunlock",
    version = ["1.0"],
    description = ["A program that can be used to retrieve the bootloader unlock token for @|bold Xiaomi|@ devices. (and unlock the bootloader) using @|bold Termux|@."],
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true
)
class MainTool : Callable<Int> {
    @Option(
        names = ["--help"],
        usageHelp = true,
        description = ["Display a help message"]
    )
    private var help: Boolean = false

    @Option(
        names = ["--version"],
        versionHelp = true,
        description = ["Version information"]
    )
    private var version: Boolean = false

    @Option(
        names = ["--debug"],
        description = ["Output messages about what the tool is doing"],
    )
    private var debug: Boolean = false

    @Option(
        names = ["--region"],
        paramLabel = "REGION",
        completionCandidates = RegionCandidates::class,
        description = ["Tool server host regions: \${COMPLETION-CANDIDATES}"],
        defaultValue = "india",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
        interactive = true
    )
    private lateinit var region: String

    @Parameters(
        paramLabel = "DATA",
        description = ["Install 'miunlock-account.apk' from repo, login and copypaste the response."]
    )
    private var loginData: String? = null

    override fun call(): Int {
//        var host = Utils.hosts[region] ?: Utils.hosts[2]!!
        val host = Utils.hosts[region]!!
        return 0
    }
}