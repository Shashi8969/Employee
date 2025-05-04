package com.example.rrserviceadmin

import java.util.Locale

object StringHeandling {
    //Write here all string functions
    fun String?.toSentenceCase(): String? {
        return this?.let {
            it.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
    fun String?.removeNewlineBeforeSpace(): String? {
        return this?.replace("\n ", "")
    }
    fun String?.capitalizeFirstLetterOfNewlines(locale: Locale = Locale.getDefault()): String? {
        return this?.let {
            it.split("\n")
                .joinToString("\n") { line ->
                    if (line.isNotEmpty()) {
                        line.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                    } else {
                        line
                    }
                }
        }
    }
    fun String.removeAllSpaces(): String {
        return this.replace(" ", "")
    }
    fun String?.capitalizeSentencesRobust(): String? {
        return this?.let {
            split(Regex("[.?!]")) // Split by ". "! " or "? " (one or more spaces)
                .joinToString(". ") { sentence -> // Still join with ". " as the primary separator
                    val trimmedSentence = sentence.trim()
                    if (trimmedSentence.isNotEmpty()) {
                        trimmedSentence.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    } else {
                        trimmedSentence
                    }
                }
        }
    }
    fun String?.commaToNewlineAnySpace(): String? {
        return this?.replace(Regex(",\\s*"), "\n")
    }
    fun String?.commaToNewlineOutsideBrackets(): String? {
        return this?.let { input ->
            val result = StringBuilder()
            var bracketLevel = 0
            for (char in input) {
                when (char) {
                    '(' -> bracketLevel++
                    ')' -> if (bracketLevel > 0) bracketLevel-- // Decrement if inside brackets
                    ',' -> {
                        if (bracketLevel == 0) {
                            result.append('\n') // Replace with newline outside brackets
                            continue // Skip appending the comma
                        }
                    }
                }
                result.append(char) // Append the current character
            }
            result.toString()
        }
    }
}