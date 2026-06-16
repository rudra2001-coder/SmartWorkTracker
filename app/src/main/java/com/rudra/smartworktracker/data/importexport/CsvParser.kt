package com.rudra.smartworktracker.data.importexport

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {

    fun parse(inputStream: InputStream): List<Map<String, String>> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = mutableListOf<List<String>>()

        reader.lineSequence().forEach { rawLine ->
            val trimmed = rawLine.trim().trim('\uFEFF')
            if (trimmed.isNotEmpty()) {
                lines.add(parseCsvLine(trimmed))
            }
        }

        if (lines.size < 2) return emptyList()

        val headers = lines.first().map { it.trim() }
        return lines.drop(1).map { row ->
            val map = mutableMapOf<String, String>()
            headers.forEachIndexed { index, header ->
                map[header] = row.getOrElse(index) { "" }.trim()
            }
            map
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
