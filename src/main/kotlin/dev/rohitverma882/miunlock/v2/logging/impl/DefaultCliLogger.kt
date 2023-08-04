package dev.rohitverma882.miunlock.v2.logging.impl

import dev.rohitverma882.miunlock.v2.cli.MainTool
import dev.rohitverma882.miunlock.v2.logging.CliLogger

import java.util.logging.Logger
import java.util.logging.SimpleFormatter

internal class DefaultCliLogger(
    private val logger: Logger = Logger.getLogger(MainTool::class.java.name),
    private val errorLogger: Logger = Logger.getLogger(logger.name + "Err"),
) : CliLogger {

    init {
        logger.useParentHandlers = false
        if (logger.handlers.isEmpty()) {
            logger.addHandler(FlushingStreamHandler(System.out, SimpleFormatter()))
        }
    }

    companion object {
        init {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%4\$s: %5\$s %n")
        }
    }

    override fun error(msg: String) = errorLogger.severe(msg)
    override fun info(msg: String) = logger.info(msg)
    override fun trace(msg: String) = logger.finest(msg)
    override fun warn(msg: String) = errorLogger.warning(msg)
}