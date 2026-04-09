package com.kidsroutine.core.ai

import org.junit.Assert.*
import org.junit.Test

class AIProviderRegistryTest {

    // ── register ────────────────────────────────────────────────────

    @Test
    fun `initially no active provider`() {
        val registry = AIProviderRegistry()
        assertNull(registry.getActive())
    }

    @Test
    fun `register sets first provider as active`() {
        val registry = AIProviderRegistry()
        val provider = FakeProvider("gemini")
        registry.register("gemini", provider)
        assertNotNull(registry.getActive())
        assertEquals("gemini", registry.getActive()?.name)
    }

    @Test
    fun `register second provider does not change active`() {
        val registry = AIProviderRegistry()
        registry.register("gemini", FakeProvider("gemini"))
        registry.register("openai", FakeProvider("openai"))
        assertEquals("gemini", registry.getActive()?.name)
    }

    // ── setActive ───────────────────────────────────────────────────

    @Test
    fun `setActive returns true for registered provider`() {
        val registry = AIProviderRegistry()
        registry.register("gemini", FakeProvider("gemini"))
        registry.register("openai", FakeProvider("openai"))
        assertTrue(registry.setActive("openai"))
        assertEquals("openai", registry.getActive()?.name)
    }

    @Test
    fun `setActive returns false for unknown provider`() {
        val registry = AIProviderRegistry()
        assertFalse(registry.setActive("nonexistent"))
    }

    // ── get ─────────────────────────────────────────────────────────

    @Test
    fun `get returns registered provider by id`() {
        val registry = AIProviderRegistry()
        val provider = FakeProvider("gemini")
        registry.register("gemini", provider)
        assertSame(provider, registry.get("gemini"))
    }

    @Test
    fun `get returns null for unknown id`() {
        val registry = AIProviderRegistry()
        assertNull(registry.get("unknown"))
    }

    // ── listProviders ───────────────────────────────────────────────

    @Test
    fun `listProviders initially empty`() {
        val registry = AIProviderRegistry()
        assertTrue(registry.listProviders().isEmpty())
    }

    @Test
    fun `listProviders returns all registered`() {
        val registry = AIProviderRegistry()
        registry.register("a", FakeProvider("a"))
        registry.register("b", FakeProvider("b"))
        assertEquals(2, registry.listProviders().size)
        assertTrue(registry.listProviders().containsKey("a"))
        assertTrue(registry.listProviders().containsKey("b"))
    }

    @Test
    fun `listProviders returns copy, not internal reference`() {
        val registry = AIProviderRegistry()
        registry.register("a", FakeProvider("a"))
        val providers = registry.listProviders()
        registry.register("b", FakeProvider("b"))
        // Original snapshot should still have 1 entry
        assertEquals(1, providers.size)
    }

    // ── Fake provider for testing ───────────────────────────────────

    private class FakeProvider(override val name: String) : AIProvider {
        override suspend fun isConfigured(): Boolean = true
        override suspend fun generateText(
            prompt: String, maxTokens: Int, temperature: Float, systemPrompt: String?
        ): Result<String> = Result.success("fake")
        override fun streamText(
            prompt: String, maxTokens: Int, temperature: Float, systemPrompt: String?
        ) = kotlinx.coroutines.flow.flowOf(Result.success("fake"))
        override suspend fun validateResponse(text: String) = ValidationResult(isSafe = true)
    }
}
