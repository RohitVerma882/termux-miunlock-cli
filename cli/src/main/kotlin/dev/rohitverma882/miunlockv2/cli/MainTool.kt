package dev.rohitverma882.miunlockv2.cli

import dev.rohitverma882.miunlockv2.utils.Utils

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import java.util.concurrent.Callable

@Command(
    name = "termux-miunlockv2",
    version = ["1.0"],
    description = ["A program that can be used to retrieve the bootloader unlock token for Xiaomi devices. (and unlock the bootloader) using Termux."],
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
        names = ["--verbose"],
        description = ["Output messages about what the tool is doing"]
    )
    private var verbose: Boolean = false

    @Option(
        names = ["--region"],
        paramLabel = "REGION",
        completionCandidates = RegionCandidates::class,
        description = ["Tool server host regions: \${COMPLETION-CANDIDATES}"],
        defaultValue = "unknown",
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS
    )
    private lateinit var region: String

    @Option(
        names = ["--data"],
        paramLabel = "DATA",
        description = ["Install 'miunlock-account.apk' from repo, login and copypaste the response."],
        required = true
    )
    private lateinit var data: String

    private lateinit var host: String

    override fun call(): Int {
        host = Utils.hosts[region]!!

        return 0
    }
}