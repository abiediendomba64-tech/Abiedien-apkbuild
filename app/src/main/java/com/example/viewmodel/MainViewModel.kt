package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen(val title: String) {
    data object Dashboard : Screen("Dashboard Eksekutif")
    data object Transaksi : Screen("Buku Kas & Bank")
    data object Anggaran : Screen("Kontrol Anggaran (RAB)")
    data object PenjualanUnit : Screen("Penjualan & Stok Unit")
    data object Kontraktor : Screen("Kontraktor & SPK")
    data object Payroll : Screen("Payroll & Absensi Buruh")
    data object Intercompany : Screen("Intercompany Induk-Anak")
    data object PettyCash : Screen("Kas Kecil (Petty Cash)")
    data object Pajak : Screen("Kepatuhan Pajak SAK EP")
    data object Jurnal : Screen("Jurnal Otomatis Double Entry")
    data object DokumenKwitansi : Screen("Surat & Kwitansi Resmi")
    data object AuditKontrol : Screen("Audit & Kontrol Sistem")
    data object AiAdvisor : Screen("Asisten AI & Scan Dokumen")
}

data class DashboardMetrics(
    val totalKasBank: Long = 0L,
    val totalBudget: Long = 0L,
    val totalActualPosted: Long = 0L,
    val totalCommitment: Long = 0L,
    val totalAvailableBudget: Long = 0L,
    val unitTotal: Int = 0,
    val unitSold: Int = 0,
    val unitBooked: Int = 0,
    val unitAvailable: Int = 0,
    val overBudgetCount: Int = 0,
    val pendingApprovalCount: Int = 0,
    val activeAlarmCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())
    private val cloudTransactionRepository = CloudTransactionRepository()
    private val geminiService = GeminiService()

    val currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))

    val allPT = repository.allPT.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allProyek = repository.allProyek.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allLahan = repository.allLahan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allUnit = repository.allUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPihak = repository.allPihak.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCOA = repository.allCOA.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCostCode = repository.allCostCode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAnggaran = repository.allAnggaran.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRevisi = repository.allRevisi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransaksi = repository.allTransaksi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val postedTransaksi = repository.postedTransaksi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPettyCashAdvances = repository.allPettyCashAdvances.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allIntercompany = repository.allIntercompany.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allKontrak = repository.allKontrak.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPembayaranCustomer = repository.allPembayaranCustomer.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPayroll = repository.allPayroll.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDokumen = repository.allDokumen.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeUsers = repository.activeUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allKontraktor = repository.allKontraktor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTaxFilings = repository.allTaxFilings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allInvestors = repository.allInvestors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val imageUploadManager = ImageUploadManager(application, db.appDao())
    val allPaymentProofs = imageUploadManager.getAllProofs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Entity Filter for Executive Dashboard ("ALL", "PT-001", "PT-002")
    val selectedConsolidationEntity = MutableStateFlow("ALL")

    // Project Profitability Analysis Flow
    val projectProfitabilities: StateFlow<List<ProjectProfitability>> = combine(
        allProyek,
        allLahan,
        allUnit,
        allTransaksi,
        allKontrak
    ) { prjList, lhnList, unitList, txList, ktrList ->
        prjList.map { prj ->
            ProjectProfitabilityEngine.calculate(
                proyek = prj,
                lahanList = lhnList,
                unitList = unitList,
                transaksiList = txList,
                kontrakList = ktrList,
                pembayaranList = allPembayaranCustomer.value
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consolidated Financial Group Summary
    val consolidatedSummary: StateFlow<ConsolidatedGroupSummary> = combine(
        allPT,
        allProyek,
        allTransaksi,
        allIntercompany,
        allKontrak
    ) { ptList, prjList, txList, icList, ktrList ->
        ConsolidationEngine.calculateConsolidation(
            ptList,
            prjList,
            txList,
            icList,
            ktrList,
            allPembayaranCustomer.value
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ConsolidatedGroupSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0.0, emptyList())
    )

    // Notification toast / snackbar message
    val userFeedbackMessage = MutableStateFlow<String?>(null)
    val isErrorMessage = MutableStateFlow(false)

    // AI Chat Messages
    val aiMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Selamat datang! Saya Asisten Pakar Pembukuan Keuangan Developer Properti SAK EP. Tanyakan aturan pembukuan, penjelasan Bahasa Awam, atau unggah foto kwitansi untuk dianalisis." to false
        )
    )
    val isAiLoading = MutableStateFlow(false)

    // Derived Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        postedTransaksi,
        allAnggaran,
        allUnit
    ) { txList, anggaranList, unitList ->
        val totalMasuk = txList.filter { it.tipe == "MASUK" }.sumOf { it.nominal }
        val totalKeluar = txList.filter { it.tipe == "KELUAR" }.sumOf { it.nominal }
        val saldoKas = totalMasuk - totalKeluar

        val totalBudget = anggaranList.sumOf { it.budgetBerlaku }
        val totalActual = anggaranList.sumOf { it.actualPosted }
        val totalCommitment = anggaranList.sumOf { it.commitment }
        val totalAvailable = totalBudget - totalActual - totalCommitment

        val totalUnits = unitList.size
        val sold = unitList.count { it.statusPenjualan == "SOLD" }
        val booked = unitList.count { it.statusPenjualan == "BOOKED" }
        val available = unitList.count { it.statusPenjualan == "AVAILABLE" }

        val overBudget = anggaranList.count { it.statusBudget == "OVER BUDGET" }

        DashboardMetrics(
            totalKasBank = saldoKas,
            totalBudget = totalBudget,
            totalActualPosted = totalActual,
            totalCommitment = totalCommitment,
            totalAvailableBudget = totalAvailable,
            unitTotal = totalUnits,
            unitSold = sold,
            unitBooked = booked,
            unitAvailable = available,
            overBudgetCount = overBudget,
            activeAlarmCount = if (overBudget > 0) 1 else 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Derived Double-Entry Journal
    val journalReport: StateFlow<JournalReport> = combine(
        postedTransaksi,
        allCOA
    ) { txList, coaList ->
        AccountingEngine.generateAutomaticJournal(txList, coaList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalReport(emptyList(), 0L, 0L, true, 0L))

    // Derived 16 Live Audit Checks
    @Suppress("UNCHECKED_CAST")
    val auditChecks: StateFlow<List<AuditCheckItem>> = combine(
        allTransaksi,
        allAnggaran,
        allRevisi,
        allIntercompany,
        allPettyCashAdvances,
        allLahan,
        allKontrak,
        allPembayaranCustomer,
        activeUsers,
        allProyek,
        allPT,
        journalReport
    ) { params ->
        val tx = params[0] as List<TransaksiKasBankRecord>
        val ang = params[1] as List<AnggaranProyek>
        val rev = params[2] as List<RevisiAnggaranRecord>
        val ic = params[3] as List<IntercompanyRecord>
        val pc = params[4] as List<PettyCashAdvanceRecord>
        val lh = params[5] as List<MasterLahan>
        val kt = params[6] as List<PenjualanUnitContract>
        val pb = params[7] as List<PembayaranCustomerRecord>
        val us = params[8] as List<UserAppEntity>
        val pr = params[9] as List<MasterProyek>
        val pt = params[10] as List<MasterPT>
        val jr = params[11] as JournalReport

        AuditChecker.evaluate(tx, ang, rev, ic, pc, lh, kt, pb, us, pr, pt, jr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun navigateTo(screen: Screen) {
        if (currentScreen.value != screen) {
            screenStack.value = screenStack.value + screen
            currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        val stack = screenStack.value
        return if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            screenStack.value = newStack
            currentScreen.value = newStack.last()
            true
        } else {
            false
        }
    }

    fun refreshTransactionsFromCloud() {
        viewModelScope.launch {
            try {
                val remote = cloudTransactionRepository.fetchTransactions()
                if (remote.isNotEmpty()) {
                    db.appDao().clearAllTransaksi()
                    db.appDao().insertTransaksiList(remote)
                }
                userFeedbackMessage.value = "Data Buku Kas/Bank diperbarui dari Supabase (" + remote.size + " transaksi)."
                isErrorMessage.value = false
            } catch (e: Throwable) {
                userFeedbackMessage.value = "Gagal memuat Buku Kas/Bank dari Supabase: " + (e.message ?: "unknown error")
                isErrorMessage.value = true
            }
        }
    }

    fun submitTransaksi(tx: TransaksiKasBankRecord) {
        viewModelScope.launch {
            try {
                val remote = cloudTransactionRepository.postTransaction(tx)
                db.appDao().insertTransaksi(remote)
                userFeedbackMessage.value = "Transaksi " + remote.id + " tersimpan di Supabase dan menunggu approval."
                isErrorMessage.value = false
            } catch (e: Throwable) {
                userFeedbackMessage.value = e.message ?: "Gagal menyimpan transaksi ke Supabase."
                isErrorMessage.value = true
            }
        }
    }

    fun approveTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                val remote = cloudTransactionRepository.approveTransaction(transactionId)
                db.appDao().insertTransaksi(remote)
                userFeedbackMessage.value = if (remote.statusSistem == "POSTED") {
                    "Transaksi " + remote.id + " berhasil POSTED oleh user terautentikasi."
                } else {
                    "Approval tahap 1 transaksi " + remote.id + " tersimpan; menunggu approval berikutnya."
                }
                isErrorMessage.value = false
            } catch (e: Throwable) {
                userFeedbackMessage.value = e.message ?: "Approval transaksi gagal."
                isErrorMessage.value = true
            }
        }
    }

    fun createReversal(originalTxId: String, reason: String, performedBy: String) {
        viewModelScope.launch {
            try {
                val remote = cloudTransactionRepository.reverseTransaction(originalTxId, reason)
                db.appDao().insertTransaksi(remote)
                userFeedbackMessage.value = "Reversal " + remote.id + " tersimpan di Supabase dan transaksi asli dinetralkan."
                isErrorMessage.value = false
            } catch (e: Throwable) {
                userFeedbackMessage.value = e.message ?: "Reversal gagal."
                isErrorMessage.value = true
            }
        }
    }

    fun submitRevisiAnggaran(revisi: RevisiAnggaranRecord) {
        viewModelScope.launch {
            val result = repository.submitRevisiAnggaran(revisi)
            result.onSuccess {
                userFeedbackMessage.value = "Revisi Anggaran ${it.revisionId} berhasil disetujui (APPROVED)!"
                isErrorMessage.value = false
            }.onFailure {
                userFeedbackMessage.value = it.message ?: "Gagal menyetujui revisi anggaran."
                isErrorMessage.value = true
            }
        }
    }

    fun requestPettyCash(advance: PettyCashAdvanceRecord) {
        viewModelScope.launch {
            val result = repository.requestPettyCashAdvance(advance)
            result.onSuccess {
                userFeedbackMessage.value = "Permohonan Advance ${it.advanceId} berhasil dicatat!"
                isErrorMessage.value = false
            }.onFailure {
                userFeedbackMessage.value = it.message ?: "Gagal memproses kas kecil."
                isErrorMessage.value = true
            }
        }
    }

    fun updatePayrollStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updatePayrollStatus(id, newStatus)
            userFeedbackMessage.value = "Status gaji berhasil diperbarui menjadi: $newStatus"
            isErrorMessage.value = false
        }
    }

    fun recordCustomerPayment(pembayaran: PembayaranCustomerRecord) {
        viewModelScope.launch {
            repository.insertPembayaranCustomer(pembayaran)
            userFeedbackMessage.value = "Pembayaran ${pembayaran.receiptId} untuk kontrak ${pembayaran.noKontrak} berhasil dicatat!"
            isErrorMessage.value = false
        }
    }

    fun addDocumentRegistry(doc: DokumenRegistryRecord) {
        viewModelScope.launch {
            repository.insertDokumen(doc)
            userFeedbackMessage.value = "Dokumen ${doc.noDokumen} berhasil didaftarkan ke Registry!"
            isErrorMessage.value = false
        }
    }

    fun clearFeedback() {
        userFeedbackMessage.value = null
    }

    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        val current = aiMessages.value.toMutableList()
        current.add(question to true)
        aiMessages.value = current
        isAiLoading.value = true

        viewModelScope.launch {
            val result = geminiService.generateAccountingAdvice(question)
            isAiLoading.value = false
            val answer = result.getOrElse { "Maaf, terjadi kendala saat memproses jawaban: ${it.message}" }
            aiMessages.value = aiMessages.value + (answer to false)
        }
    }

    fun recordAttendance(att: AttendanceRecord) {
        viewModelScope.launch {
            repository.insertAttendance(att)
            // Automatically update worker's workdays and overtime in payroll
            val currentPayroll = allPayroll.value
            val matchWorker = currentPayroll.find { it.nama.equals(att.nama, ignoreCase = true) }
            if (matchWorker != null) {
                val addition = if (att.status == "Hadir" || att.status == "Lembur") 1.0 else 0.0
                val newHariKerja = matchWorker.hariKerja + addition
                val newLemburMoney = if (att.jamLembur > 0) (att.jamLembur * 25000.0).toLong() else 0L
                val newTotal = (matchWorker.gajiPerHari * newHariKerja).toLong()
                val updatedPayroll = matchWorker.copy(
                    hariKerja = newHariKerja,
                    totalGaji = newTotal,
                    uangLembur = matchWorker.uangLembur + newLemburMoney
                )
                repository.updatePayroll(updatedPayroll)
            }
            userFeedbackMessage.value = "Presensi ${att.nama} (${att.status}) tersimpan & upah otomatis disinkronkan ke laporan!"
            isErrorMessage.value = false
        }
    }

    fun addNewUnit(unit: MasterUnit) {
        viewModelScope.launch {
            repository.insertUnit(unit)
            userFeedbackMessage.value = "Unit ${unit.id} (${unit.tipeRumah}) berhasil ditambahkan ke inventori properti!"
            isErrorMessage.value = false
        }
    }

    fun updateUnitStatus(unit: MasterUnit) {
        viewModelScope.launch {
            repository.updateUnit(unit)
            userFeedbackMessage.value = "Status Unit ${unit.id} diperbarui menjadi ${unit.statusPenjualan}!"
            isErrorMessage.value = false
        }
    }

    fun addNewInvestor(investor: MitraInvestorRecord) {
        viewModelScope.launch {
            repository.insertInvestor(investor)
            userFeedbackMessage.value = "Mitra Investor ${investor.namaInvestor} (${investor.persentaseBagiHasil}%) berhasil didaftarkan!"
            isErrorMessage.value = false
        }
    }

    fun submitTaxFiling(filing: TaxFilingRecord) {
        viewModelScope.launch {
            repository.insertTaxFiling(filing)
            userFeedbackMessage.value = "Pelaporan Pajak ${filing.jenisPajak} (${filing.masaPajak}) berhasil direkam!"
            isErrorMessage.value = false
        }
    }

    fun exportPayrollCsv(): String {
        return CsvSyncHelper.exportPayrollToCsv(allPayroll.value)
    }

    fun importPayrollFromCsv(csvText: String) {
        viewModelScope.launch {
            try {
                val parsed = CsvSyncHelper.parsePayrollFromCsv(csvText)
                if (parsed.isNotEmpty()) {
                    parsed.forEach { item ->
                        repository.updatePayrollStatus(item.id, item.status)
                    }
                    userFeedbackMessage.value = "Sinkronisasi Spreadsheet: ${parsed.size} baris data berhasil diperbarui!"
                    isErrorMessage.value = false
                } else {
                    userFeedbackMessage.value = "Tidak ada baris data valid yang ditemukan dalam CSV."
                    isErrorMessage.value = true
                }
            } catch (e: Exception) {
                userFeedbackMessage.value = "Gagal mengimpor CSV: ${e.message}"
                isErrorMessage.value = true
            }
        }
    }

    fun savePaymentProof(
        bitmap: Bitmap,
        transactionId: String,
        proofType: ProofType,
        uploadedBy: String = "Operator",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val res = imageUploadManager.saveImageLocally(bitmap, transactionId, proofType, uploadedBy, notes)
            res.onSuccess {
                userFeedbackMessage.value = "Bukti fisik ${it.proofType} berhasil disimpan secara lokal dan diverifikasi!"
                isErrorMessage.value = false
            }.onFailure {
                userFeedbackMessage.value = "Gagal menyimpan bukti fisik: ${it.message}"
                isErrorMessage.value = true
            }
        }
    }

    fun exportTransactionsCsv(): String {
        return SpreadsheetParserService.exportTransactionsToCsv(allTransaksi.value)
    }

    fun exportTransactionsXlsx(): SpreadsheetExportBundle {
        return SpreadsheetParserService.exportTransactionsToXlsx(allTransaksi.value)
    }

    fun importTransactionsFromCsv(csvText: String) {
        viewModelScope.launch {
            try {
                val list = SpreadsheetParserService.parseTransactionsFromCsv(csvText)
                if (list.isNotEmpty()) {
                    list.forEach { tx ->
                        repository.submitAndPostTransaksi(tx)
                    }
                    userFeedbackMessage.value = "Migrasi Data Berhasil: ${list.size} transaksi diimpor ke Buku Kas & Bank!"
                    isErrorMessage.value = false
                } else {
                    userFeedbackMessage.value = "Format CSV tidak sesuai atau baris kosong."
                    isErrorMessage.value = true
                }
            } catch (e: Exception) {
                userFeedbackMessage.value = "Gagal memproses migrasi data: ${e.message}"
                isErrorMessage.value = true
            }
        }
    }

    fun analyzeReceipt(bitmap: Bitmap) {
        val current = aiMessages.value.toMutableList()
        current.add("📷 [Mengunggah Foto Nota / Kwitansi Fisik...]" to true)
        aiMessages.value = current
        isAiLoading.value = true

        viewModelScope.launch {
            val result = geminiService.analyzeReceipt(bitmap)
            isAiLoading.value = false
            val answer = result.getOrElse { "Gagal menganalisis dokumen nota: ${it.message}" }
            aiMessages.value = aiMessages.value + (answer to false)
        }
    }

    // AI Market Price Research & Nominal Alarm System
    val latestPriceAnalysis = MutableStateFlow<PriceResearchAnalysis?>(null)
    val priceAlarmsList = MutableStateFlow<List<PriceResearchAnalysis>>(emptyList())
    val isPriceAuditLoading = MutableStateFlow(false)

    fun checkInstantPriceAudit(
        itemName: String,
        nominal: Long,
        volume: Double = 1.0,
        costCode: String = "",
        satuanInput: String = ""
    ): PriceResearchAnalysis {
        val analysis = AiPriceAuditor.audit(
            itemName = itemName,
            totalNominal = nominal,
            volume = volume,
            costCode = costCode,
            satuanInput = satuanInput
        )
        latestPriceAnalysis.value = analysis
        return analysis
    }

    fun performAiPriceAudit(
        itemName: String,
        nominal: Long,
        volume: Double = 1.0,
        costCode: String = "",
        satuanInput: String = ""
    ) {
        viewModelScope.launch {
            isPriceAuditLoading.value = true
            val analysis = geminiService.researchPriceWithAi(
                itemName = itemName,
                nominal = nominal,
                volume = volume,
                costCode = costCode
            )
            latestPriceAnalysis.value = analysis
            isPriceAuditLoading.value = false

            if (analysis.isAlarmTriggered) {
                priceAlarmsList.value = listOf(analysis) + priceAlarmsList.value.take(19)
                userFeedbackMessage.value = "⚠️ ALARM NOMINAL: '${analysis.itemName}' terindikasi markup +${analysis.deviationPercent.toInt()}% di atas standar pasar!"
                isErrorMessage.value = true
            } else {
                userFeedbackMessage.value = "✅ Riset AI: Rancangan harga '${analysis.itemName}' masuk akal (${analysis.statusLabel})."
                isErrorMessage.value = false
            }
        }
    }

    fun clearLatestPriceAnalysis() {
        latestPriceAnalysis.value = null
    }

    // AI Expense Anomaly Analysis against Predefined RAB Categories
    val expenseAnomalyReport: StateFlow<AnomalyAuditReport> = combine(
        allTransaksi,
        allAnggaran
    ) { txList, rabList ->
        AiExpenseAnomalyAuditor.auditTransactions(txList, rabList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnomalyAuditReport(0, 0, 0, 0, emptyList(), "Memuat analisis anomali RAB...")
    )

    val aiDeepAnomalyInsight = MutableStateFlow<String?>(null)
    val isDeepAnomalyLoading = MutableStateFlow(false)

    fun requestDeepAiAnomalyAnalysis() {
        viewModelScope.launch {
            isDeepAnomalyLoading.value = true
            val report = geminiService.analyzeRabExpenseAnomaliesWithAi(
                transactions = allTransaksi.value,
                anggaranList = allAnggaran.value
            )
            aiDeepAnomalyInsight.value = report.auditSummary
            isDeepAnomalyLoading.value = false
            if (report.criticalCount > 0) {
                userFeedbackMessage.value = "⚠️ Deteksi Anomali Kritis: Ditemukan ${report.criticalCount} transaksi melanggar plafon/kategori RAB!"
                isErrorMessage.value = true
            } else {
                userFeedbackMessage.value = "✅ Audit AI Selesai: ${report.auditSummary}"
                isErrorMessage.value = false
            }
        }
    }
}
