package com.example

import com.example.data.*
import com.example.domain.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testTerbilangHelper() {
    assertEquals("Dua Puluh Lima Juta Rupiah", TerbilangHelper.konversi(25_000_000L))
    assertEquals("Satu Miliar Tiga Ratus Lima Puluh Juta Rupiah", TerbilangHelper.konversi(1_350_000_000L))
    assertEquals("Tujuh Ratus Delapan Puluh Ribu Rupiah", TerbilangHelper.konversi(780_000L))
    assertEquals("Nol Rupiah", TerbilangHelper.konversi(0L))
  }

  @Test
  fun testTaxEnginePphFinal() {
    val hargaTransaksi = 1_000_000_000L
    val pph = TaxEngine.hitungPphFinalPengalihan(hargaTransaksi)
    // 2.5% dari 1 Miliar = 25 Juta
    assertEquals(25_000_000L, pph)
  }

  @Test
  fun testTaxEnginePpnDtp() {
    val hargaRumah = 1_500_000_000L
    val res = TaxEngine.hitungPpnDtp(hargaRumah, 100.0)
    assertEquals(165_000_000L, res.ppnTotal)
    assertEquals(165_000_000L, res.ppnDitanggungPemerintah)
    assertEquals(0L, res.ppnDibayarCustomer)
  }

  @Test
  fun testAccountingEngineBalancedJournal() {
    val sampleTx = listOf(
      TransaksiKasBankRecord(
        id = "TX-01",
        tanggal = "2026-10-01",
        tipe = "MASUK",
        rekeningBank = "BCA Operasional",
        projectId = "PRJ-001",
        costCode = "",
        akunEfektif = "3100",
        pihakId = "PHK-01",
        pihakNama = "Owner",
        nominal = 500_000_000L,
        keterangan = "Setoran modal",
        docRef = "DOC-01",
        dibuatOleh = "Operator",
        disetujuiOleh1 = "Director",
        statusInput = "SUBMITTED",
        statusSistem = "POSTED",
        isPosted = true
      ),
      TransaksiKasBankRecord(
        id = "TX-02",
        tanggal = "2026-10-02",
        tipe = "KELUAR",
        rekeningBank = "BCA Operasional",
        projectId = "PRJ-001",
        costCode = "BLD-001",
        akunEfektif = "1410",
        pihakId = "PHK-02",
        pihakNama = "Vendor Semen",
        nominal = 100_000_000L,
        keterangan = "Beli material semen",
        docRef = "DOC-02",
        dibuatOleh = "Operator",
        disetujuiOleh1 = "Director",
        statusInput = "SUBMITTED",
        statusSistem = "POSTED",
        isPosted = true
      )
    )

    val sampleCoa = listOf(
      CoaAccount("1110", "BCA Operasional", "Aset", "Debit", true),
      CoaAccount("1410", "WIP Bangunan", "Aset", "Debit", true),
      CoaAccount("3100", "Modal Disetor", "Ekuitas", "Kredit", true)
    )

    val report = AccountingEngine.generateAutomaticJournal(sampleTx, sampleCoa)
    assertTrue(report.isBalanced)
    assertEquals(0L, report.selisih)
    assertEquals(600_000_000L, report.totalDebit)
    assertEquals(600_000_000L, report.totalKredit)
  }

  @Test
  fun testCsvSyncHelperExportAndParse() {
    val samplePayroll = listOf(
      PayrollBuruhRecord(
        id = 1L,
        kode = "E",
        nama = "Eeng",
        pekerjaan = "Buruh Lapangan",
        jenisGaji = "Harian",
        gajiPerHari = 120000L,
        hariKerja = 6.5,
        totalGaji = 780000L,
        tanggal = "2026-09-19",
        status = "Bayar",
        noWa = "6281318575529",
        catatan = "6 hari kerja periode 13-19 Sep"
      )
    )

    val csv = CsvSyncHelper.exportPayrollToCsv(samplePayroll)
    assertTrue(csv.contains("Eeng"))
    assertTrue(csv.contains("780000"))

    val parsed = CsvSyncHelper.parsePayrollFromCsv(csv)
    assertEquals(1, parsed.size)
    assertEquals("Eeng", parsed[0].nama)
    assertEquals(780000L, parsed[0].totalGaji)
  }

  @Test
  fun testConsolidationEngine() {
    val ptList = listOf(
      MasterPT("PT-01", "PT Gema Abadi Nugraha", "123", "456", "Ciamis", "BCA", "Induk"),
      MasterPT("PT-02", "PT Setia Surya Nugraha", "987", "654", "Tasik", "BCA", "Anak")
    )
    val prjList = listOf(
      MasterProyek("PRJ-01", "Gunung Padang", "PT-01", 120, 10_000_000_000L, "Ciamis", "BERJALAN"),
      MasterProyek("PRJ-02", "Green Surya", "PT-02", 50, 5_000_000_000L, "Tasik", "BERJALAN")
    )
    val txList = listOf(
      TransaksiKasBankRecord("TX-01", "2026-10-01", "MASUK", "BCA", "PRJ-01", "", "3100", "PHK", "Owner", 1_000_000_000L, "Modal", "DOC", "Op", "Dir", statusSistem = "POSTED"),
      TransaksiKasBankRecord("TX-02", "2026-10-01", "MASUK", "BCA", "PRJ-02", "", "3100", "PHK", "Owner", 500_000_000L, "Modal", "DOC", "Op", "Dir", statusSistem = "POSTED")
    )

    val summary = ConsolidationEngine.calculateConsolidation(
      ptList,
      prjList,
      txList,
      emptyList(),
      emptyList(),
      emptyList()
    )

    assertEquals(2, summary.perEntityList.size)
    assertEquals(1_500_000_000L, summary.totalKasBankGroup)
  }

  @Test
  fun testAiPriceAuditorFairPrice() {
    // Semen wajar: 100 sak seharga 6.500.000 (Rp 65.000/sak)
    val analysis = AiPriceAuditor.audit(
      itemName = "Semen Portland Komposit",
      totalNominal = 6_500_000L,
      volume = 100.0,
      costCode = "BLD-001",
      satuanInput = "Sak"
    )
    assertFalse(analysis.isAlarmTriggered)
    assertEquals("NORMAL_WAJAR", analysis.alarmSeverity)
    assertTrue(analysis.deviationPercent <= 0.0)
  }

  @Test
  fun testAiPriceAuditorAlarmNominalMarkup() {
    // Semen markup abnormal: 100 sak seharga 11.500.000 (Rp 115.000/sak, standar max 75.000)
    val analysis = AiPriceAuditor.audit(
      itemName = "Semen Portland Komposit",
      totalNominal = 11_500_000L,
      volume = 100.0,
      costCode = "BLD-001",
      satuanInput = "Sak"
    )
    assertTrue(analysis.isAlarmTriggered)
    assertEquals("ALARM_NOMINAL_MARKUP", analysis.alarmSeverity)
    assertTrue(analysis.deviationPercent > 15.0)
    assertTrue(analysis.statusLabel.contains("ALARM NOMINAL"))
    assertTrue(analysis.recommendation.contains("TAHAN PEMBAYARAN"))
  }

  @Test
  fun testAiExpenseAnomalyAuditorCategoryMismatch() {
    val sampleTx = listOf(
      TransaksiKasBankRecord(
        id = "TX-MISMATCH",
        tanggal = "2026-10-02",
        tipe = "KELUAR",
        rekeningBank = "BCA Operasional",
        projectId = "PRJ-001",
        costCode = "OVH-001", // Overhead padahal beli semen fisik
        akunEfektif = "6100",
        pihakId = "PHK-02",
        pihakNama = "Toko Material Ciamis",
        nominal = 25_000_000L,
        keterangan = "Pembelian semen gresik 300 sak",
        docRef = "INV-100",
        dibuatOleh = "Operator",
        disetujuiOleh1 = "Director"
      )
    )

    val sampleRab = listOf(
      AnggaranProyek(
        id = "ANG-01",
        projectId = "PRJ-001",
        costCode = "OVH-001",
        costCodeNama = "Beban Operasional Kantor",
        budgetAwal = 50_000_000L,
        actualPosted = 0L
      )
    )

    val report = AiExpenseAnomalyAuditor.auditTransactions(sampleTx, sampleRab)
    assertTrue(report.anomaliesCount > 0)
    val anomaly = report.items.find { it.txId == "TX-MISMATCH" }
    assertNotNull(anomaly)
    assertEquals(AnomalyType.CATEGORY_MISMATCH, anomaly?.anomalyType)
  }

  @Test
  fun testAiExpenseAnomalyAuditorBudgetOverrun() {
    val sampleTx = listOf(
      TransaksiKasBankRecord(
        id = "TX-OVERRUN",
        tanggal = "2026-10-02",
        tipe = "KELUAR",
        rekeningBank = "BCA Operasional",
        projectId = "PRJ-001",
        costCode = "BLD-001",
        akunEfektif = "1410",
        pihakId = "PHK-02",
        pihakNama = "Vendor Besi",
        nominal = 80_000_000L, // Lebih dari sisa budget (50jt)
        keterangan = "Besi beton konstruksi",
        docRef = "INV-200",
        dibuatOleh = "Operator",
        disetujuiOleh1 = "Director"
      )
    )

    val sampleRab = listOf(
      AnggaranProyek(
        id = "ANG-BLD",
        projectId = "PRJ-001",
        costCode = "BLD-001",
        costCodeNama = "Pekerjaan Struktur Beton",
        budgetAwal = 100_000_000L,
        actualPosted = 50_000_000L // sisa budget 50jt
      )
    )

    val report = AiExpenseAnomalyAuditor.auditTransactions(sampleTx, sampleRab)
    assertTrue(report.criticalCount > 0)
    val anomaly = report.items.find { it.txId == "TX-OVERRUN" }
    assertNotNull(anomaly)
    assertEquals(AnomalyType.BUDGET_OVERRUN, anomaly?.anomalyType)
    assertEquals(AnomalySeverity.CRITICAL, anomaly?.severity)
  }

  @Test
  fun testProjectProfitabilityEngine() {
    val proyek = MasterProyek(
      id = "PRJ-001",
      nama = "Gunung Padang Village",
      entityId = "PT-001",
      unitCount = 100,
      totalBudget = 50_000_000_000L,
      lokasi = "Ciamis",
      status = "BERJALAN"
    )

    val unitList = listOf(
      MasterUnit("U-01", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "READY", "SHM Terbit", "SOLD", 500_000_000L),
      MasterUnit("U-02", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "CONSTRUCTION", "SHM Terbit", "AVAILABLE", 500_000_000L)
    )

    val profit = ProjectProfitabilityEngine.calculate(
      proyek = proyek,
      lahanList = emptyList(),
      unitList = unitList,
      transaksiList = emptyList(),
      kontrakList = emptyList(),
      pembayaranList = emptyList()
    )

    assertEquals("PRJ-001", profit.projectId)
    assertEquals(1, profit.unitSold)
    assertEquals(2, profit.totalUnits)
    assertEquals(1_000_000_000L, profit.projectedSalesRevenue)
    assertEquals(0L, profit.contractedSalesRevenue)
  }
}
