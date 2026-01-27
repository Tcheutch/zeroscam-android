package com.zeroscam.coredata.db

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonCodecTest {
    @Test
    fun `encode then decode - preserves values`() {
        val values = listOf("a", "b", "c")
        val json = JsonCodec.toJsonArrayString(values)
        val decoded = JsonCodec.fromJsonArrayString(json)
        assertEquals(values, decoded)
    }

    @Test
    fun `decode blank - returns empty`() {
        val decoded = JsonCodec.fromJsonArrayString("")
        assertEquals(emptyList<String>(), decoded)
    }
}
