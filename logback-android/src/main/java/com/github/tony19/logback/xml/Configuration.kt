package com.github.tony19.logback.xml

import ch.qos.logback.classic.spi.ILoggingEvent
import com.gitlab.mvysny.konsumexml.Konsumer

data class Configuration (
    var debug: Boolean? = false,
    var scan: Boolean? = false,
    var scanPeriod: String? = null,
    var appenders: List<Appender>? = emptyList(),
    var properties: List<Property>? = emptyList(),
    var timestamps: List<Timestamp>? = emptyList(),
    var includes: List<Include>? = emptyList(),
    var optionalIncludes: List<Includes>?,
    var loggers: List<Logger>?,
    var root: Root?,
    var resolvedAppenders: Map<String, ch.qos.logback.core.Appender<ILoggingEvent>> = emptyMap()
) {
    companion object {
        fun xml(k: Konsumer): Configuration {
            k.checkCurrent("configuration")

            return Configuration(
                debug = k.attributes.getValueOpt("debug")?.toBoolean(),
                scan = k.attributes.getValueOpt("scan")?.toBoolean(),
                scanPeriod = k.attributes.getValueOpt("scanPeriod"),
                appenders = k.children("appender") { Appender.xml(this) },
                properties = k.children("property") { Property.xml(this) },
                timestamps = k.children("timestamp") { Timestamp.xml(this) },
                includes = k.children("include") { Include.xml(this) },
                optionalIncludes = k.children("includes") { Includes.xml(this) },
                loggers = k.children("logger") { Logger.xml(this) },
                root = k.childOpt("root") { Root.xml(this) }
            ).apply {
                val appenderRefs = getAppenderRefs().map {ref ->
                    appenders?.find { it.name == ref }
                }

                resolvedAppenders = resolveAppenders(k, appenderRefs)
            }
        }
    }

    private fun getAppenderRefs(): List<String?> {
        val rootAppenderRefs = root?.appenderRefs?.map { it.ref }
        val loggerAppenderRefs = loggers?.flatMap { logger -> logger.appenderRefs?.map { it.ref } }
        return rootAppenderRefs!! + loggerAppenderRefs!!
    }

    private fun resolveAppenders(k: Konsumer, appenderRefs: List<String?>) : Map<String, ch.qos.logback.core.Appender<ILoggingEvent>> {
        return appenderRefs.map { appenders.find { appender -> appender.name == it } }
    }
}