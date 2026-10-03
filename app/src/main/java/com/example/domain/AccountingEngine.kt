package com.example.domain

import com.example.data.CoaAccount
import com.example.data.TransaksiKasBankRecord

data class JournalLine(
    val barisId: String,
    val refTxId: String,
    val tanggal: String,
    val sisi: String, // "D" or "K"
    val kodeAkun: String,
    val namaAkun: String,
    val debit: Long,
    val kredit: Long,
    val keterangan: String
)

data class JournalReport(
    val entries: List<JournalLine>,
    val totalDebit: Long,
    val totalKredit: Long,
    val isBalanced: Boolean,
    val selisih: Long
)

object AccountingEngine {

    fun generateAutomaticJournal(
        postedTransactions: List<TransaksiKasBankRecord>,
        coaList: List<CoaAccount>
    ): JournalReport {
        val coaMap = coaList.associateBy { it.kodeAkun }
        val lines = mutableListOf<JournalLine>()

        var totalD = 0L
        var totalK = 0L

        postedTransactions.forEach { tx ->
            val bankAccountCode = when (tx.rekeningBank) {
                "BCA Operasional" -> "1110"
                "BCA Proyek" -> "1120"
                "Kas Kecil" -> "1130"
                else -> "1110"
            }
            val bankAccountName = coaMap[bankAccountCode]?.namaAkun ?: "Kas & Bank"
            val effectiveAccountCode = if (tx.akunEfektif.isNotBlank()) tx.akunEfektif else "6100"
            val effectiveAccountName = coaMap[effectiveAccountCode]?.namaAkun ?: "Akun Terkait ($effectiveAccountCode)"

            if (tx.tipe == "MASUK") {
                // Debit: Bank, Credit: Effective Account
                lines.add(
                    JournalLine(
                        barisId = "JV-${tx.id}-D",
                        refTxId = tx.id,
                        tanggal = tx.tanggal,
                        sisi = "D",
                        kodeAkun = bankAccountCode,
                        namaAkun = bankAccountName,
                        debit = tx.nominal,
                        kredit = 0L,
                        keterangan = tx.keterangan
                    )
                )
                lines.add(
                    JournalLine(
                        barisId = "JV-${tx.id}-K",
                        refTxId = tx.id,
                        tanggal = tx.tanggal,
                        sisi = "K",
                        kodeAkun = effectiveAccountCode,
                        namaAkun = effectiveAccountName,
                        debit = 0L,
                        kredit = tx.nominal,
                        keterangan = tx.keterangan
                    )
                )
                totalD += tx.nominal
                totalK += tx.nominal
            } else {
                // Debit: Effective Account (WIP, Beban, Piutang IC), Credit: Bank
                lines.add(
                    JournalLine(
                        barisId = "JV-${tx.id}-D",
                        refTxId = tx.id,
                        tanggal = tx.tanggal,
                        sisi = "D",
                        kodeAkun = effectiveAccountCode,
                        namaAkun = effectiveAccountName,
                        debit = tx.nominal,
                        kredit = 0L,
                        keterangan = tx.keterangan
                    )
                )
                lines.add(
                    JournalLine(
                        barisId = "JV-${tx.id}-K",
                        refTxId = tx.id,
                        tanggal = tx.tanggal,
                        sisi = "K",
                        kodeAkun = bankAccountCode,
                        namaAkun = bankAccountName,
                        debit = 0L,
                        kredit = tx.nominal,
                        keterangan = tx.keterangan
                    )
                )
                totalD += tx.nominal
                totalK += tx.nominal
            }
        }

        return JournalReport(
            entries = lines,
            totalDebit = totalD,
            totalKredit = totalK,
            isBalanced = totalD == totalK,
            selisih = totalD - totalK
        )
    }
}
