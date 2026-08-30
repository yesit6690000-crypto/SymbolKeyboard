package com.example.symbolkeyboard

object SymbolCipher {

    val letterToSymbol: Map<Char, String> = mapOf(
        'a' to "ζ", 'b' to "η", 'c' to "θ", 'd' to "ι", 'e' to "κ",
        'f' to "λ", 'g' to "μ", 'h' to "ν", 'i' to "ξ", 'j' to "ο",
        'k' to "π", 'l' to "ρ", 'm' to "σ", 'n' to "τ", 'o' to "υ",
        'p' to "φ", 'q' to "χ", 'r' to "ψ", 's' to "ω", 't' to "α",
        'u' to "β", 'v' to "γ", 'w' to "δ", 'x' to "ε", 'y' to "Ж",
        'z' to "Ѳ"
    )

    val digitToSymbol: Map<Char, String> = mapOf(
        '0' to "⓪", '1' to "①", '2' to "②", '3' to "③", '4' to "④",
        '5' to "⑤", '6' to "⑥", '7' to "⑦", '8' to "⑧", '9' to "⑨"
    )

    val specialToSymbol: Map<Char, String> = mapOf(
        '.' to "⁘", ',' to "⁏", '!' to "‼", '?' to "⁇", '-' to "—",
        '\'' to "ʹ", '"' to "″", ':' to "⁚", ';' to "⁝",
        '(' to "﹙", ')' to "﹚", '/' to "⁄", '@' to "﹫", '_' to "‗",
        '#' to "﹟", '&' to "﹠", '+' to "﹢", '~' to "〜", '*' to "⁎"
    )

    private val symbolToLetter: Map<String, Char> =
        letterToSymbol.entries.associate { (k, v) -> v to k }
    private val symbolToDigit: Map<String, Char> =
        digitToSymbol.entries.associate { (k, v) -> v to k }
    private val symbolToSpecial: Map<String, Char> =
        specialToSymbol.entries.associate { (k, v) -> v to k }

    /** Encodes one input char (letter/digit/punct) into its display symbol. */
    fun encodeChar(c: Char): String {
        val lower = c.lowercaseChar()
        return when {
            letterToSymbol.containsKey(lower) -> {
                val sym = letterToSymbol.getValue(lower)
                if (c.isUpperCase()) sym.uppercase() else sym
            }
            digitToSymbol.containsKey(c) -> digitToSymbol.getValue(c)
            specialToSymbol.containsKey(c) -> specialToSymbol.getValue(c)
            else -> c.toString() // space, newline, anything unmapped passes through
        }
    }

    fun encode(text: String): String = text.map { encodeChar(it) }.joinToString("")

    /** Decodes a symbol string back into plain English. */
    fun decode(text: String): String {
        val sb = StringBuilder()
        for (c in text) {
            val cStr = c.toString()
            val lower = cStr.lowercase()
            when {
                symbolToLetter.containsKey(lower) -> {
                    val letter = symbolToLetter.getValue(lower)
                    sb.append(if (cStr != lower) letter.uppercaseChar() else letter)
                }
                symbolToDigit.containsKey(cStr) -> sb.append(symbolToDigit.getValue(cStr))
                symbolToSpecial.containsKey(cStr) -> sb.append(symbolToSpecial.getValue(cStr))
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }
}
