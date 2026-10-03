package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val dao: AppDao) {

    // Master flows
    val allPT: Flow<List<MasterPT>> = dao.getAllPT()
    val allProyek: Flow<List<MasterProyek>> = dao.getAllProyek()
    val allLahan: Flow<List<MasterLahan>> = dao.getAllLahan()
    val allUnit: Flow<List<MasterUnit>> = dao.getAllUnit()
    val allPihak: Flow<List<MasterPihak>> = dao.getAllPihak()
    val allCOA: Flow<List<CoaAccount>> = dao.getAllCOA()
    val allCostCode: Flow<List<MasterCostCode>> = dao.getAllCostCode()
    val allAnggaran: Flow<List<AnggaranProyek>> = dao.getAllAnggaran()
    val allRevisi: Flow<List<RevisiAnggaranRecord>> = dao.getAllRevisi()
    val allTransaksi: Flow<List<TransaksiKasBankRecord>> = dao.getAllTransaksi()
    val postedTransaksi: Flow<List<TransaksiKasBankRecord>> = dao.getPostedTransaksi()
    val allPettyCashAdvances: Flow<List<PettyCashAdvanceRecord>> = dao.getAllPettyCashAdvances()
    val allIntercompany: Flow<List<IntercompanyRecord>> = dao.getAllIntercompany()
    val allKontrak: Flow<List<PenjualanUnitContract>> = dao.getAllKontrak()
    val allPembayaranCustomer: Flow<List<PembayaranCustomerRecord>> = dao.getAllPembayaranCustomer()
    val allPayroll: Flow<List<PayrollBuruhRecord>> = dao.getAllPayroll()
    val allDokumen: Flow<List<DokumenRegistryRecord>> = dao.getAllDokumen()
    val activeUsers: Flow<List<UserAppEntity>> = dao.getActiveUsers()
    val allKontraktor: Flow<List<KontraktorSpkRecord>> = dao.getAllKontraktor()
    val auditLogs: Flow<List<AuditLog>> = dao.getAuditLogs()
    val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
    val allTaxFilings: Flow<List<TaxFilingRecord>> = dao.getAllTaxFilings()
    val allInvestors: Flow<List<MitraInvestorRecord>> = dao.getAllInvestor()

    suspend fun insertUnit(unit: MasterUnit) = dao.insertSingleUnit(unit)
    suspend fun updateUnit(unit: MasterUnit) = dao.updateUnit(unit)
    suspend fun insertInvestor(investor: MitraInvestorRecord) = dao.insertInvestor(investor)
    suspend fun updateInvestor(investor: MitraInvestorRecord) = dao.updateInvestor(investor)

    // --- Hard-Stop Posting Engine ---
    suspend fun submitAndPostTransaksi(tx: TransaksiKasBankRecord): Result<TransaksiKasBankRecord> {
        val existingTx = dao.getTransaksiById(tx.id)
        if (existingTx != null && existingTx.statusSistem == "POSTED") {
            return Result.failure(Exception("TRANSAKSI SUDAH POSTED: Transaksi POSTED bersifat immutable dan tidak boleh diubah."))
        }

        if (tx.nominal <= 0) {
            return Result.failure(Exception("NILAI TIDAK VALID: Nominal transaksi harus lebih besar dari Rp 0."))
        }

        // Exempt accounts for Cost Code requirement (e.g. Intercompany 1230, Petty Cash 1320, Modal 3100)
        val isCostCodeExempt = tx.akunEfektif in listOf("1230", "1320", "3100", "2430")

        if (tx.tipe == "KELUAR" && tx.costCode.isBlank() && !isCostCodeExempt) {
            return Result.failure(Exception("COST CODE KOSONG: Transaksi KELUAR wajib memilih Cost Code proyek."))
        }

        if (tx.costCode.isNotBlank()) {
            val costCodes = dao.getAllCostCode().first()
            val match = costCodes.find { it.kode == tx.costCode && it.aktif }
            if (match == null) {
                return Result.failure(Exception("COST CODE TIDAK VALID ATAU NONAKTIF: Cost code '${tx.costCode}' tidak ditemukan di Master Cost Code."))
            }
        }

        // Approval checks
        val approver1 = dao.getUserByName(tx.disetujuiOleh1)
        val approver2 = if (tx.disetujuiOleh2.isNotBlank()) dao.getUserByName(tx.disetujuiOleh2) else null

        if (tx.dibuatOleh == tx.disetujuiOleh1) {
            return Result.failure(Exception("APPROVER = PEMBUAT: Pembuat transaksi (${tx.dibuatOleh}) tidak boleh menyetujui transaksinya sendiri."))
        }

        val requiredRank = when {
            tx.nominal <= 5_000_000L -> 1 // Supervisor
            tx.nominal <= 25_000_000L -> 2 // Manager / Head Finance
            else -> 3 // Director
        }

        if (approver1 == null || approver1.pangkat < requiredRank) {
            return Result.failure(Exception("PANGKAT APPROVER KURANG: Nominal Rp ${tx.nominal} memerlukan persetujuan pangkat minimal $requiredRank (saat ini ${approver1?.peran ?: "Belum dipilih"})."))
        }

        if (tx.nominal > 100_000_000L && (approver2 == null || approver2.pangkat < 4)) {
            return Result.failure(Exception("BUTUH OWNER SEBAGAI APPROVER 2: Transaksi di atas Rp 100 Juta wajib disetujui Owner (Pangkat 4)."))
        }

        // Document Registry Check
        if (tx.docRef.isNotBlank()) {
            val doc = dao.getDokumenByNo(tx.docRef)
            if (doc != null && doc.status != "VALID") {
                return Result.failure(Exception("DOKUMEN BELUM VALID: Dokumen '${tx.docRef}' berstatus '${doc.status}' di Dokumen Registry. Harus VALID sebelum posting."))
            }
        }

        // Hard-stop Budget Validation
        var linkedAnggaran: AnggaranProyek? = null
        if (tx.tipe == "KELUAR" && tx.costCode.isNotBlank() && !isCostCodeExempt) {
            linkedAnggaran = dao.getAnggaranByProjectAndCostCode(tx.projectId, tx.costCode)
            if (linkedAnggaran == null) {
                return Result.failure(Exception("BUDGET TIDAK DITEMUKAN: Belum ada pos anggaran untuk Proyek ${tx.projectId} dan Cost Code ${tx.costCode}."))
            }

            if (tx.nominal > linkedAnggaran.availableBudget) {
                val overAmount = tx.nominal - linkedAnggaran.availableBudget
                return Result.failure(Exception("REJECTED - OVER BUDGET! Sisa budget tersedia Rp ${linkedAnggaran.availableBudget}, permohonan Rp ${tx.nominal} (Kelebihan Rp $overAmount). Ajukan Revisi Anggaran terlebih dahulu."))
            }
        }

        // Posting Success -> Update Anggaran & Persist
        val postedRecord = tx.copy(
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            isPosted = true,
            candidateActual = if (tx.tipe == "KELUAR") tx.nominal else 0L
        )

        dao.insertTransaksi(postedRecord)

        // Update real actual posted in Budget
        if (linkedAnggaran != null) {
            val updatedAnggaran = linkedAnggaran.copy(
                actualPosted = linkedAnggaran.actualPosted + tx.nominal
            )
            dao.updateAnggaran(updatedAnggaran)
        }

        dao.insertAuditLog(
            AuditLog(
                user = tx.dibuatOleh,
                action = "TRANSACTION_POSTED",
                entityId = "PT-001",
                projectId = tx.projectId,
                recordId = tx.id,
                details = "Transaksi ${tx.id} nominal Rp ${tx.nominal} berhasil diposting ke Buku Kas & Bank"
            )
        )

        return Result.success(postedRecord)
    }

    // --- Immutable Reversal Flow ---
    suspend fun createReversal(originalTxId: String, reason: String, performedBy: String): Result<TransaksiKasBankRecord> {
        val original = dao.getTransaksiById(originalTxId)
            ?: return Result.failure(Exception("Transaksi $originalTxId tidak ditemukan."))

        if (original.statusSistem != "POSTED") {
            return Result.failure(Exception("Hanya transaksi POSTED yang dapat direversal."))
        }

        val reverseType = if (original.tipe == "KELUAR") "MASUK" else "KELUAR"
        val revId = "RV-${original.id.replace("TX-", "")}"

        val reversalRecord = TransaksiKasBankRecord(
            id = revId,
            tanggal = original.tanggal,
            tipe = reverseType,
            rekeningBank = original.rekeningBank,
            projectId = original.projectId,
            costCode = original.costCode,
            akunEfektif = original.akunEfektif,
            pihakId = original.pihakId,
            pihakNama = original.pihakNama,
            nominal = original.nominal,
            keterangan = "REVERSAL ATAS ${original.id}: $reason",
            docRef = original.docRef,
            dibuatOleh = performedBy,
            disetujuiOleh1 = original.disetujuiOleh1,
            disetujuiOleh2 = original.disetujuiOleh2,
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            reversalOfId = original.id,
            isPosted = true,
            candidateActual = if (reverseType == "KELUAR") original.nominal else 0L
        )

        dao.insertTransaksi(reversalRecord)

        // Mark original as REVERSED
        dao.updateTransaksi(original.copy(statusSistem = "REVERSED"))

        // Release actual on Budget
        if (original.tipe == "KELUAR" && original.costCode.isNotBlank()) {
            val anggaran = dao.getAnggaranByProjectAndCostCode(original.projectId, original.costCode)
            if (anggaran != null) {
                dao.updateAnggaran(anggaran.copy(actualPosted = maxOf(0L, anggaran.actualPosted - original.nominal)))
            }
        }

        dao.insertAuditLog(
            AuditLog(
                user = performedBy,
                action = "TRANSACTION_REVERSED",
                entityId = "PT-001",
                projectId = original.projectId,
                recordId = revId,
                details = "Reversal ${original.id} alasan: $reason"
            )
        )

        return Result.success(reversalRecord)
    }

    // --- Budget Revision with Approval Audit ---
    suspend fun submitRevisiAnggaran(revisi: RevisiAnggaranRecord): Result<RevisiAnggaranRecord> {
        val anggaran = dao.getAnggaranByProjectAndCostCode(revisi.projectId, revisi.costCode)
            ?: return Result.failure(Exception("Pos Anggaran tidak ditemukan."))

        val approver = dao.getUserByName(revisi.approvedBy)
        if (approver == null || approver.pangkat < 3) {
            return Result.failure(Exception("Persetujuan Revisi Anggaran memerlukan pangkat minimal Director (Pangkat 3)."))
        }

        val approvedRevisi = revisi.copy(status = "APPROVED")
        dao.insertRevisi(approvedRevisi)

        // Update Anggaran revisiApproved
        dao.updateAnggaran(
            anggaran.copy(
                revisiApproved = anggaran.revisiApproved + revisi.difference
            )
        )

        dao.insertAuditLog(
            AuditLog(
                user = revisi.requestedBy,
                action = "BUDGET_REVISED",
                entityId = "PT-001",
                projectId = revisi.projectId,
                recordId = revisi.revisionId,
                details = "Revisi Budget ${revisi.costCode} dari Rp ${revisi.oldAmount} menjadi Rp ${revisi.newAmount} disetujui oleh ${revisi.approvedBy}"
            )
        )

        return Result.success(approvedRevisi)
    }

    // --- Petty Cash with Lock Control ---
    suspend fun requestPettyCashAdvance(advance: PettyCashAdvanceRecord): Result<PettyCashAdvanceRecord> {
        val unsettled = dao.getUnsettledAdvancesForUser(advance.penerimaNama)
        if (unsettled.isNotEmpty()) {
            val openAdv = unsettled.first()
            return Result.failure(
                Exception("LOCKED - ADVANCE SEBELUMNYA BELUM SETTLED! Pengguna '${advance.penerimaNama}' masih memiliki advance '${openAdv.advanceId}' yang belum diselesaikan (Outstanding Rp ${openAdv.outstanding}).")
            )
        }

        if (advance.jumlahAdvance > advance.plafonUser) {
            return Result.failure(
                Exception("MELEBIHI PLAFON: Permintaan Rp ${advance.jumlahAdvance} melebihi batas plafon Rp ${advance.plafonUser}.")
            )
        }

        dao.insertPettyCashAdvance(advance)
        dao.insertAuditLog(
            AuditLog(
                user = advance.penerimaNama,
                action = "PETTY_CASH_REQUEST",
                entityId = "PT-001",
                projectId = advance.projectId,
                recordId = advance.advanceId,
                details = "Pencairan Petty Cash Rp ${advance.jumlahAdvance}"
            )
        )

        return Result.success(advance)
    }

    suspend fun updatePayrollStatus(id: Long, newStatus: String) {
        val list = dao.getAllPayroll().first()
        val item = list.find { it.id == id } ?: return
        dao.updatePayroll(item.copy(status = newStatus))
    }

    suspend fun updatePayroll(payroll: PayrollBuruhRecord) {
        dao.updatePayroll(payroll)
    }

    suspend fun insertDokumen(doc: DokumenRegistryRecord) {
        dao.insertDokumen(doc)
    }

    suspend fun insertPembayaranCustomer(pembayaran: PembayaranCustomerRecord) {
        dao.insertPembayaranCustomer(pembayaran)
        // Also update unit contract terbayarValid
        val kontrak = dao.getKontrakById(pembayaran.noKontrak)
        if (kontrak != null) {
            val newTerbayar = kontrak.terbayarValid + pembayaran.nominal
            val newStatus = if (newTerbayar >= kontrak.hargaNett) "LUNAS" else "ACTIVE"
            dao.updateKontrak(kontrak.copy(terbayarValid = newTerbayar, status = newStatus))
        }
    }

    suspend fun insertAttendance(attendance: AttendanceRecord) {
        dao.insertAttendance(attendance)
    }

    suspend fun insertTaxFiling(filing: TaxFilingRecord) {
        dao.insertTaxFiling(filing)
    }

    suspend fun updateTaxFiling(filing: TaxFilingRecord) {
        dao.updateTaxFiling(filing)
    }
}
