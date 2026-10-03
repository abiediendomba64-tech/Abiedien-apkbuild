package com.example.domain

import com.example.data.*

data class EntityFinancialSummary(
    val entityId: String,
    val namaPT: String,
    val peran: String, // "Induk", "Anak"
    val saldoKasBank: Long,
    val pendapatanUnit: Long,
    val pengeluaranWipKonstruksi: Long,
    val bebanOverheadPayroll: Long,
    val piutangIntercompany: Long,
    val hutangIntercompany: Long,
    val labaBersih: Long
)

data class ConsolidatedGroupSummary(
    val totalKasBankGroup: Long,
    val totalPendapatanKonsolidasi: Long,
    val totalBebanWipKonsolidasi: Long,
    val totalOverheadKonsolidasi: Long,
    val eliminasiIntercompany: Long, // Eliminasi transaksi saldo timbal balik
    val totalAsetProyekKonsolidasi: Long,
    val labaBersihGroup: Long,
    val profitMarginPercent: Double,
    val perEntityList: List<EntityFinancialSummary>
)

object ConsolidationEngine {

    fun calculateConsolidation(
        ptList: List<MasterPT>,
        proyekList: List<MasterProyek>,
        transaksiList: List<TransaksiKasBankRecord>,
        intercompanyList: List<IntercompanyRecord>,
        kontrakList: List<PenjualanUnitContract>,
        pembayaranList: List<PembayaranCustomerRecord>
    ): ConsolidatedGroupSummary {
        val postedTx = transaksiList.filter { it.statusSistem == "POSTED" }

        // Map projects to their entity ID
        val projectEntityMap = proyekList.associate { it.id to it.entityId }

        val entitySummaries = ptList.map { pt ->
            // Transactions belonging to this entity
            val entityTx = postedTx.filter { tx ->
                val txEntity = projectEntityMap[tx.projectId] ?: "PT-001"
                txEntity == pt.id
            }

            val masuk = entityTx.filter { it.tipe == "MASUK" }.sumOf { it.nominal }
            val keluar = entityTx.filter { it.tipe == "KELUAR" }.sumOf { it.nominal }
            val saldoKas = masuk - keluar

            // Revenue from customer payments belonging to this entity's projects
            val entityProjects = proyekList.filter { it.entityId == pt.id }.map { it.id }.toSet()
            val entityKontrak = kontrakList.filter { it.projectId in entityProjects }
            val entityKontrakNos = entityKontrak.map { it.noKontrak }.toSet()

            val pendapatan = pembayaranList.filter { it.noKontrak in entityKontrakNos && it.status == "VALID" }
                .sumOf { it.nominal }

            // WIP expenses (Cost codes LND, INF, BLD with accounts 1410/1420)
            val wip = entityTx.filter {
                it.tipe == "KELUAR" && (it.akunEfektif == "1410" || it.akunEfektif == "1420" || it.costCode.startsWith("BLD") || it.costCode.startsWith("INF") || it.costCode.startsWith("LND"))
            }.sumOf { it.nominal }

            // Overhead expenses (OVH, 6100)
            val overhead = entityTx.filter {
                it.tipe == "KELUAR" && (it.akunEfektif == "6100" || it.costCode.startsWith("OVH"))
            }.sumOf { it.nominal }

            // Intercompany balance
            val piutangIc = if (pt.peran == "Induk") {
                intercompanyList.filter { it.entitySumber == pt.nama }.sumOf { it.outstanding }
            } else 0L

            val hutangIc = if (pt.peran == "Anak") {
                intercompanyList.filter { it.entityTujuan == pt.nama }.sumOf { it.outstanding }
            } else 0L

            val laba = pendapatan - overhead

            EntityFinancialSummary(
                entityId = pt.id,
                namaPT = pt.nama,
                peran = pt.peran,
                saldoKasBank = saldoKas,
                pendapatanUnit = pendapatan,
                pengeluaranWipKonstruksi = wip,
                bebanOverheadPayroll = overhead,
                piutangIntercompany = piutangIc,
                hutangIntercompany = hutangIc,
                labaBersih = laba
            )
        }

        // Consolidated elimination:
        // Intercompany receivables (1230) and payables (2430) between parent & child cancel out in consolidation!
        val totalIntercompanyEliminasi = intercompanyList.sumOf { it.outstanding }

        val totalKas = entitySummaries.sumOf { it.saldoKasBank }
        val totalPendapatan = entitySummaries.sumOf { it.pendapatanUnit }
        val totalWip = entitySummaries.sumOf { it.pengeluaranWipKonstruksi }
        val totalOverhead = entitySummaries.sumOf { it.bebanOverheadPayroll }
        val totalAset = totalKas + totalWip
        val totalLaba = totalPendapatan - totalOverhead

        val margin = if (totalPendapatan > 0) {
            (totalLaba.toDouble() / totalPendapatan.toDouble()) * 100.0
        } else 0.0

        return ConsolidatedGroupSummary(
            totalKasBankGroup = totalKas,
            totalPendapatanKonsolidasi = totalPendapatan,
            totalBebanWipKonsolidasi = totalWip,
            totalOverheadKonsolidasi = totalOverhead,
            eliminasiIntercompany = totalIntercompanyEliminasi,
            totalAsetProyekKonsolidasi = totalAset,
            labaBersihGroup = totalLaba,
            profitMarginPercent = margin,
            perEntityList = entitySummaries
        )
    }
}
