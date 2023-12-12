package dev.rohitverma882.miunlock.cli

import picocli.CommandLine

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val exitCode = CommandLine(MainTool()).execute(*args)
    exitProcess(exitCode)
}
