package com.example.domain

import com.example.data.AnggaranProyek
import com.example.data.TransaksiKasBankRecord
import com.example.ui.components.toRupiah

data class ExpenseAnomaly(
    val txId: String,
    val costCode: String,
    val itemDescription: String,
    val nominal: Long,
    val anomalyType: AnomalyType,
    val severity: AnomalySeverity,
    val explanation: String,
    val aiForensicAudit: String,
    val recommendedAction: String
)

enum class AnomalyType {
    CATEGORY_MISMATCH,       // Salah alokasi kategori pos RAB (misal semen di pos Overhead)
    BUDGET_OVERRUN,          // Melebihi plafon sisa anggaran RAB proyek
    STRUCTURING_SPLIT,       // Indikasi pemecahan nota/faktur di bawah plafon approval Direksi (< 10jt)
    ABNORMAL_UNIT_PRICE,     // Harga satuan tidak wajar / melenceng jauh dari standar pasar
    OFF_SCHEDULE_SPIKE       // Lonjakan pengeluaran tanpa milestone fisik BAST yang jelas
}

enum class AnomalySeverity {
    CRITICAL, // Merah: Tahan pembayaran & butuh audit investigasi
    WARNING,  // Kuning: Perlu klarifikasi pengawas lapangan
    SAFE      // Hijau: Sesuai pos anggaran & standar kewajaran
}

data class AnomalyAuditReport(
    val totalAnalyzed: Int,
    val anomaliesCount: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val items: List<ExpenseAnomaly>,
    val auditSummary: String
)

object AiExpenseAnomalyAuditor {

    // Predefined RAB category rules
    private val rabCategoryRules = mapOf(
        "LND" to listOf("tanah", "lahan", "notaris", "ajb", "shm", "bphtb", "pph final", "patok", "sertifikat"),
        "INF" to listOf("jalan", "aspal", "drainase", "u-ditch", "saluran", "cut", "fill", "pematangan", "paving", "gorong", "kavling"),
        "BLD" to listOf("semen", "besi", "pasir", "batu", "bata", "hebel", "genteng", "cat", "keramik", "pondasi", "atap", "plester", "kusen"),
        "MKT" to listOf("komisi", "brosur", "iklan", "banner", "marketing", "facebook", "instagram", "pameran", "agen", "baliho"),
        "OVH" to listOf("gaji", "upah", "listrik", "air", "atk", "konsumsi", "operasional", "bensin", "keamanan", "kebersihan", "staf")
    )

    fun auditTransactions(
        transactions: List<TransaksiKasBankRecord>,
        anggaranList: List<AnggaranProyek>
    ): AnomalyAuditReport {
        val expenseTx = transactions.filter { it.tipe == "KELUAR" }
        val anomalies = mutableListOf<ExpenseAnomaly>()

        // 1. Check for Structuring / Split Invoicing (transactions under 10m on same day/pihak)
        val groupedByDatePihak = expenseTx.groupBy { "${it.tanggal}_${it.pihakId}" }
        groupedByDatePihak.forEach { (_, group) ->
            if (group.size >= 2 && group.all { it.nominal in 7_000_000L..9_999_999L }) {
                val totalSum = group.sumOf { it.nominal }
                group.forEach { tx ->
                    anomalies.add(
                        ExpenseAnomaly(
                            txId = tx.id,
                            costCode = tx.costCode,
                            itemDescription = tx.keterangan,
                            nominal = tx.nominal,
                            anomalyType = AnomalyType.STRUCTURING_SPLIT,
                            severity = AnomalySeverity.CRITICAL,
                            explanation = "Terindikasi Pemecahan Tagihan (Smurfing / Structuring): ${group.size} transaksi beruntun senilai total ${totalSum.toRupiah()} di bawah batas limit otorisasi Direksi Rp 10.000.000.",
                            aiForensicAudit = "Pola transaksi menunjukkan kesengajaan memecah tagihan pada tanggal yang sama ke pihak '${tx.pihakNama}' guna menghindari eskalasi persetujuan Direktur Utama.",
                            recommendedAction = "TAHAN PENCAIRAN! Satukan seluruh tagihan menjadi satu voucher PPB dan mintakan approval Direktur Utama."
                        )
                    )
                }
            }
        }

        // 2. Check for Category Mismatches & Budget Overrun
        expenseTx.forEach { tx ->
            if (anomalies.none { it.txId == tx.id }) {
                val prefix = tx.costCode.take(3).uppercase()
                val descLower = tx.keterangan.lowercase()

                // Check budget overrun
                val matchedBudget = anggaranList.find { it.projectId == tx.projectId && it.costCode == tx.costCode }
                if (matchedBudget != null && tx.nominal > matchedBudget.availableBudget) {
                    val excess = tx.nominal - matchedBudget.availableBudget
                    anomalies.add(
                        ExpenseAnomaly(
                            txId = tx.id,
                            costCode = tx.costCode,
                            itemDescription = tx.keterangan,
                            nominal = tx.nominal,
                            anomalyType = AnomalyType.BUDGET_OVERRUN,
                            severity = AnomalySeverity.CRITICAL,
                            explanation = "Overbudget Pos ${tx.costCode}: Nominal pengeluaran (${tx.nominal.toRupiah()}) melebihi sisa plafon anggaran tersedia (${matchedBudget.availableBudget.toRupiah()}) sebesar ${excess.toRupiah()}.",
                            aiForensicAudit = "Pelanggaran hard-stop batas anggaran SAK EP. Alokasi pos ${matchedBudget.costCodeNama} telah terserap habis atau mendekati 100%.",
                            recommendedAction = "Blokir pencairan dana sampai diajukan permohonan Revisi Anggaran (Addendum RAB) resmi yang disetujui Direksi."
                        )
                    )
                    return@forEach
                }

                // Check Category Mismatch
                var suspectedCategory = ""
                for ((cat, keywords) in rabCategoryRules) {
                    if (keywords.any { descLower.contains(it) }) {
                        suspectedCategory = cat
                        break
                    }
                }

                if (suspectedCategory.isNotBlank() && prefix in rabCategoryRules.keys && prefix != suspectedCategory) {
                    anomalies.add(
                        ExpenseAnomaly(
                            txId = tx.id,
                            costCode = tx.costCode,
                            itemDescription = tx.keterangan,
                            nominal = tx.nominal,
                            anomalyType = AnomalyType.CATEGORY_MISMATCH,
                            severity = AnomalySeverity.WARNING,
                            explanation = "Salah Alokasi Kategori RAB: Uraian '${tx.keterangan}' terdeteksi sebagai pos $suspectedCategory, namun dicatat ke pos ${tx.costCode} ($prefix).",
                            aiForensicAudit = "Distorsi pelaporan HPP konstruksi. Pengeluaran material/jasa tidak boleh dibebankan ke pos biaya yang keliru karena akan mengacaukan perhitungan laba rugi per unit.",
                            recommendedAction = "Lakukan jurnal koreksi atau ubah Cost Code ke kategori yang sesuai ($suspectedCategory) sebelum posting permanen."
                        )
                    )
                    return@forEach
                }

                // Check Abnormal Spike without breakdown
                if (prefix == "OVH" && tx.nominal > 20_000_000L && !descLower.contains("gaji")) {
                    anomalies.add(
                        ExpenseAnomaly(
                            txId = tx.id,
                            costCode = tx.costCode,
                            itemDescription = tx.keterangan,
                            nominal = tx.nominal,
                            anomalyType = AnomalyType.OFF_SCHEDULE_SPIKE,
                            severity = AnomalySeverity.WARNING,
                            explanation = "Lonjakan Nominal Pos Overhead (${tx.nominal.toRupiah()}): Nilai tunggal di luar pola operasional standar.",
                            aiForensicAudit = "Pengeluaran operasional non-gaji bernilai di atas Rp 20 juta wajib melampirkan rincian nota bill-of-quantities dan disetujui Manajer Keuangan.",
                            recommendedAction = "Minta rincian sub-biaya pengeluaran dan verifikasi kwitansi resmi sebelum otorisasi."
                        )
                    )
                }
            }
        }

        val criticalCount = anomalies.count { it.severity == AnomalySeverity.CRITICAL }
        val warningCount = anomalies.count { it.severity == AnomalySeverity.WARNING }

        val summary = when {
            criticalCount > 0 -> "DETEKSI ANOMALI KRITIS: Ditemukan $criticalCount transaksi yang melanggar plafon RAB atau terindikasi pemecahan tagihan. Tinjau sebelum pencairan dana kas!"
            warningCount > 0 -> "PERINGATAN ANOMALI: Ditemukan $warningCount transaksi dengan indikasi salah alokasi pos RAB atau lonjakan nominal."
            else -> "SELURUH PENGELUARAN AMAN: Seluruh pengeluaran konsisten dengan kategori RAB dan dalam batas toleransi anggaran."
        }

        return AnomalyAuditReport(
            totalAnalyzed = expenseTx.size,
            anomaliesCount = anomalies.size,
            criticalCount = criticalCount,
            warningCount = warningCount,
            items = anomalies,
            auditSummary = summary
        )
    }
}
