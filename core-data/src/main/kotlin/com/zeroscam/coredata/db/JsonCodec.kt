package com.zeroscam.coredata.db

/**
 * Minimal JSON codec for List<String>.
 *
 * Why not org.json?
 * - In local JVM unit tests (testDebugUnitTest), Android framework JSON
 * classes may not be available depending on the runner/classpath.
 * - This implementation keeps core-data tests and CI stable.
 */
object JsonCodec {
    fun toJsonArrayString(values: List<String>): String {
        if (values.isEmpty()) return "[]"

        return buildString {
            append('[')
            values.forEachIndexed { index, value ->
                if (index > 0) append(',')
                append('"')
                append(escapeJsonString(value))
                append('"')
            }
            append(']')
        }
    }

    fun fromJsonArrayString(json: String): List<String> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        require(trimmed.first() == '[' && trimmed.last() == ']') {
            "Expected JSON array"
        }

        val body = trimmed.substring(1, trimmed.length - 1).trim()
        if (body.isEmpty()) return emptyList()

        return parseStringArrayBody(body)
    }

    private fun escapeJsonString(value: String): String {
        if (value.isEmpty()) return ""

        return buildString(value.length) {
            for (ch in value) {
                when (ch) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\t' -> append("\\t")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\u000C' -> append("\\f")
                    else -> append(ch)
                }
            }
        }
    }

    private fun parseStringArrayBody(body: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0

        fun skipWhitespace() {
            while (i < body.length && body[i].isWhitespace()) i += 1
        }

        while (i < body.length) {
            skipWhitespace()
            require(i < body.length && body[i] == '"') {
                "Expected string at position $i"
            }
            i += 1 // opening quote

            val sb = StringBuilder()
            while (i < body.length) {
                val ch = body[i]
                when (ch) {
                    '"' -> {
                        i += 1
                        break
                    }
                    '\\' -> {
                        i += 1
                        require(i < body.length) { "Invalid escape at end of input" }
                        val esc = body[i]
                        when (esc) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                require(i + 4 < body.length) { "Invalid unicode escape" }
                                val hex = body.substring(i + 1, i + 5)
                                sb.append(hex.toInt(16).toChar())
                                i += 4
                            }
                            else -> error("Unsupported escape: \\$esc")
                        }
                        i += 1
                    }
                    else -> {
                        sb.append(ch)
                        i += 1
                    }
                }
            }
            out.add(sb.toString())

            skipWhitespace()
            if (i >= body.length) break
            require(body[i] == ',') { "Expected ',' at position $i" }
            i += 1
        }

        return out
    }
}
