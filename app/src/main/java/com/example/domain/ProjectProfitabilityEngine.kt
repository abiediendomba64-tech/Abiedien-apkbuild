package com.example.domain

import com.example.data.*
import com.example.ui.components.toRupiah

data class CostOptimizationItem(
    val category: String,
    val title: String,
    val potentialSavingOrImpact: String,
    val recommendation: String,
    val priority: String // "TINGGI", "SEDANG", "INFO"
)

data class ProjectProfitability(
    val projectId: String,
    val namaProyek: String,
    val entityId: String,
    val lokasi: String,
    val totalUnits: Int,
    val unitSold: Int,
    val unitBooked: Int,
    val unitAvailable: Int,
    val projectedSalesRevenue: Long, // Nilai total proyeksi penjualan seluruh unit
    val contractedSalesRevenue: Long, // Nilai kontrak terjual / terpesan
    val cashReceivedRevenue: Long, // Kas riil masuk dari konsumen
    val uncollectedReceivables: Long, // Piutang konsumen yang belum tertagih
    
    // Rincian Biaya HPP Proyek
    val landCost: Long, // Pembebasan Lahan + PPh + BPHTB
    val infraCost: Long, // Pematangan lahan, drainase, jalan (INF)
    val buildingCost: Long, // Konstruksi struktur, dinding, atap, finishing (BLD)
    val marketingCost: Long, // Komisi agen & biaya promosi (MKT)
    val overheadPayrollCost: Long, // Gaji staf & upah operasional (OVH)
    val totalProjectCost: Long, // Total HPP Investasi
    
    // Profitabilitas & Margin
    val grossProfit: Long, // Laba Kotor (Revenue - Lahan - Infra - Bangunan)
    val grossProfitMargin: Double, // %
    val netProfit: Long, // Laba Bersih Proyek
    val netProfitMargin: Double, // %
    val realizedNetProfit: Long, // Kas masuk riil - Pengeluaran kas riil
    val roiPercent: Double, // Return on Investment %
    val breakEvenUnits: Int, // Titik Impas (Jumlah unit BEP)
    
    // Rekomendasi Optimasi Biaya
    val costOptimizationInsights: List<CostOptimizationItem>
)

object ProjectProfitabilityEngine {

    fun calculate(
        proyek: MasterProyek,
        lahanList: List<MasterLahan>,
        unitList: List<MasterUnit>,
        transaksiList: List<TransaksiKasBankRecord>,
        kontrakList: List<PenjualanUnitContract>,
        pembayaranList: List<PembayaranCustomerRecord>
    ): ProjectProfitability {
        val projectUnits = unitList.filter { it.projectId == proyek.id }
        val projectLahan = lahanList.filter { it.projectId == proyek.id }
        val projectTx = transaksiList.filter { it.projectId == proyek.id && it.statusSistem == "POSTED" }
        val projectKontrak = kontrakList.filter { it.projectId == proyek.id }
        val projectKontrakNos = projectKontrak.map { it.noKontrak }.toSet()
        val projectPembayaran = pembayaranList.filter { it.noKontrak in projectKontrakNos && it.status == "VALID" }

        val totalUnits = if (projectUnits.isNotEmpty()) projectUnits.size else proyek.unitCount
        val unitSold = projectUnits.count { it.statusPenjualan == "SOLD" }
        val unitBooked = projectUnits.count { it.statusPenjualan == "BOOKED" }
        val unitAvailable = projectUnits.count { it.statusPenjualan == "AVAILABLE" }.let {
            if (projectUnits.isEmpty()) proyek.unitCount else it
        }

        val projectedSalesRevenue = if (projectUnits.isNotEmpty()) {
            projectUnits.sumOf { it.hargaJual }
        } else {
            proyek.unitCount * 320_000_000L
        }

        val contractedSalesRevenue = projectKontrak.sumOf { it.hargaNett }
        val cashReceivedRevenue = projectPembayaran.sumOf { it.nominal }
        val uncollectedReceivables = if (contractedSalesRevenue > cashReceivedRevenue) contractedSalesRevenue - cashReceivedRevenue else 0L

        // Land costs: Pembebasan + PPh + BPHTB or LND transactions
        val landFromLahan = projectLahan.sumOf { it.hargaPembebasan + it.pphPenjual + it.bphtb }
        val landFromTx = projectTx.filter { it.tipe == "KELUAR" && (it.costCode.startsWith("LND") || it.akunEfektif == "1420") }.sumOf { it.nominal }
        val landCost = maxOf(landFromLahan, landFromTx, 2_450_000_000L)

        // Infrastructure
        val infraFromTx = projectTx.filter { it.tipe == "KELUAR" && it.costCode.startsWith("INF") }.sumOf { it.nominal }
        val infraCost = if (infraFromTx > 0) infraFromTx else (totalUnits * 16_000_000L)

        // Building
        val bldFromTx = projectTx.filter { it.tipe == "KELUAR" && it.costCode.startsWith("BLD") }.sumOf { it.nominal }
        val buildingCost = if (bldFromTx > 0) bldFromTx else (totalUnits * 110_000_000L)

        // Marketing & Commission
        val mktFromTx = projectTx.filter { it.tipe == "KELUAR" && (it.costCode.startsWith("MKT") || it.tipeTransaksiKhusus == "KOMISI_MARKETING") }.sumOf { it.nominal }
        val marketingCost = if (mktFromTx > 0) mktFromTx else (projectedSalesRevenue * 0.025).toLong()

        // Overhead & Payroll
        val ovhFromTx = projectTx.filter { it.tipe == "KELUAR" && (it.costCode.startsWith("OVH") || it.akunEfektif == "6100") }.sumOf { it.nominal }
        val overheadPayrollCost = if (ovhFromTx > 0) ovhFromTx else 420_000_000L

        val totalProjectCost = landCost + infraCost + buildingCost + marketingCost + overheadPayrollCost
        val grossProfit = projectedSalesRevenue - (landCost + infraCost + buildingCost)
        val grossProfitMargin = if (projectedSalesRevenue > 0) (grossProfit.toDouble() / projectedSalesRevenue.toDouble()) * 100.0 else 0.0

        val netProfit = projectedSalesRevenue - totalProjectCost
        val netProfitMargin = if (projectedSalesRevenue > 0) (netProfit.toDouble() / projectedSalesRevenue.toDouble()) * 100.0 else 0.0

        val realizedExpenses = projectTx.filter { it.tipe == "KELUAR" }.sumOf { it.nominal }
        val realizedNetProfit = cashReceivedRevenue - realizedExpenses

        val roiPercent = if (totalProjectCost > 0) (netProfit.toDouble() / totalProjectCost.toDouble()) * 100.0 else 0.0

        val avgUnitPrice = if (totalUnits > 0) projectedSalesRevenue / totalUnits else 300_000_000L
        val breakEvenUnits = if (avgUnitPrice > 0) (totalProjectCost / avgUnitPrice).toInt().coerceAtMost(totalUnits) else 0

        // Optimization insights
        val insights = mutableListOf<CostOptimizationItem>()

        if (uncollectedReceivables > 100_000_000L) {
            insights.add(
                CostOptimizationItem(
                    category = "Piutang Konsumen (KPR)",
                    title = "Akselerasi Pencairan Akad Kredit Bank",
                    potentialSavingOrImpact = "Likuiditas Tertahan: ${uncollectedReceivables.toRupiah()}",
                    recommendation = "Kawal kelengkapan splitzing sertifikat SHM & PBG ke kantor notaris rekanan bank agar SP3K segera cair ke rekening escrow developer.",
                    priority = "TINGGI"
                )
            )
        }

        insights.add(
            CostOptimizationItem(
                category = "Pengadaan Material Konstruksi",
                title = "Kontrak Payung Material Semen & Baja Tulangan",
                potentialSavingOrImpact = "Potensi Efisiensi: 6% - 9% pos BLD (~${(buildingCost * 0.07).toLong().toRupiah()})",
                recommendation = "Lakukan negosiasi pasokan batching plant semen dan pabrikasi besi langsung ke distributor resmi untuk memotong rantai mark-up subkontraktor.",
                priority = "SEDANG"
            )
        )

        insights.add(
            CostOptimizationItem(
                category = "Kontraktor & SPK",
                title = "Penerapan Retensi 5% & Termin Berbasis BAST Fisik",
                potentialSavingOrImpact = "Proteksi Kualitas & Garansi Bangunan",
                recommendation = "Pencairan termin konstruksi wajib diverifikasi oleh Site Manager dengan bukti foto progres fisik dan penahanan retensi selama 100 hari.",
                priority = "INFO"
            )
        )

        if (unitAvailable > 0) {
            insights.add(
                CostOptimizationItem(
                    category = "Penjualan & Stok Unit",
                    title = "Optimalisasi Stok Kavling Tersedia ($unitAvailable Unit)",
                    potentialSavingOrImpact = "Proyeksi Tambahan Kas: ${(unitAvailable * avgUnitPrice).toRupiah()}",
                    recommendation = "Maksimalkan fasilitas insentif PPN DTP 100% dari pemerintah dengan kampanye digital marketing kavling siap bangun.",
                    priority = "TINGGI"
                )
            )
        }

        return ProjectProfitability(
            projectId = proyek.id,
            namaProyek = proyek.nama,
            entityId = proyek.entityId,
            lokasi = proyek.lokasi,
            totalUnits = totalUnits,
            unitSold = unitSold,
            unitBooked = unitBooked,
            unitAvailable = unitAvailable,
            projectedSalesRevenue = projectedSalesRevenue,
            contractedSalesRevenue = contractedSalesRevenue,
            cashReceivedRevenue = cashReceivedRevenue,
            uncollectedReceivables = uncollectedReceivables,
            landCost = landCost,
            infraCost = infraCost,
            buildingCost = buildingCost,
            marketingCost = marketingCost,
            overheadPayrollCost = overheadPayrollCost,
            totalProjectCost = totalProjectCost,
            grossProfit = grossProfit,
            grossProfitMargin = grossProfitMargin,
            netProfit = netProfit,
            netProfitMargin = netProfitMargin,
            realizedNetProfit = realizedNetProfit,
            roiPercent = roiPercent,
            breakEvenUnits = breakEvenUnits,
            costOptimizationInsights = insights
        )
    }
}
