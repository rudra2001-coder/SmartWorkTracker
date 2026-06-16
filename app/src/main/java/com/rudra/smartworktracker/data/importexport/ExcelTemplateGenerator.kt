package com.rudra.smartworktracker.data.importexport

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelTemplateGenerator {

    fun generate(template: ImportTemplate, outputStream: OutputStream) {
        val headers = template.headers
        val exampleValues = headers.map { template.exampleRow[it] ?: "" }
        val fieldData = headers.map { header ->
            listOf(
                header,
                if (header in template.requiredFields) "Yes" else "No",
                template.fieldDescriptions[header] ?: "",
                template.exampleRow[header] ?: ""
            )
        }

        val ss = SharedStringsBuilder()
        val sheet1Data = buildSheet1(ss, headers, exampleValues)
        val sheet2Data = buildSheet2(ss, fieldData)

        ZipOutputStream(outputStream).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            zip.write(COMPRESSED_CONTENT_TYPES.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            zip.write(COMPRESSED_RELS.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            zip.write(COMPRESSED_WORKBOOK.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            zip.write(COMPRESSED_WORKBOOK_RELS.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/styles.xml"))
            zip.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            zip.write(COMPRESSED_STYLES.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zip.write(buildSharedStringsXml(ss.strings).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(buildSheetXml(sheet1Data).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
            zip.write(buildSheetXml(sheet2Data).toByteArray())
            zip.closeEntry()
        }
    }

    private fun buildSharedStringsXml(strings: List<String>): String {
        val sb = StringBuilder()
        sb.append(XML_HEADER)
        sb.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"${strings.size}\" uniqueCount=\"${strings.size}\">")
        for (s in strings) {
            sb.append("<si><t>${xmlEscape(s)}</t></si>")
        }
        sb.append("</sst>")
        return sb.toString()
    }

    private fun buildSheetXml(data: List<List<Cell>>): String {
        val sb = StringBuilder()
        sb.append(XML_HEADER)
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        sb.append("<sheetViews><sheetView tabSelected=\"1\" workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>")
        sb.append("<cols>")
        data.firstOrNull()?.forEachIndexed { idx, _ ->
            sb.append("<col min=\"${idx + 1}\" max=\"${idx + 1}\" width=\"${colWidth(idx)}\" customWidth=\"1\"/>")
        }
        sb.append("</cols>")
        sb.append("<sheetData>")
        data.forEachIndexed { rowIdx, row ->
            sb.append("<row r=\"${rowIdx + 1}\">")
            row.forEach { cell ->
                sb.append("<c r=\"${cell.ref}\" t=\"s\"")
                if (rowIdx == 0) sb.append(" s=\"1\"")
                sb.append("><v>${cell.ssIndex}</v></c>")
            }
            sb.append("</row>")
        }
        sb.append("</sheetData>")
        sb.append("</worksheet>")
        return sb.toString()
    }

    private fun buildSheet1(
        ss: SharedStringsBuilder,
        headers: List<String>,
        exampleValues: List<String>
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>()
        rows.add(headers.mapIndexed { idx, h -> Cell(colLetter(idx), ss.add(h)) })
        rows.add(exampleValues.mapIndexed { idx, v -> Cell(colLetter(idx), ss.add(v)) })
        return rows
    }

    private fun buildSheet2(ss: SharedStringsBuilder, fieldData: List<List<String>>): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>()
        val headerRow = listOf("Field", "Required", "Description", "Example")
        rows.add(headerRow.mapIndexed { idx, h -> Cell(colLetter(idx), ss.add(h)) })
        for (row in fieldData) {
            rows.add(row.mapIndexed { idx, v -> Cell(colLetter(idx), ss.add(v)) })
        }
        return rows
    }

    private fun colWidth(idx: Int): Int = when (idx) {
        0 -> 22; 1 -> 12; 2 -> 55; 3 -> 28; else -> 20
    }

    private fun colLetter(index: Int): String {
        val sb = StringBuilder()
        var i = index
        while (i >= 0) {
            sb.append('A' + (i % 26))
            i = i / 26 - 1
        }
        return sb.reverse().toString()
    }

    private fun xmlEscape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    data class Cell(val ref: String, val ssIndex: Int)

    class SharedStringsBuilder {
        private val _strings = mutableListOf<String>()
        private val indexMap = mutableMapOf<String, Int>()
        val strings: List<String> get() = _strings

        fun add(value: String): Int {
            indexMap[value]?.let { return it }
            val idx = _strings.size
            _strings.add(value)
            indexMap[value] = idx
            return idx
        }
    }

    private const val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"

    private val COMPRESSED_CONTENT_TYPES = """
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""".trimIndent()

        private val COMPRESSED_RELS = """
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent()

        private val COMPRESSED_WORKBOOK = """
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Template" sheetId="1" r:id="rId1"/>
    <sheet name="Instructions" sheetId="2" r:id="rId2"/>
  </sheets>
</workbook>""".trimIndent()

        private val COMPRESSED_WORKBOOK_RELS = """
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""".trimIndent()

        private val COMPRESSED_STYLES = """
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
  </fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
  </fills>
  <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
  </cellXfs>
  <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
</styleSheet>""".trimIndent()
}
