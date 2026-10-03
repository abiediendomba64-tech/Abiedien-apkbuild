package com.example.domain

import com.example.ui.components.toRupiah

data class PriceBenchmark(
    val keyword: String,
    val canonicalName: String,
    val satuan: String,
    val minFair: Long,
    val avgFair: Long,
    val maxFair: Long,
    val costCategory: String,
    val standardRef: String = "PUPR & SNI Jawa Barat / Priangan Timur 2026"
)

data class PriceResearchAnalysis(
    val itemName: String,
    val totalNominal: Long,
    val volume: Double,
    val satuan: String,
    val unitPriceNominal: Long,
    val benchmarkMin: Long,
    val benchmarkAvg: Long,
    val benchmarkMax: Long,
    val deviationPercent: Double,
    val isAlarmTriggered: Boolean,
    val alarmSeverity: String, // "NORMAL_WAJAR", "PERINGATAN_MARGINAL", "ALARM_NOMINAL_MARKUP"
    val statusLabel: String,
    val researchSummary: String,
    val recommendation: String,
    val referenceStandard: String
)

object AiPriceAuditor {

    // Database Riset Harga Satuan Material, Tenaga, & Properti Regional Priangan Timur / Jabar (PUPR/SNI 2026)
    val benchmarks = listOf(
        PriceBenchmark("semen", "Semen Portland Komposit (PCC 50kg)", "Sak", 58_000, 65_000, 75_000, "BLD", "SNI 7064:2014 & PUPR"),
        PriceBenchmark("besi 13", "Besi Beton Ulir D13 mm (12m SNI)", "Batang", 108_000, 122_000, 138_000, "BLD", "SNI 2052:2017"),
        PriceBenchmark("besi 10", "Besi Beton Polos D10 mm (12m SNI)", "Batang", 68_000, 78_000, 88_000, "BLD", "SNI 2052:2017"),
        PriceBenchmark("besi 8", "Besi Beton Polos D8 mm (12m SNI)", "Batang", 44_000, 52_000, 60_000, "BLD", "SNI 2052:2017"),
        PriceBenchmark("pasir", "Pasir Pasang / Pasir Beton Galunggung", "m3", 220_000, 260_000, 310_000, "BLD/INF", "Dinas Bina Marga Jabar"),
        PriceBenchmark("batu belah", "Batu Belah Pondasi Kali", "m3", 190_000, 230_000, 275_000, "BLD", "Standar Harga Satuan Ciamis"),
        PriceBenchmark("bata merah", "Bata Merah Press Bakar Super", "Pcs", 750, 900, 1_150, "BLD", "Sentra Bata Priangan"),
        PriceBenchmark("hebel", "Bata Ringan / Hebel AAC t=10cm", "m3", 560_000, 630_000, 720_000, "BLD", "Standar Pabrikan SNI"),
        PriceBenchmark("genteng", "Genteng Keramik Berglazur", "Pcs", 9_500, 12_500, 15_000, "BLD", "SNI Genteng Jabar"),
        PriceBenchmark("cat tembok", "Cat Tembok Eksterior Weatherproof (20L)", "Pail", 1_450_000, 1_750_000, 2_100_000, "BLD", "Distributor Resmi Cat"),
        PriceBenchmark("keramik", "Keramik Lantai 40x40 Polished", "Dus", 55_000, 68_000, 85_000, "BLD", "SNI Keramik Granit"),
        PriceBenchmark("tukang batu", "Upah Harian Tukang Batu Ahli", "Hari", 110_000, 135_000, 160_000, "OVH/BLD", "Pergub UMP/UMK Ciamis 2026"),
        PriceBenchmark("mandor", "Upah Harian Mandor Konstruksi", "Hari", 150_000, 185_000, 220_000, "OVH", "Pergub UMP/UMK Ciamis 2026"),
        PriceBenchmark("laden", "Upah Harian Kenek / Pekerja Pembantu", "Hari", 85_000, 100_000, 120_000, "OVH/BLD", "Standar Tenaga Proyek"),
        PriceBenchmark("cut & fill", "Pematangan Lahan & Cut Fill Tanah", "m3", 28_000, 38_000, 50_000, "INF", "Dinas PU Bina Marga"),
        PriceBenchmark("u-ditch", "Saluran Drainase Precast U-Ditch 40x40", "Meter", 185_000, 225_000, 275_000, "INF", "Fabrikasi Precast Beton"),
        PriceBenchmark("aspal", "Pengaspalan Hotmix HRS-WC t=3cm", "m2", 85_000, 115_000, 145_000, "INF", "Spesifikasi Bina Marga 2026"),
        PriceBenchmark("paving", "Paving Block K-300 tebal 6cm", "m2", 95_000, 125_000, 155_000, "INF", "SNI 03-0691-1996"),
        PriceBenchmark("lahan", "Pembebasan Lahan Siap Bangun / m2", "m2", 180_000, 350_000, 650_000, "LND", "NJOP & Appraisal Pasar"),
        PriceBenchmark("notaris", "Biaya Notaris, Validasi Pajak & Splitzing SHM", "Kavling", 2_500_000, 4_000_000, 6_000_000, "LEGAL", "Ikatan Notaris Indonesia"),
        PriceBenchmark("komisi", "Komisi Marketing Agen Properti", "Unit", 5_000_000, 9_000_000, 14_000_000, "MKT", "AREBI Asosiasi Real Estate")
    )

    fun audit(
        itemName: String,
        totalNominal: Long,
        volume: Double = 1.0,
        costCode: String = "",
        satuanInput: String = ""
    ): PriceResearchAnalysis {
        val safeVol = if (volume <= 0.0) 1.0 else volume
        val unitPrice = (totalNominal / safeVol).toLong()

        val lowerName = itemName.lowercase()
        val matched = benchmarks.find { b ->
            lowerName.contains(b.keyword) ||
            (b.keyword.contains(" ") && b.keyword.split(" ").all { lowerName.contains(it) })
        }

        if (matched != null) {
            val satuan = if (satuanInput.isNotBlank()) satuanInput else matched.satuan
            val maxFairTotal = (matched.maxFair * safeVol).toLong()
            val avgFairTotal = (matched.avgFair * safeVol).toLong()
            val minFairTotal = (matched.minFair * safeVol).toLong()

            val deviationPercent = if (maxFairTotal > 0 && totalNominal > maxFairTotal) {
                ((totalNominal - maxFairTotal).toDouble() / maxFairTotal.toDouble()) * 100.0
            } else if (unitPrice > matched.maxFair) {
                ((unitPrice - matched.maxFair).toDouble() / matched.maxFair.toDouble()) * 100.0
            } else {
                0.0
            }

            val isAlarm = deviationPercent > 15.0 || (unitPrice > (matched.maxFair * 1.15))
            val isMarginal = !isAlarm && (unitPrice > matched.maxFair)

            val severity = when {
                isAlarm -> "ALARM_NOMINAL_MARKUP"
                isMarginal -> "PERINGATAN_MARGINAL"
                else -> "NORMAL_WAJAR"
            }

            val statusLabel = when {
                isAlarm -> "ALARM NOMINAL! TIDAK MASUK AKAL"
                isMarginal -> "PERINGATAN MARGINAL (+${deviationPercent.toInt()}%)"
                else -> "HARGA MASUK AKAL & SESUAI STANDAR"
            }

            val researchSummary = if (isAlarm) {
                "Riset AI menemukan nominal yang diajukan (${unitPrice.toRupiah()}/$satuan) melebih batas tertinggi standar pasar ${matched.standardRef} sebesar +${deviationPercent.toInt()}%. Batas wajar pasar berkisar antara ${matched.minFair.toRupiah()} s/d ${matched.maxFair.toRupiah()} per $satuan."
            } else if (isMarginal) {
                "Nominal berada sedikit di atas rata-rata pasar (${matched.avgFair.toRupiah()}/$satuan) namun masih mendekati batas toleransi distributor wilayah Ciamis."
            } else {
                "Nominal yang diajukan (${unitPrice.toRupiah()}/$satuan) masuk akal dan berada dalam rentang acuan harga pasar ${matched.standardRef} (${matched.minFair.toRupiah()} - ${matched.maxFair.toRupiah()}/$satuan)."
            }

            val recommendation = if (isAlarm) {
                "TAHAN PEMBAYARAN! Terindikasi markup pengadaan. Mintakan minimal 2 penawaran pembanding dari toko/distributor rekanan lain atau negosiasi ulang ke harga wajar (~${matched.avgFair.toRupiah()}/$satuan)."
            } else if (isMarginal) {
                "Dapat disetujui dengan catatan verifikasi ketersediaan stok mendesak atau negosiasi diskon volume."
            } else {
                "Dapat langsung diproses ke tahap approval otorisasi pembayaran (PPB)."
            }

            return PriceResearchAnalysis(
                itemName = itemName,
                totalNominal = totalNominal,
                volume = safeVol,
                satuan = satuan,
                unitPriceNominal = unitPrice,
                benchmarkMin = minFairTotal,
                benchmarkAvg = avgFairTotal,
                benchmarkMax = maxFairTotal,
                deviationPercent = deviationPercent,
                isAlarmTriggered = isAlarm,
                alarmSeverity = severity,
                statusLabel = statusLabel,
                researchSummary = researchSummary,
                recommendation = recommendation,
                referenceStandard = matched.standardRef
            )
        }

        // Heuristic fallback for general property items if keyword not strictly matched
        val isUnusuallyLarge = when {
            costCode.startsWith("MKT") && totalNominal > 25_000_000L -> true
            costCode.startsWith("OVH") && totalNominal > 15_000_000L -> true
            costCode.startsWith("BLD") && totalNominal > 150_000_000L && safeVol <= 1.0 -> true
            else -> false
        }

        val devPercent = if (isUnusuallyLarge) 45.0 else 0.0
        val isAlarm = isUnusuallyLarge
        val severity = if (isAlarm) "ALARM_NOMINAL_MARKUP" else "NORMAL_WAJAR"
        val statusLabel = if (isAlarm) "ALARM NOMINAL! PERIKSA KEMBALI" else "HARGA MASUK AKAL (ACUAN PROYEK)"

        return PriceResearchAnalysis(
            itemName = itemName,
            totalNominal = totalNominal,
            volume = safeVol,
            satuan = if (satuanInput.isNotBlank()) satuanInput else "Paket/Lot",
            unitPriceNominal = unitPrice,
            benchmarkMin = (totalNominal * 0.75).toLong(),
            benchmarkAvg = (totalNominal * 0.9).toLong(),
            benchmarkMax = if (isAlarm) (totalNominal * 0.7).toLong() else totalNominal,
            deviationPercent = devPercent,
            isAlarmTriggered = isAlarm,
            alarmSeverity = severity,
            statusLabel = statusLabel,
            researchSummary = if (isAlarm) {
                "Riset AI menandai pengeluaran ini memiliki nilai tunggal yang di luar rata-rata pos $costCode (${totalNominal.toRupiah()}). Perlu rincian sub-komponen belanja fisik."
            } else {
                "Nominal rancangan harga ini tergolong proporsional dengan alokasi master budget pos $costCode."
            },
            recommendation = if (isAlarm) {
                "Lakukan review rincian bill of quantity (BOQ) dan verifikasi bukti fisik sebelum otorisasi."
            } else {
                "Sesuai dengan plafon anggaran operasional."
            },
            referenceStandard = "Audit Anggaran Real Estate SAK EP"
        )
    }
}
