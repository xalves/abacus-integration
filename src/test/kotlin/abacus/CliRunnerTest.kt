package abacus

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

class CliRunnerTest {
    @Test
    fun `should require API key`() {
        val props = RouteLlmProperties(apiKey = null)
        val runner = CliRunner(props)

        assertThrows<IllegalArgumentException> {
            runner.run()
        }
    }

}