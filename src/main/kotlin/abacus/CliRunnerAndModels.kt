package abacus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSource
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Scanner

private const val SUB_MENU = "[SUB-MENU]"

private const val WORD = "[WORD]"

@Component
class CliRunner(
    private val props: RouteLlmProperties
) : CommandLineRunner {

    private val mapper = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
    private val json = "application/json".toMediaType()

    // OkHttp with longer timeouts for streaming
    private val client = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(0)) // no read timeout for SSE-like stream
        .build()

    private val contextOptions = listOf(
        "[SUB-MENU] You will get a sentence, your task is to avoid using 'are'. Also in your answer avoid using 'be' " +
                "forms. This was flagged from write-good style from vale. Sentence: [SENTENCE]",
        "[SUB-MENU] You will get a sentence, your task is to avoid using 'is' and make the sentence look as similar as " +
                "possible to the original one. Also in your answer avoid using 'be' and also solutions that use 's will " +
                "not work as well forms. This was flagged from write-good style from vale, the sentence should be a " +
                "syntactically correct english sentence. Sentence: [SENTENCE] Your output should be formatted like " +
                "Answer: your answer",
        "[SUB-MENU] You will get a sentence, your task is to avoid using 'be' and make the sentence look as similar as " +
                "possible to the original one. Also in your answer avoid using 'is' and also solutions that use 's will " +
                "not work as well forms. This was flagged from write-good style from vale, the sentence should be a " +
                "syntactically correct english sentence. Sentence: [SENTENCE] Your output should be formatted like " +
                "Answer: your answer",
        "[SUB-MENU] The word \"[WORD]\" is too wordy related based on write-good.TooWordy, propose alternatives. Assume the" +
                " following sentence is where the word in question is used: ",
        "[SUB-MENU] The word \"[WORD]\" may come as condescending following the rule alex.condescending, propose alternatives." +
                " Assume the following sentence is where the word in question is used: ",
        "You are an expert technical writer, please assist in the best way you cam."
    )

    private val appModes = listOf(
        "Standard Mode",
        "Benchmark Mode"

    )

    override fun run(vararg args: String?) {
        require(!props.apiKey.isNullOrBlank()) {
            "Missing API key. Set env var ROUTELLM_API_KEY or configure routellm.api-key."
        }

        val scanner = Scanner(System.`in`)
        println("RouteLLM Kotlin CLI (Spring Boot)")

        loop@ while (true) {
            println()
            println("Select app setup:")
            appModes.forEachIndexed { idx, ctx ->
                println("${idx + 1}. $ctx")
            }
            println("0. Exit")
            print("Your choice: ")

            val choice = readInt(scanner)
            if (choice == 0) break@loop
            if (choice !in 1..appModes.size) {
                println("Invalid choice.")
                continue
            }

            val selectedContext = appModes[choice - 1]
            if (selectedContext.equals("Standard Mode")) {
                standardMode(scanner)
            } else if (selectedContext.equals("Benchmark Mode")) {
                benchmarkMode(scanner)
            }
            println()
            println("--------")
        }

        println("Goodbye!")
    }

    private fun standardMode(scanner: Scanner) {
        loop@ while (true) {
            println()
            println("Select context:")
            contextOptions.forEachIndexed { idx, ctx ->
                println("${idx + 1}. $ctx")
            }
            println("0. Exit")
            print("Your choice: ")

            val choice = readInt(scanner)
            if (choice == 0) break@loop
            if (choice !in 1..contextOptions.size) {
                println("Invalid choice.")
                continue
            }

            val selectedContext = contextOptions[choice - 1]
            var userPrompt = ""
            if (selectedContext.contains(SUB_MENU)) {

                if (selectedContext.contains(WORD)) {
                    print("Enter the word:")
                    val userInputWord = readLineSafe(scanner)

                    println()
                    print("Enter your Sentence for context:")
                    val contextSentence = readLineSafe(scanner)
                    println("Response")
                    println("--------")

                    userPrompt = contextOptions[choice - 1]
                        .replace(SUB_MENU, "")
                        .replace(WORD, userInputWord)
                        .plus(contextSentence)
                } else {
                    print("Enter the sentence:")
                    val userInputSentence = readLineSafe(scanner)

                    println()
                    println("Response")
                    println("--------")

                    userPrompt = contextOptions[choice - 1]
                        .replace(SUB_MENU, "")
                        .replace("[SENTENCE]", userInputSentence)
                }

            } else {
                print("Enter your prompt: ")
                userPrompt = readLineSafe(scanner)

                println()
                println("Response")
                println("--------")
            }

            try {
                streamChat(selectedContext, userPrompt, "")
            } catch (ex: Exception) {
                System.err.println("Error: ${ex.message}")
            }

            println()
            println("--------")
        }
    }

    private fun benchmarkMode(scanner: Scanner) {
        loop@ while (true) {
            println()
            println("Select context:")
            contextOptions.forEachIndexed { idx, ctx ->
                println("${idx + 1}. $ctx")
            }
            println("0. Exit")
            print("Your choice: ")

            val choice = readInt(scanner)
            if (choice == 0) break@loop
            if (choice !in 1..contextOptions.size) {
                println("Invalid choice.")
                continue
            }

            val selectedContext = contextOptions[choice - 1]
            var userPrompt = ""
            if (selectedContext.contains(SUB_MENU)) {

                if (selectedContext.contains(WORD)) {
                    print("Enter the word:")
                    val userInputWord = readLineSafe(scanner)

                    println()
                    print("Enter your Sentence for context:")
                    val contextSentence = readLineSafe(scanner)
                    println("Response")
                    println("--------")

                    userPrompt = contextOptions[choice - 1]
                        .replace(SUB_MENU, "")
                        .replace(WORD, userInputWord)
                        .plus(contextSentence)
                } else {
                    print("Enter the sentence:")
                    val userInputSentence = readLineSafe(scanner)

                    println()
                    println("Response")
                    println("--------")

                    userPrompt = contextOptions[choice - 1]
                        .replace(SUB_MENU, "")
                        .replace("[SENTENCE]", userInputSentence)
                }

            } else {
                print("Enter your prompt: ")
                userPrompt = readLineSafe(scanner)

                println()
                println("Response")
                println("--------")
            }

            val models = listOf(
                "meta-llama/Meta-Llama-3.1-8B-Instruct", // 0,02$ Winner - 3 times total of 15 times
                "gpt-5-nano",                            // 0,05$ Winner - 10 times total of 15 times
                "openai/gpt-oss-120b",                   // 0,08$ Winner - 10 times total of 15 times
                "Qwen/Qwen3-32B",                        // 0,09$ Winner - 8 times total of 15 times
                "route-llm"                              // 3,00$ Winner - 2 times total of 15 times

            )
            models.forEachIndexed { index, s ->
                println("Index: $index, s: $s")
                try {
                    streamChat(selectedContext, userPrompt, s)
                } catch (ex: Exception) {
                    System.err.println("Error: ${ex.message}")
                }
            }


            println()
            println("--------")
        }
    }

    private fun streamChat(context: String, userPrompt: String, model: String) {
        model.ifBlank { println("Model is Blank! Using props.model") }
        val payload = ChatRequest(
            model = model.ifBlank { props.model },
            messages = listOf(
                ChatMessage(role = "system", content = context),
                ChatMessage(role = "user", content = userPrompt)
            ),
            stream = true
        )
        val body = RequestBody.create(json, mapper.writeValueAsBytes(payload))

        val request = Request.Builder()
            .url(props.apiUrl)
            .addHeader("Authorization", "Bearer ${props.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response: Response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: ${response.body?.string()}")
            }
            println("Priting Response: $response")
            val source: BufferedSource = response.body?.source()
                ?: throw IllegalStateException("Empty response body")

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break

                // Parse chunk -> choices[0].delta.content
                runCatching {
                    val chunk: StreamChunk = mapper.readValue(data, StreamChunk::class.java)
                    val content = chunk.choices.firstOrNull()?.delta?.content
                    if (!content.isNullOrEmpty()) {
                        print(content)
                        System.out.flush()
                    }
                }
            }
            println()
        }
    }

    private fun readInt(scanner: Scanner): Int {
        while (true) {
            val token = scanner.nextLine().trim()
            val n = token.toIntOrNull()
            if (n != null) return n
            print("Please enter a number: ")
        }
    }

    private fun readLineSafe(scanner: Scanner): String {
        val line = scanner.nextLine()
        return if (line.isNotBlank()) line else readLineSafe(scanner)
    }
}