package dev.rohitverma882.miunlockv2.cli

import picocli.CommandLine

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val mainTool = MainTool()
    val exitCode = CommandLine(mainTool).execute(*args)
    exitProcess(exitCode)
}
