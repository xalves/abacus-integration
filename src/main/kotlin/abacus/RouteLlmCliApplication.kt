package abacus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(RouteLlmProperties::class)
class RouteLlmCliApplication

fun main(args: Array<String>) {
    runApplication<RouteLlmCliApplication>(*args)
}

@ConfigurationProperties(prefix = "routellm")
data class RouteLlmProperties(
    var apiUrl: String = "https://routellm.abacus.ai/v1/chat/completions",
    var model: String = "route-llm",
    var apiKey: String? = null
)