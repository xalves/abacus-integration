package abacus

data class ChatMessage(val role: String, val content: String)
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true
)

data class StreamChunk(
    val model: String,
    val created: Long,
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val delta: Delta,
    val finish_reason: String?,
    val logprobs: Any?
)

data class Delta(
    val content: String?,
    val role: String?
)
