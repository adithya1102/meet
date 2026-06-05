package com.example.lib.utils

object ValidationUtils {

    // Verhoeff Algorithm Tables
    private val d = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
        intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
        intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
        intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
        intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
        intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
        intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
        intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
        intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
    )

    private val p = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
    )

    private val inv = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

    /**
     * Validates an Aadhaar number using the Verhoeff algorithm.
     * Must be exactly 12 digits.
     */
    fun validateAadhaar(aadhaar: String): Boolean {
        if (aadhaar.length != 12 || !aadhaar.all { it.isDigit() }) {
            return false
        }
        var c = 0
        val digits = aadhaar.map { it.toString().toInt() }
        for (i in digits.indices) {
            val index = (digits.size - 1 - i)
            c = d[c][p[i % 8][digits[index]]]
        }
        return c == 0
    }

    /**
     * Performs a simple fuzzy match (Levenshtein distance) on names to verify Aadhaar match.
     */
    fun areNamesFuzzyMatching(name1: String, name2: String): Boolean {
        val n1 = name1.trim().lowercase().replace(Regex("\\s+"), " ")
        val n2 = name2.trim().lowercase().replace(Regex("\\s+"), " ")
        if (n1 == n2) return true

        // Just check if major substrings or tokens exist
        val tokens1 = n1.split(" ").filter { it.length > 1 }
        val tokens2 = n2.split(" ").filter { it.length > 1 }
        
        var matches = 0
        for (t1 in tokens1) {
            if (tokens2.any { it.contains(t1) || t1.contains(it) }) {
                matches++
            }
        }
        return matches > 0 || levenshteinDistance(n1, n2) <= 3
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                if (s1[i - 1] == s2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,    // deletion
                        dp[i][j - 1] + 1,    // insertion
                        dp[i - 1][j - 1] + 1 // substitution
                    )
                }
            }
        }
        return dp[s1.length][s2.length]
    }
}
