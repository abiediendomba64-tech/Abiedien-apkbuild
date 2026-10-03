package com.example.domain

object TerbilangHelper {
    private val satuan = arrayOf(
        "", "Satu", "Dua", "Tiga", "Empat", "Lima",
        "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"
    )

    fun konversi(nilai: Long): String {
        return when {
            nilai < 0 -> "Minus " + konversi(-nilai)
            nilai == 0L -> "Nol Rupiah"
            nilai < 12 -> "${satuan[nilai.toInt()]} Rupiah"
            nilai < 20 -> "${satuan[(nilai - 10).toInt()]} Belas Rupiah"
            nilai < 100 -> "${satuan[(nilai / 10).toInt()]} Puluh ${konversiRaw(nilai % 10)} Rupiah"
            nilai < 200 -> "Seratus ${konversiRaw(nilai - 100)} Rupiah"
            nilai < 1000 -> "${satuan[(nilai / 100).toInt()]} Ratus ${konversiRaw(nilai % 100)} Rupiah"
            nilai < 2000 -> "Seribu ${konversiRaw(nilai - 1000)} Rupiah"
            nilai < 1_000_000 -> "${konversiRaw(nilai / 1000)} Ribu ${konversiRaw(nilai % 1000)} Rupiah"
            nilai < 1_000_000_000 -> "${konversiRaw(nilai / 1_000_000)} Juta ${konversiRaw(nilai % 1_000_000)} Rupiah"
            nilai < 1_000_000_000_000L -> "${konversiRaw(nilai / 1_000_000_000)} Miliar ${konversiRaw(nilai % 1_000_000_000)} Rupiah"
            else -> "${konversiRaw(nilai / 1_000_000_000_000L)} Triliun ${konversiRaw(nilai % 1_000_000_000_000L)} Rupiah"
        }.replace("\\s+".toRegex(), " ").trim()
    }

    private fun konversiRaw(nilai: Long): String {
        return when {
            nilai == 0L -> ""
            nilai < 12 -> satuan[nilai.toInt()]
            nilai < 20 -> "${satuan[(nilai - 10).toInt()]} Belas"
            nilai < 100 -> "${satuan[(nilai / 10).toInt()]} Puluh ${konversiRaw(nilai % 10)}"
            nilai < 200 -> "Seratus ${konversiRaw(nilai - 100)}"
            nilai < 1000 -> "${satuan[(nilai / 100).toInt()]} Ratus ${konversiRaw(nilai % 100)}"
            nilai < 2000 -> "Seribu ${konversiRaw(nilai - 1000)}"
            nilai < 1_000_000 -> "${konversiRaw(nilai / 1000)} Ribu ${konversiRaw(nilai % 1000)}"
            nilai < 1_000_000_000 -> "${konversiRaw(nilai / 1_000_000)} Juta ${konversiRaw(nilai % 1_000_000)}"
            nilai < 1_000_000_000_000L -> "${konversiRaw(nilai / 1_000_000_000)} Miliar ${konversiRaw(nilai % 1_000_000_000)}"
            else -> "${konversiRaw(nilai / 1_000_000_000_000L)} Triliun ${konversiRaw(nilai % 1_000_000_000_000L)}"
        }.trim()
    }
}
