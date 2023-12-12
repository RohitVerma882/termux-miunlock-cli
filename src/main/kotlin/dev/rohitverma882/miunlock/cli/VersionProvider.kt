package dev.rohitverma882.miunlock.cli

import picocli.CommandLine.IVersionProvider

class VersionProvider : IVersionProvider {
    override fun getVersion(): Array<String> {
        return arrayOf("\${COMMAND-FULL-NAME} version 1.0")
    }
}