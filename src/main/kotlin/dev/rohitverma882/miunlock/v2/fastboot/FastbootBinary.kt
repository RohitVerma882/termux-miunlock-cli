package dev.rohitverma882.miunlock.v2.fastboot

import java.io.File

internal object FastbootBinary {
    private const val FASTBOOT_NAME = "mi-fastboot"

    @JvmStatic
    val FASTBOOT_BINARY: File? by lazy { find() }

    private fun find(): File? {
        return findViaPrefix() ?: findViaWhich()
    }

    private fun findViaWhich(): File? {
        val process = ProcessBuilder("which", FASTBOOT_NAME).start()
        if (process.waitFor() != 0) return null
        val output = process.inputStream.bufferedReader().use { r ->
            r.readLine().trim()
        }
        val file = File(output)
        if (!file.exists()) return null
        return file
    }

    private fun findViaPrefix(): File? {
        val prefixEnv = System.getenv("PREFIX") ?: return null
        val fastbootFile = File(prefixEnv).resolve("bin").resolve(FASTBOOT_NAME)
        if (!fastbootFile.exists()) return null
        return fastbootFile
    }
}