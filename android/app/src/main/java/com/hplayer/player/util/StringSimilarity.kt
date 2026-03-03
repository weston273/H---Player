package com.hplayer.player.util

import kotlin.math.max

object StringSimilarity {
    fun normalizedLevenshtein(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        val distance = levenshtein(a.lowercase(), b.lowercase())
        val length = max(a.length, b.length).coerceAtLeast(1)
        return 1.0 - (distance.toDouble() / length.toDouble())
    }

    private fun levenshtein(lhs: String, rhs: String): Int {
        val costs = IntArray(rhs.length + 1) { it }
        for (i in 1..lhs.length) {
            var last = i - 1
            costs[0] = i
            for (j in 1..rhs.length) {
                val old = costs[j]
                val replace = if (lhs[i - 1] == rhs[j - 1]) last else last + 1
                val insert = costs[j] + 1
                val delete = costs[j - 1] + 1
                costs[j] = minOf(replace, insert, delete)
                last = old
            }
        }
        return costs[rhs.length]
    }
}
