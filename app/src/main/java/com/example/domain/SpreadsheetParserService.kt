package com.example.domain

import com.example.data.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class SpreadsheetExportBundle(
    val fileName: String,
    val mimeType: String,
    val contentBytes: ByteArray,
    val rowCount: Int
)

data class SpreadsheetImportResult(
    val transactions: List<TransaksiKasBankRecord>,
    val budgets: List<AnggaranProyek>,
    val payrolls: List<PayrollBuruhRecord>,
    val units: List<MasterUnit>,
    val totalImported: Int,
    val errors: List<String>
)

object SpreadsheetParserService {

    // --- CSV EXPORT & IMPORT ---

    fun exportTransactionsToCsv(transactions: List<TransaksiKasBankRecord>): String {
        val sb = StringBuilder()
        // Include UTF-8 BOM for seamless display in Microsoft Excel on Windows/Android
        sb.append('\uFEFF')
        sb.append("ID Transaksi,Tanggal,Tipe,Rekening Bank,ID Proyek,Cost Code,Akun COA,Pihak/Vendor,Nominal,Keterangan,Dokumen Ref,Dibuat Oleh,Approver 1,Approver 2,Status Sistem\n")
        transactions.forEach { tx ->
            sb.append(escapeCsv(tx.id)).append(",")
            sb.append(escapeCsv(tx.tanggal)).append(",")
            sb.append(escapeCsv(tx.tipe)).append(",")
            sb.append(escapeCsv(tx.rekeningBank)).append(",")
            sb.append(escapeCsv(tx.projectId)).append(",")
            sb.append(escapeCsv(tx.costCode)).append(",")
            sb.append(escapeCsv(tx.akunEfektif)).append(",")
            sb.append(escapeCsv(tx.pihakNama)).append(",")
            sb.append(tx.nominal).append(",")
            sb.append(escapeCsv(tx.keterangan)).append(",")
            sb.append(escapeCsv(tx.docRef)).append(",")
            sb.append(escapeCsv(tx.dibuatOleh)).append(",")
            sb.append(escapeCsv(tx.disetujuiOleh1)).append(",")
            sb.append(escapeCsv(tx.disetujuiOleh2)).append(",")
            sb.append(escapeCsv(tx.statusSistem)).append("\n")
        }
        return sb.toString()
    }

    fun parseTransactionsFromCsv(csvText: String): List<TransaksiKasBankRecord> {
        val cleanText = csvText.removePrefix("\uFEFF")
        val lines = cleanText.lines()
        val result = mutableListOf<TransaksiKasBankRecord>()

        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            val tokens = parseCsvLine(line)
            if (tokens.size >= 9) {
                val id = tokens.getOrNull(0)?.ifBlank { "TX-${System.currentTimeMillis()}" } ?: "TX-${System.currentTimeMillis()}"
                val tanggal = tokens.getOrNull(1)?.ifBlank { "2026-10-03" } ?: "2026-10-03"
                val tipe = tokens.getOrNull(2)?.uppercase()?.ifBlank { "KELUAR" } ?: "KELUAR"
                val rekening = tokens.getOrNull(3)?.ifBlank { "BCA Operasional" } ?: "BCA Operasional"
                val prjId = tokens.getOrNull(4)?.ifBlank { "PRJ-001" } ?: "PRJ-001"
                val costCode = tokens.getOrNull(5) ?: ""
                val akun = tokens.getOrNull(6)?.ifBlank { "1410" } ?: "1410"
                val pihak = tokens.getOrNull(7)?.ifBlank { "Vendor / Mitra" } ?: "Vendor / Mitra"
                val nominal = tokens.getOrNull(8)?.replace(".", "")?.replace(",", "")?.toLongOrNull() ?: 0L
                val keterangan = tokens.getOrNull(9) ?: "Transaksi Import"
                val docRef = tokens.getOrNull(10) ?: ""
                val dibuat = tokens.getOrNull(11)?.ifBlank { "Operator" } ?: "Operator"
                val app1 = tokens.getOrNull(12)?.ifBlank { "H. Bambang Nugraha" } ?: "H. Bambang Nugraha"
                val app2 = tokens.getOrNull(13) ?: ""
                val status = tokens.getOrNull(14)?.ifBlank { "POSTED" } ?: "POSTED"

                result.add(
                    TransaksiKasBankRecord(
                        id = id,
                        tanggal = tanggal,
                        tipe = tipe,
                        rekeningBank = rekening,
                        projectId = prjId,
                        costCode = costCode,
                        akunEfektif = akun,
                        pihakId = "PHK-IMP",
                        pihakNama = pihak,
                        nominal = nominal,
                        keterangan = keterangan,
                        docRef = docRef,
                        dibuatOleh = dibuat,
                        disetujuiOleh1 = app1,
                        disetujuiOleh2 = app2,
                        statusInput = "SUBMITTED",
                        statusSistem = status,
                        isPosted = status == "POSTED"
                    )
                )
            }
        }
        return result
    }

    // --- XLSX WORKBOOK EXPORT (OpenXML Standard) ---

    fun exportTransactionsToXlsx(transactions: List<TransaksiKasBankRecord>): SpreadsheetExportBundle {
        val out = ByteArrayOutputStream()
        val zip = ZipOutputStream(out)

        // 1. [Content_Types].xml
        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
        writeZipFile(zip, "[Content_Types].xml", contentTypesXml)

        // 2. _rels/.rels
        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
        writeZipFile(zip, "_rels/.rels", relsXml)

        // 3. xl/_rels/workbook.xml.rels
        val wbRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
        writeZipFile(zip, "xl/_rels/workbook.xml.rels", wbRelsXml)

        // 4. xl/workbook.xml
        val wbXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Buku_Kas_Bank" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
        writeZipFile(zip, "xl/workbook.xml", wbXml)

        // 5. xl/worksheets/sheet1.xml
        val sheetBuilder = StringBuilder()
        sheetBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
""")

        // Header Row 1
        val headers = listOf("ID", "Tanggal", "Tipe", "Rekening", "Proyek", "CostCode", "Akun", "Pihak", "Nominal", "Keterangan", "Status")
        sheetBuilder.append("    <row r=\"1\">\n")
        headers.forEachIndexed { colIdx, text ->
            val colLetter = ('A' + colIdx).toString()
            sheetBuilder.append("      <c r=\"${colLetter}1\" t=\"inlineStr\"><is><t>${escapeXml(text)}</t></is></c>\n")
        }
        sheetBuilder.append("    </row>\n")

        // Data Rows
        transactions.forEachIndexed { rowIdx, tx ->
            val r = rowIdx + 2
            sheetBuilder.append("    <row r=\"$r\">\n")
            sheetBuilder.append("      <c r=\"A$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.id)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"B$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.tanggal)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"C$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.tipe)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"D$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.rekeningBank)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"E$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.projectId)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"F$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.costCode)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"G$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.akunEfektif)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"H$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.pihakNama)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"I$r\"><v>${tx.nominal}</v></c>\n")
            sheetBuilder.append("      <c r=\"J$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.keterangan)}</t></is></c>\n")
            sheetBuilder.append("      <c r=\"K$r\" t=\"inlineStr\"><is><t>${escapeXml(tx.statusSistem)}</t></is></c>\n")
            sheetBuilder.append("    </row>\n")
        }

        sheetBuilder.append("""  </sheetData>
</worksheet>""")
        writeZipFile(zip, "xl/worksheets/sheet1.xml", sheetBuilder.toString())

        zip.finish()
        zip.close()

        val bytes = out.toByteArray()
        return SpreadsheetExportBundle(
            fileName = "Laporan_Keuangan_Developer_${System.currentTimeMillis()}.xlsx",
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            contentBytes = bytes,
            rowCount = transactions.size
        )
    }

    private fun writeZipFile(zip: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zip.putNextEntry(entry)
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
