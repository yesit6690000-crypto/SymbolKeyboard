package com.example.symbolkeyboard

/**
 * Central place that defines your "code language".
 * Change the symbols on the right to invent your own cipher —
 * everything else in the app (keys + decode strip) reads from this map.
 */
object SymbolCipher {

    // letter -> symbol (what gets typed into the text field)
    val letterToSymbol: Map<Char, String> = mapOf(
    'a' to "ζ", 'b' to "η", 'c' to "θ", 'd' to "ι", 'e' to "κ",
    'f' to "λ", 'g' to "μ", 'h' to "ν", 'i' to "ξ", 'j' to "ο",
    'k' to "π", 'l' to "ρ", 'm' to "σ", 'n' to "τ", 'o' to "υ",
    'p' to "φ", 'q' to "χ", 'r' to "ψ", 's' to "ω", 't' to "α",
    'u' to "β", 'v' to "γ", 'w' to "δ", 'x' to "ε", 'y' to "Ж",
    'z' to "Ѳ"
)

    // reverse map, built automatically: symbol -> letter
    val symbolToLetter: Map<String, Char> =
        letterToSymbol.entries.associate { (letter, symbol) -> symbol to letter }

    val rows: List<String> = listOf(
        "qwertyuiop",
        "asdfghjkl",
        "zxcvbnm"
    )

    fun symbolFor(letter: Char): String =
        letterToSymbol[letter.lowercaseChar()] ?: letter.toString()

    /**
     * Decode a string of committed symbols back into plain English.
     * Anything not in the map (spaces, punctuation, digits) passes through unchanged.
     */
    fun decode(text: CharSequence): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            var matched = false
            // try 1-char symbols (all of ours are 1 char, but this stays safe for multi-char symbols too)
            for (len in 2 downTo 1) {
                if (i + len <= text.length) {
                    val chunk = text.substring(i, i + len)
                    val letter = symbolToLetter[chunk]
                    if (letter != null) {
                        sb.append(letter)
                        i += len
                        matched = true
                        break
                    }
                }
            }
            if (!matched) {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }
}
