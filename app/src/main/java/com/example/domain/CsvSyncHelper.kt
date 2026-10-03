package com.example.domain

import com.example.data.PayrollBuruhRecord

object CsvSyncHelper {

    fun exportPayrollToCsv(payrollList: List<PayrollBuruhRecord>): String {
        val sb = StringBuilder()
        sb.append("id,kode,nama,pekerjaan,jenis_gaji,gaji_per_hari,hari_kerja,total_gaji,tanggal,status,no_wa,catatan\n")
        payrollList.forEach { p ->
            val hkStr = if (p.hariKerja % 1.0 == 0.0) p.hariKerja.toInt().toString() else p.hariKerja.toString().replace('.', ',')
            val escapedCatatan = p.catatan.replace("\"", "\"\"")
            sb.append("${p.id},${p.kode},${p.nama},${p.pekerjaan},${p.jenisGaji},${p.gajiPerHari},\"$hkStr\",${p.totalGaji},${p.tanggal},${p.status},${p.noWa},\"$escapedCatatan\"\n")
        }
        return sb.toString()
    }

    fun parsePayrollFromCsv(csvText: String): List<PayrollBuruhRecord> {
        val lines = csvText.lines()
        val result = mutableListOf<PayrollBuruhRecord>()

        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            // Handle comma separated values taking into account potential quotes
            val tokens = parseCsvLine(line)
            if (tokens.size >= 10) {
                val id = tokens.getOrNull(0)?.toLongOrNull() ?: (System.currentTimeMillis() % 100000)
                val kode = tokens.getOrNull(1) ?: ""
                val nama = tokens.getOrNull(2) ?: ""
                val pekerjaan = tokens.getOrNull(3) ?: "Buruh Lapangan"
                val jenisGaji = tokens.getOrNull(4) ?: "Harian"
                val gajiPerHari = tokens.getOrNull(5)?.replace(".", "")?.toLongOrNull() ?: 120_000L
                val hariKerja = tokens.getOrNull(6)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                val totalGaji = tokens.getOrNull(7)?.replace(".", "")?.toLongOrNull() ?: (gajiPerHari * hariKerja).toLong()
                val tanggal = tokens.getOrNull(8)?.ifBlank { "2026-09-19" } ?: "2026-09-19"
                val status = tokens.getOrNull(9)?.ifBlank { "Pending" } ?: "Pending"
                val noWa = tokens.getOrNull(10) ?: ""
                val catatan = tokens.getOrNull(11) ?: ""

                result.add(
                    PayrollBuruhRecord(
                        id = id,
                        kode = kode,
                        nama = nama,
                        pekerjaan = pekerjaan,
                        jenisGaji = jenisGaji,
                        gajiPerHari = gajiPerHari,
                        hariKerja = hariKerja,
                        totalGaji = totalGaji,
                        tanggal = tanggal,
                        status = status,
                        noWa = noWa,
                        catatan = catatan
                    )
                )
            }
        }
        return result
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
