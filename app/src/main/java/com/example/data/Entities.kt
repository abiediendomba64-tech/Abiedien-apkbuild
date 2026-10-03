package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_pt")
data class MasterPT(
    @PrimaryKey val id: String, // e.g. "PT-001"
    val nama: String, // "PT Gema Abadi Nugraha"
    val nib: String,
    val npwp: String,
    val alamat: String,
    val rekeningBank: String, // "BCA Operasional (1110)"
    val peran: String // "Induk", "Anak"
)

@Entity(tableName = "master_proyek")
data class MasterProyek(
    @PrimaryKey val id: String, // "PRJ-001"
    val nama: String, // "Perumahan Gunung Padang, Ciamis"
    val entityId: String, // "PT-001"
    val unitCount: Int, // 120
    val totalBudget: Long, // e.g. 15_000_000_000
    val lokasi: String,
    val status: String // "BERJALAN", "PERENCANAAN", "SELESAI"
)

@Entity(tableName = "master_lahan")
data class MasterLahan(
    @PrimaryKey val id: String, // "LND-001"
    val projectId: String,
    val pemilikAsal: String,
    val luasM2: Double,
    val hargaPembebasan: Long,
    val statusLegalitas: String, // "SHM", "AJB", "HGB", "Atas Nama PT"
    val pphPenjual: Long,
    val bphtb: Long,
    val statusKwitansi: String // "LUNAS", "DP", "BELUM"
)

@Entity(tableName = "master_unit")
data class MasterUnit(
    @PrimaryKey val id: String, // "A-01"
    val projectId: String,
    val tipeRumah: String, // "Tipe 36/72", "Tipe 45/90", "Tipe 54/108"
    val luasTanahM2: Double,
    val luasBangunanM2: Double,
    val statusPembangunan: String, // "READY", "CONSTRUCTION", "PLANNING"
    val statusLegal: String, // "SHM Terbit", "Induk", "Pecah"
    val statusPenjualan: String, // "AVAILABLE", "BOOKED", "SOLD"
    val hargaJual: Long
)

@Entity(tableName = "master_pihak")
data class MasterPihak(
    @PrimaryKey val id: String, // "PHK-001"
    val nama: String,
    val kategori: String, // "VENDOR", "KONTRAKTOR", "CUSTOMER", "NOTARIS_PPAT"
    val telepon: String,
    val rekening: String,
    val status: String // "AKTIF", "NONAKTIF"
)

@Entity(tableName = "coa_account")
data class CoaAccount(
    @PrimaryKey val kodeAkun: String, // "1110", "1120", "1130", "1230", "1320", "1410", "2110", "2430", "3100", "4100", "5100", "6100"
    val namaAkun: String,
    val klasifikasi: String, // "Aset Lancar", "Aset Tetap", "Liabilitas", "Ekuitas", "Pendapatan", "HPP", "Beban Operasional"
    val saldoNormal: String, // "Debit", "Kredit"
    val aktif: Boolean
)

@Entity(tableName = "master_cost_code")
data class MasterCostCode(
    @PrimaryKey val kode: String, // "LND-001", "INF-001", "INF-002", "BLD-001", "BLD-002", "BLD-003", "OVH-001"
    val nama: String,
    val kategori: String, // "Lahan", "Infrastruktur", "Bangunan", "Overhead"
    val akunCoaDefault: String,
    val aktif: Boolean
)

@Entity(tableName = "anggaran_proyek")
data class AnggaranProyek(
    @PrimaryKey val id: String, // "ANG-001"
    val projectId: String,
    val costCode: String,
    val costCodeNama: String,
    val budgetAwal: Long,
    val revisiApproved: Long = 0L,
    val commitment: Long = 0L,
    val actualPosted: Long = 0L,
    val notes: String = ""
) {
    val budgetBerlaku: Long get() = budgetAwal + revisiApproved
    val availableBudget: Long get() = budgetBerlaku - actualPosted - commitment
    val statusBudget: String get() = when {
        availableBudget < 0 -> "OVER BUDGET"
        budgetBerlaku > 0 && availableBudget <= 0.1 * budgetBerlaku -> "WARNING"
        else -> "AMAN"
    }
}

@Entity(tableName = "revisi_anggaran")
data class RevisiAnggaranRecord(
    @PrimaryKey val revisionId: String, // "REV-2026-001"
    val tanggal: String,
    val budgetId: String,
    val costCode: String,
    val projectId: String,
    val oldAmount: Long,
    val newAmount: Long,
    val difference: Long,
    val reason: String,
    val requestedBy: String,
    val approvedBy: String,
    val approvalDate: String,
    val docRef: String,
    val status: String // "APPROVED", "INVALID", "PENDING"
)

@Entity(tableName = "transaksi_kas_bank")
data class TransaksiKasBankRecord(
    @PrimaryKey val id: String, // "TX-20261003-000001"
    val tanggal: String,
    val tipe: String, // "MASUK", "KELUAR"
    val rekeningBank: String, // "BCA Operasional", "BCA Proyek", "Kas Kecil"
    val projectId: String, // "PRJ-001"
    val costCode: String, // "BLD-001"
    val akunEfektif: String, // "1410"
    val pihakId: String,
    val pihakNama: String,
    val nominal: Long,
    val keterangan: String,
    val docRef: String, // "PPB-001", "KW-001"
    val dibuatOleh: String,
    val disetujuiOleh1: String,
    val disetujuiOleh2: String = "",
    val statusInput: String = "SUBMITTED", // "DRAFT", "SUBMITTED", "VOID"
    val statusSistem: String = "PENDING_APPROVAL", // "POSTED", "PENDING_APPROVAL", "PENDING_DOKUMEN", "REJECTED", "VOID", "REVERSED"
    val rejectionReason: String = "",
    val reversalOfId: String = "",
    val candidateActual: Long = 0L,
    val isPosted: Boolean = false,
    val photoProofUri: String = "",
    val signedProofStatus: String = "VALID", // "VALID", "PENDING_VERIFICATION", "MISSING_SIGNATURE"
    val signatureApproverName: String = "",
    val kategoriEntitas: String = "PT_INDUK", // "PT_INDUK", "PT_ANAK", "MITRA_INVESTOR", "KASIR_LAPANGAN"
    val tipeTransaksiKhusus: String = "OPERASIONAL", // "BELANJA_RAB", "OPERASIONAL", "MODAL_INVESTOR", "BAGI_HASIL_INVESTOR", "KOMISI_MARKETING", "GESER_UANG_MUTASI"
    val rekeningTujuan: String = "",
    val persentaseKomisiBagiHasil: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mitra_investor")
data class MitraInvestorRecord(
    @PrimaryKey val id: String, // e.g. "INV-001"
    val namaInvestor: String,
    val projectId: String,
    val modalDisetor: Long,
    val persentaseBagiHasil: Double, // e.g. 20.0%
    val totalDividenDiterima: Long = 0L,
    val status: String = "AKTIF", // "AKTIF", "SELESAI"
    val telepon: String = "",
    val rekeningBank: String = "",
    val tanggalBergabung: String = "2026-01-15",
    val keterangan: String = ""
)

@Entity(tableName = "petty_cash_advance")
data class PettyCashAdvanceRecord(
    @PrimaryKey val advanceId: String, // "ADV-2026-001"
    val tanggal: String,
    val penerimaNama: String,
    val projectId: String,
    val jumlahAdvance: Long,
    val plafonUser: Long,
    val terpakaiTerverifikasi: Long = 0L,
    val dikembalikan: Long = 0L,
    val jatuhTempo: String,
    val voucherTxId: String = "",
    val statusSettlement: String, // "SETTLED", "OPEN", "OVERDUE"
    val keterangan: String
) {
    val outstanding: Long get() = jumlahAdvance - terpakaiTerverifikasi - dikembalikan
}

@Entity(tableName = "petty_cash_expense")
data class PettyCashExpenseRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val advanceId: String,
    val tanggal: String,
    val uraian: String,
    val costCode: String,
    val nominal: Long,
    val noNota: String,
    val verifikasiNota: String // "Ya", "Belum", "Ditolak"
)

@Entity(tableName = "intercompany_record")
data class IntercompanyRecord(
    @PrimaryKey val id: String, // "IC-2026-001"
    val tanggal: String,
    val entitySumber: String, // "PT Gema Abadi Nugraha"
    val entityTujuan: String, // "PT Setia Surya Nugraha"
    val projectId: String,
    val voucherSumberTxId: String, // Akun 1230 KELUAR
    val voucherTujuanTxId: String, // Akun 2430 MASUK
    val nominal: Long,
    val settlementTotal: Long = 0L,
    val status: String, // "OUTSTANDING", "PARTIAL", "REIMBURSED", "CLOSED"
    val keterangan: String
) {
    val outstanding: Long get() = nominal - settlementTotal
}

@Entity(tableName = "penjualan_unit_contract")
data class PenjualanUnitContract(
    @PrimaryKey val noKontrak: String, // "KTR-2026-001"
    val tanggal: String,
    val unitId: String,
    val projectId: String,
    val customerNama: String,
    val customerNik: String,
    val skema: String, // "KPR", "CASH_KERAS", "IN_HOUSE", "CASH_BERTAHAP"
    val hargaBruto: Long,
    val diskon: Long = 0L,
    val bookingFee: Long,
    val totalDp: Long,
    val kprPlafond: Long = 0L,
    val terbayarValid: Long = 0L,
    val status: String // "ACTIVE", "LUNAS", "CANCELLED"
) {
    val hargaNett: Long get() = hargaBruto - diskon
    val sisaTagihan: Long get() = hargaNett - terbayarValid
}

@Entity(tableName = "pembayaran_customer")
data class PembayaranCustomerRecord(
    @PrimaryKey val receiptId: String, // "RCT-2026-001"
    val tanggal: String,
    val noKontrak: String,
    val unitId: String,
    val customerNama: String,
    val jenisPembayaran: String, // "BOOKING_FEE", "DP", "KPR_CAIR", "ANGSURAN", "PELUNASAN", "REFUND"
    val nominal: Long,
    val rekeningTujuan: String,
    val voucherTxId: String = "",
    val status: String // "VALID", "PENDING", "REJECTED"
)

@Entity(tableName = "payroll_buruh")
data class PayrollBuruhRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val kode: String, // "E", "R", "A", "E", "D", "O", "B"
    val nama: String, // "Eeng", "Rohman", "Abah", "eteh", "Dafid", "Ompong", "Bayu"
    val pekerjaan: String, // "Buruh Lapangan", "STAF"
    val jenisGaji: String, // "Harian", "Bulanan"
    val gajiPerHari: Long, // 120000, 50000, 1000000, 700000, 3000000
    val hariKerja: Double, // 6.5, 6, 7, 30, 0, 5
    val totalGaji: Long,
    val tanggal: String, // "2026-09-19"
    val status: String, // "Bayar", "Pending"
    val noWa: String, // "6281318575529"
    val catatan: String, // "6 hari kerja periode 13-19 Sep"
    val potonganKasbon: Long = 0L,
    val potonganBpjs: Long = 0L,
    val pph21Terutang: Long = 0L,
    val uangLembur: Long = 0L
) {
    val gajiBersih: Long get() = totalGaji + uangLembur - potonganKasbon - potonganBpjs - pph21Terutang
}

@Entity(tableName = "attendance_record")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val workerId: Long,
    val nama: String,
    val tanggal: String,
    val status: String, // "Hadir", "Lembur", "Izin", "Sakit", "Alpa"
    val jamMasuk: String = "08:00",
    val jamKeluar: String = "17:00",
    val jamLembur: Double = 0.0,
    val catatan: String = ""
)

@Entity(tableName = "tax_filing_record")
data class TaxFilingRecord(
    @PrimaryKey val id: String, // e.g. "SPT-PPH42-202609"
    val jenisPajak: String, // "PPh Final 4(2)", "PPN Keluaran/DTP", "PPh 21 Buruh", "PBB Proyek"
    val masaPajak: String, // "September 2026"
    val tahunPajak: Int = 2026,
    val entityId: String, // "PT-001"
    val dasarPengenaanPajak: Long, // DPP
    val tarifPersen: Double,
    val nominalPajak: Long,
    val kodeBilling: String,
    val ntpn: String,
    val statusSetor: String, // "LUNAS", "BELUM_SETOR"
    val statusLapor: String, // "SUDAH_LAPOR", "DRAFT"
    val tanggalBayar: String,
    val tanggalLapor: String,
    val buktiDocRef: String = ""
)

@Entity(tableName = "dokumen_registry")
data class DokumenRegistryRecord(
    @PrimaryKey val id: String, // "DOC-2026-001"
    val noDokumen: String, // "KW-2026/09/001"
    val jenisDokumen: String, // "KWITANSI", "SPK_KONTRAKTOR", "INVOICE_NOTA", "BAST", "BUKTI_TRANSFER", "SURAT_PENGANTAR"
    val peruntukan: String,
    val pihakTerkait: String,
    val status: String, // "VALID", "PENDING", "REQUIRED", "REJECTED"
    val tanggal: String,
    val verifiedBy: String = "",
    val notes: String = ""
)

@Entity(tableName = "user_app")
data class UserAppEntity(
    @PrimaryKey val nama: String,
    val peran: String, // "Pelaksana", "Site Manager", "Admin Finance", "Kasir", "Supervisor", "Manager", "Head Finance", "Director", "Owner"
    val pangkat: Int, // 0, 1, 2, 3, 4
    val plafonPettyCash: Long,
    val aktif: Boolean
)

@Entity(tableName = "kontraktor_spk")
data class KontraktorSpkRecord(
    @PrimaryKey val spkId: String, // "SPK-001"
    val kontraktorNama: String,
    val projectId: String,
    val jenisPekerjaan: String, // "Pondasi & Struktur Blok A", "Finishing Unit A1-A10"
    val nilaiKontrak: Long,
    val uangMuka: Long,
    val retensiPersen: Double = 5.0, // Retensi 5%
    val progressFisikPersen: Double = 0.0,
    val terbayarTermin: Long = 0L,
    val status: String // "BERJALAN", "SELESAI", "PENDING_BAST"
) {
    val retensiNilai: Long get() = (nilaiKontrak * (retensiPersen / 100.0)).toLong()
    val sisaKontrak: Long get() = nilaiKontrak - terbayarTermin
}

@Entity(tableName = "audit_log")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val user: String,
    val action: String,
    val entityId: String,
    val projectId: String,
    val recordId: String,
    val details: String
)

enum class ProofType {
    RECEIPT,
    SIGNATURE,
    INVOICE,
    SPK,
    BAST
}

@Entity(tableName = "payment_proof_record")
data class PaymentProofRecord(
    @PrimaryKey val id: String, // e.g. "PRF-20261003-0001"
    val transactionId: String,
    val proofType: String, // "RECEIPT", "SIGNATURE", "INVOICE", "SPK", "BAST"
    val fileName: String,
    val filePath: String,
    val fileUri: String,
    val fileSizeBytes: Long,
    val mimeType: String = "image/jpeg",
    val sha256Checksum: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String = "Operator",
    val notes: String = ""
)
