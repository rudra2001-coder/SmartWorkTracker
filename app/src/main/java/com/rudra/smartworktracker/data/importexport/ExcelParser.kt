package com.rudra.smartworktracker.data.importexport

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

object ExcelParser {

    fun parse(inputStream: InputStream): List<Map<String, String>> {
        val zip = ZipInputStream(inputStream)
        val sharedStrings = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()
        var entry = zip.nextEntry

        while (entry != null) {
            when {
                entry.name.equals("xl/sharedStrings.xml", ignoreCase = true) -> {
                    sharedStrings.addAll(parseSharedStrings(zip))
                }
                entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true) -> {
                    rows.addAll(parseSheet(zip, sharedStrings))
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        zip.close()

        if (rows.size < 2) return emptyList()

        val headers = rows.first().map { it.trim() }
        return rows.drop(1).map { row ->
            val map = mutableMapOf<String, String>()
            headers.forEachIndexed { index, header ->
                map[header] = row.getOrElse(index) { "" }.trim()
            }
            map
        }
    }

    private fun parseSharedStrings(inputStream: InputStream): List<String> {
        val strings = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var inText = false
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") inText = true
                }
                XmlPullParser.TEXT -> {
                    if (inText) strings.add(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") inText = false
                }
            }
            eventType = parser.next()
        }
        return strings
    }

    private fun parseSheet(inputStream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var currentRow = mutableListOf<String>()
        var inCell = false
        var cellType: String? = null
        var cellValue = StringBuilder()
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            inCell = true
                            cellValue = StringBuilder()
                        }
                        "v" -> { /* value tag */ }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inCell) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> { /* end of value */ }
                        "c" -> {
                            val value = cellValue.toString().trim()
                            currentRow.add(
                                if (cellType == "s" && value.isNotEmpty())
                                    sharedStrings.getOrElse(value.toIntOrNull() ?: -1) { value }
                                else value
                            )
                            inCell = false
                        }
                        "row" -> {
                            if (currentRow.isNotEmpty()) rows.add(currentRow.toList())
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }
}
