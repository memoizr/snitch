package snitch.example

import org.slf4j.Logger


class ExampleLoggerImpl(private val logger: Logger) : ExampleLogger {
    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String) {
        logger.warn(message)
    }

    override fun error(message: String) {
        logger.error(message)
    }
}