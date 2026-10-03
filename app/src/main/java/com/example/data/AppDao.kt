package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Master PT
    @Query("SELECT * FROM master_pt")
    fun getAllPT(): Flow<List<MasterPT>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPT(ptList: List<MasterPT>)

    // Master Proyek
    @Query("SELECT * FROM master_proyek")
    fun getAllProyek(): Flow<List<MasterProyek>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProyek(proyekList: List<MasterProyek>)

    // Master Lahan
    @Query("SELECT * FROM master_lahan")
    fun getAllLahan(): Flow<List<MasterLahan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLahan(lahanList: List<MasterLahan>)

    // Master Unit
    @Query("SELECT * FROM master_unit")
    fun getAllUnit(): Flow<List<MasterUnit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unitList: List<MasterUnit>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleUnit(unit: MasterUnit)

    @Update
    suspend fun updateUnit(unit: MasterUnit)

    // Mitra Investor
    @Query("SELECT * FROM mitra_investor ORDER BY modalDisetor DESC")
    fun getAllInvestor(): Flow<List<MitraInvestorRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestor(investor: MitraInvestorRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestorList(list: List<MitraInvestorRecord>)

    @Update
    suspend fun updateInvestor(investor: MitraInvestorRecord)

    // Master Pihak
    @Query("SELECT * FROM master_pihak")
    fun getAllPihak(): Flow<List<MasterPihak>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPihak(pihakList: List<MasterPihak>)

    // COA
    @Query("SELECT * FROM coa_account ORDER BY kodeAkun ASC")
    fun getAllCOA(): Flow<List<CoaAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCOA(coaList: List<CoaAccount>)

    // Master Cost Code
    @Query("SELECT * FROM master_cost_code ORDER BY kode ASC")
    fun getAllCostCode(): Flow<List<MasterCostCode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCostCode(costCodeList: List<MasterCostCode>)

    // Anggaran Proyek
    @Query("SELECT * FROM anggaran_proyek")
    fun getAllAnggaran(): Flow<List<AnggaranProyek>>

    @Query("SELECT * FROM anggaran_proyek WHERE projectId = :projectId AND costCode = :costCode LIMIT 1")
    suspend fun getAnggaranByProjectAndCostCode(projectId: String, costCode: String): AnggaranProyek?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnggaran(anggaranList: List<AnggaranProyek>)

    @Update
    suspend fun updateAnggaran(anggaran: AnggaranProyek)

    // Revisi Anggaran
    @Query("SELECT * FROM revisi_anggaran ORDER BY tanggal DESC")
    fun getAllRevisi(): Flow<List<RevisiAnggaranRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisi(revisi: RevisiAnggaranRecord)

    // Transaksi Kas & Bank
    @Query("SELECT * FROM transaksi_kas_bank ORDER BY tanggal DESC, id DESC")
    fun getAllTransaksi(): Flow<List<TransaksiKasBankRecord>>

    @Query("SELECT * FROM transaksi_kas_bank WHERE statusSistem = 'POSTED' ORDER BY tanggal DESC")
    fun getPostedTransaksi(): Flow<List<TransaksiKasBankRecord>>

    @Query("SELECT * FROM transaksi_kas_bank WHERE id = :id LIMIT 1")
    suspend fun getTransaksiById(id: String): TransaksiKasBankRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: TransaksiKasBankRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksiList(list: List<TransaksiKasBankRecord>)

    @Update
    suspend fun updateTransaksi(transaksi: TransaksiKasBankRecord)

    // Petty Cash
    @Query("SELECT * FROM petty_cash_advance ORDER BY tanggal DESC")
    fun getAllPettyCashAdvances(): Flow<List<PettyCashAdvanceRecord>>

    @Query("SELECT * FROM petty_cash_advance WHERE penerimaNama = :penerima AND statusSettlement <> 'SETTLED'")
    suspend fun getUnsettledAdvancesForUser(penerima: String): List<PettyCashAdvanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPettyCashAdvance(advance: PettyCashAdvanceRecord)

    @Update
    suspend fun updatePettyCashAdvance(advance: PettyCashAdvanceRecord)

    @Query("SELECT * FROM petty_cash_expense WHERE advanceId = :advanceId")
    fun getExpensesForAdvance(advanceId: String): Flow<List<PettyCashExpenseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPettyCashExpense(expense: PettyCashExpenseRecord)

    // Intercompany
    @Query("SELECT * FROM intercompany_record ORDER BY tanggal DESC")
    fun getAllIntercompany(): Flow<List<IntercompanyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntercompany(ic: IntercompanyRecord)

    @Update
    suspend fun updateIntercompany(ic: IntercompanyRecord)

    // Penjualan Unit & Kontrak
    @Query("SELECT * FROM penjualan_unit_contract ORDER BY tanggal DESC")
    fun getAllKontrak(): Flow<List<PenjualanUnitContract>>

    @Query("SELECT * FROM penjualan_unit_contract WHERE noKontrak = :noKontrak LIMIT 1")
    suspend fun getKontrakById(noKontrak: String): PenjualanUnitContract?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKontrak(kontrak: PenjualanUnitContract)

    @Update
    suspend fun updateKontrak(kontrak: PenjualanUnitContract)

    // Pembayaran Customer
    @Query("SELECT * FROM pembayaran_customer ORDER BY tanggal DESC")
    fun getAllPembayaranCustomer(): Flow<List<PembayaranCustomerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPembayaranCustomer(pembayaran: PembayaranCustomerRecord)

    // Payroll Buruh
    @Query("SELECT * FROM payroll_buruh ORDER BY tanggal DESC, id ASC")
    fun getAllPayroll(): Flow<List<PayrollBuruhRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payrollList: List<PayrollBuruhRecord>)

    @Update
    suspend fun updatePayroll(payroll: PayrollBuruhRecord)

    // Dokumen Registry
    @Query("SELECT * FROM dokumen_registry ORDER BY tanggal DESC")
    fun getAllDokumen(): Flow<List<DokumenRegistryRecord>>

    @Query("SELECT * FROM dokumen_registry WHERE noDokumen = :noDokumen LIMIT 1")
    suspend fun getDokumenByNo(noDokumen: String): DokumenRegistryRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDokumen(dokumen: DokumenRegistryRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDokumenList(list: List<DokumenRegistryRecord>)

    // Users
    @Query("SELECT * FROM user_app WHERE aktif = 1")
    fun getActiveUsers(): Flow<List<UserAppEntity>>

    @Query("SELECT * FROM user_app WHERE nama = :nama LIMIT 1")
    suspend fun getUserByName(nama: String): UserAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAppEntity>)

    // Kontraktor SPK
    @Query("SELECT * FROM kontraktor_spk")
    fun getAllKontraktor(): Flow<List<KontraktorSpkRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKontraktor(spkList: List<KontraktorSpkRecord>)

    @Update
    suspend fun updateKontraktor(spk: KontraktorSpkRecord)

    // Audit Log
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 200")
    fun getAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Attendance
    @Query("SELECT * FROM attendance_record ORDER BY tanggal DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceRecord>)

    // Tax Filing
    @Query("SELECT * FROM tax_filing_record ORDER BY tahunPajak DESC, id DESC")
    fun getAllTaxFilings(): Flow<List<TaxFilingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxFiling(filing: TaxFilingRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxFilingList(list: List<TaxFilingRecord>)

    @Update
    suspend fun updateTaxFiling(filing: TaxFilingRecord)

    // Payment Proofs & Signatures
    @Query("SELECT * FROM payment_proof_record ORDER BY capturedAt DESC")
    fun getAllPaymentProofs(): Flow<List<PaymentProofRecord>>

    @Query("SELECT * FROM payment_proof_record WHERE transactionId = :txId ORDER BY capturedAt ASC")
    fun getProofsForTransaction(txId: String): Flow<List<PaymentProofRecord>>

    @Query("SELECT * FROM payment_proof_record WHERE id = :id LIMIT 1")
    suspend fun getProofById(id: String): PaymentProofRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProof(proof: PaymentProofRecord)

    @Delete
    suspend fun deleteProof(proof: PaymentProofRecord)

    @Query("DELETE FROM payment_proof_record WHERE id = :id")
    suspend fun deleteProofById(id: String)
}
