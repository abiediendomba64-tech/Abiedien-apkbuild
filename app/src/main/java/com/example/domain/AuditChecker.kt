package com.example.domain

import com.example.data.*

data class AuditCheckItem(
    val no: Int,
    val title: String,
    val countOrValue: String,
    val isOk: Boolean,
    val recommendation: String
)

object AuditChecker {

    fun evaluate(
        transaksiList: List<TransaksiKasBankRecord>,
        anggaranList: List<AnggaranProyek>,
        revisiList: List<RevisiAnggaranRecord>,
        intercompanyList: List<IntercompanyRecord>,
        pettyCashList: List<PettyCashAdvanceRecord>,
        lahanList: List<MasterLahan>,
        kontrakList: List<PenjualanUnitContract>,
        pembayaranList: List<PembayaranCustomerRecord>,
        userList: List<UserAppEntity>,
        proyekList: List<MasterProyek>,
        ptList: List<MasterPT>,
        journalReport: JournalReport
    ): List<AuditCheckItem> {
        val items = mutableListOf<AuditCheckItem>()

        // 1. Transaksi REJECTED
        val rejectedTx = transaksiList.count { it.statusSistem.startsWith("REJECTED") }
        items.add(
            AuditCheckItem(
                no = 1,
                title = "Transaksi Berstatus REJECTED",
                countOrValue = "$rejectedTx Transaksi",
                isOk = rejectedTx == 0,
                recommendation = "Perbaiki sumber transaksi atau batalkan transaksi yang ditolak."
            )
        )

        // 2. Transaksi PENDING APPROVAL
        val pendingApprTx = transaksiList.count { it.statusSistem.startsWith("PENDING_APPROVAL") }
        items.add(
            AuditCheckItem(
                no = 2,
                title = "Transaksi PENDING APPROVAL",
                countOrValue = "$pendingApprTx Transaksi",
                isOk = pendingApprTx == 0,
                recommendation = "Lengkapi persetujuan approver berwenang sesuai matriks kewenangan."
            )
        )

        // 3. Transaksi PENDING DOKUMEN
        val pendingDocTx = transaksiList.count { it.statusSistem.startsWith("PENDING_DOKUMEN") }
        items.add(
            AuditCheckItem(
                no = 3,
                title = "Transaksi PENDING DOKUMEN",
                countOrValue = "$pendingDocTx Transaksi",
                isOk = pendingDocTx == 0,
                recommendation = "Daftarkan dokumen pendukung (nota/kwitansi/SPK) ke Dokumen Registry hingga VALID."
            )
        )

        // 4. Pos Anggaran OVER BUDGET
        val overBudgetCount = anggaranList.count { it.statusBudget == "OVER BUDGET" }
        items.add(
            AuditCheckItem(
                no = 4,
                title = "Pos Anggaran OVER BUDGET",
                countOrValue = "$overBudgetCount Pos Anggaran",
                isOk = overBudgetCount == 0,
                recommendation = "Tahan pengeluaran pada pos terkait atau ajukan Revisi Anggaran resmi."
            )
        )

        // 5. Selisih Jurnal Otomatis (Debit - Kredit)
        val selisihJurnal = journalReport.selisih
        items.add(
            AuditCheckItem(
                no = 5,
                title = "Keseimbangan Jurnal Otomatis (Debit - Kredit)",
                countOrValue = "Rp $selisihJurnal",
                isOk = selisihJurnal == 0L,
                recommendation = "Periksa bagan akun (COA) dan transaksi berstatus ganjil."
            )
        )

        // 6. Revisi Anggaran Belum Disetujui / Invalid
        val invalidRevisi = revisiList.count { it.status != "APPROVED" }
        items.add(
            AuditCheckItem(
                no = 6,
                title = "Revisi Anggaran Belum Disetujui / Invalid",
                countOrValue = "$invalidRevisi Pengajuan",
                isOk = invalidRevisi == 0,
                recommendation = "Review pengajuan revisi anggaran oleh Direktur / Owner."
            )
        )

        // 7. Intercompany Belum Settle
        val openIntercompany = intercompanyList.count { it.status == "OUTSTANDING" || it.status == "PARTIAL" }
        items.add(
            AuditCheckItem(
                no = 7,
                title = "Piutang / Hutang Intercompany Belum Dilunasi",
                countOrValue = "$openIntercompany Catatan",
                isOk = openIntercompany == 0,
                recommendation = "Pastikan pencatatan settlement dua sisi antara PT Induk dan Anak PT."
            )
        )

        // 8. Petty Cash Open / Overdue
        val openPettyCash = pettyCashList.count { it.statusSettlement != "SETTLED" }
        items.add(
            AuditCheckItem(
                no = 8,
                title = "Petty Cash Belum Selesai (OPEN / OVERDUE)",
                countOrValue = "$openPettyCash Advance",
                isOk = openPettyCash == 0,
                recommendation = "Verifikasi nota belanja kas kecil. Advance baru untuk staf terkunci otomatis jika belum settle."
            )
        )

        // 9. Master Lahan Integritas
        val missingLahanDoc = lahanList.count { it.statusKwitansi == "BELUM" }
        items.add(
            AuditCheckItem(
                no = 9,
                title = "Pembebasan Lahan Tanpa Kwitansi Lunas",
                countOrValue = "$missingLahanDoc Bidang",
                isOk = missingLahanDoc == 0,
                recommendation = "Lengkapi bukti kwitansi fisik pembebasan tanah dan legalitas ke Notaris/PPAT."
            )
        )

        // 10. Penjualan Unit Keteraturan Kontrak
        val activeContracts = kontrakList.count { it.status == "ACTIVE" }
        items.add(
            AuditCheckItem(
                no = 10,
                title = "Kontrak Penjualan Aktif Berjalan",
                countOrValue = "$activeContracts Kontrak",
                isOk = true,
                recommendation = "Pantau jadwal jatuh tempo KPR dan pencairan termin bank."
            )
        )

        // 11. Approver Tersedia di Sistem
        val approverCount = userList.count { it.pangkat >= 1 && it.aktif }
        items.add(
            AuditCheckItem(
                no = 11,
                title = "Ketersediaan Pejabat Approver Terdaftar",
                countOrValue = "$approverCount Approver Aktif",
                isOk = approverCount >= 2,
                recommendation = "Pastikan Supervisor, Manager, Direktur dan Owner terdaftar dan aktif."
            )
        )

        // 12. Entity ID Proyek Terdaftar
        val projectsWithoutEntity = proyekList.count { it.entityId.isBlank() }
        items.add(
            AuditCheckItem(
                no = 12,
                title = "Proyek Tanpa Mapping Entity Legal (PT)",
                countOrValue = "$projectsWithoutEntity Proyek",
                isOk = projectsWithoutEntity == 0,
                recommendation = "Tautkan setiap proyek ke PT Induk atau Anak PT di Master Proyek."
            )
        )

        return items
    }
}
