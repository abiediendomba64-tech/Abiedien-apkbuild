package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MasterUnit
import com.example.data.PembayaranCustomerRecord
import com.example.domain.ProjectProfitability
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun PenjualanUnitScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val unitList by viewModel.allUnit.collectAsStateWithLifecycle()
    val kontrakList by viewModel.allKontrak.collectAsStateWithLifecycle()
    val proyekList by viewModel.allProyek.collectAsStateWithLifecycle()
    val profitabilities by viewModel.projectProfitabilities.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Inventori Real-Time, 1: Analisis Profitabilitas Proyek, 2: Kontrak & Piutang

    // Inventory filter state
    var inventoryFilter by remember { mutableStateOf("SEMUA") }
    var showAddUnitDialog by remember { mutableStateOf(false) }

    // Payment state
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedKontrakNo by remember { mutableStateOf("") }
    var paymentNominalText by remember { mutableStateOf("10000000") }
    var paymentType by remember { mutableStateOf("DP") }

    // Project Profitability Selector
    var selectedProjectIdForAnalysis by remember { mutableStateOf(proyekList.firstOrNull()?.id ?: "PRJ-001") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Stok Unit (${unitList.size})") },
                icon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Analisis Profit & ROI") },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Kontrak (${kontrakList.size})") },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // TAB 0: REAL-TIME INVENTORY TRACKING
            val filteredUnits = when (inventoryFilter) {
                "AVAILABLE" -> unitList.filter { it.statusPenjualan == "AVAILABLE" }
                "BOOKED" -> unitList.filter { it.statusPenjualan == "BOOKED" }
                "SOLD" -> unitList.filter { it.statusPenjualan == "SOLD" }
                "READY" -> unitList.filter { it.statusPembangunan == "READY" }
                "CONSTRUCTION" -> unitList.filter { it.statusPembangunan == "CONSTRUCTION" }
                else -> unitList
            }

            val totalStockValue = unitList.sumOf { it.hargaJual }
            val soldValue = unitList.filter { it.statusPenjualan == "SOLD" }.sumOf { it.hargaJual }
            val availableValue = unitList.filter { it.statusPenjualan == "AVAILABLE" }.sumOf { it.hargaJual }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "INVENTORI STOK UNIT REAL-TIME",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Pelacakan fisik kavling, sertifikat SHM, kesiapan huni & estimasi nilai aset.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Total Unit:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("${unitList.size} Unit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                                Column {
                                    Text("Terjual (Sold):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("${unitList.count { it.statusPenjualan == "SOLD" }} Unit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreenLight)
                                }
                                Column {
                                    Text("Dipesan (Booked):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("${unitList.count { it.statusPenjualan == "BOOKED" }} Unit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AccentGoldLight)
                                }
                                Column {
                                    Text("Tersedia (Ready):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text("${unitList.count { it.statusPenjualan == "AVAILABLE" }} Unit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nilai Stok Tersedia:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                Text(availableValue.toRupiah(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AccentGoldLight)
                            }
                        }
                    }
                }

                // Action Bar: Filter & Add Unit Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Ketersediaan:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Button(
                            onClick = { showAddUnitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Unit", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SEMUA", "AVAILABLE", "BOOKED", "SOLD", "READY", "CONSTRUCTION").forEach { flt ->
                            FilterChip(
                                selected = inventoryFilter == flt,
                                onClick = { inventoryFilter = flt },
                                label = { Text(flt, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                items(filteredUnits) { unit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_unit_${unit.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NavyPrimary
                                    ) {
                                        Text(
                                            text = unit.id,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = unit.tipeRumah,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "LT: ${unit.luasTanahM2.toInt()}m² • LB: ${unit.luasBangunanM2.toInt()}m²",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                StatusBadge(status = unit.statusPenjualan)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fisik: ${unit.statusPembangunan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Legal: ${unit.statusLegal}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = AccentGold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Harga Jual:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(unit.hargaJual.toRupiah(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (unit.statusPenjualan != "SOLD") {
                                        OutlinedButton(
                                            onClick = {
                                                val nextStatus = when (unit.statusPenjualan) {
                                                    "AVAILABLE" -> "BOOKED"
                                                    "BOOKED" -> "SOLD"
                                                    else -> "AVAILABLE"
                                                }
                                                viewModel.updateUnitStatus(unit.copy(statusPenjualan = nextStatus))
                                            },
                                            modifier = Modifier.height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text(if (unit.statusPenjualan == "AVAILABLE") "Set Booked" else "Set Sold", fontSize = 11.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val nextFisik = when (unit.statusPembangunan) {
                                                "PLANNING" -> "CONSTRUCTION"
                                                "CONSTRUCTION" -> "READY"
                                                else -> "READY"
                                            }
                                            viewModel.updateUnitStatus(unit.copy(statusPembangunan = nextFisik))
                                        },
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(if (unit.statusPembangunan == "CONSTRUCTION") "Set Ready" else "Progres", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: PROJECT PROFITABILITY & ROI ANALYSIS
            val activeProfitability = profitabilities.find { it.projectId == selectedProjectIdForAnalysis }
                ?: profitabilities.firstOrNull()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Project Selector Chips
                item {
                    Text("Pilih Proyek Analisis:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        proyekList.forEach { prj ->
                            FilterChip(
                                selected = selectedProjectIdForAnalysis == prj.id,
                                onClick = { selectedProjectIdForAnalysis = prj.id },
                                label = { Text(prj.nama) }
                            )
                        }
                    }
                }

                if (activeProfitability != null) {
                    val p = activeProfitability

                    // Profitability Hero Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profitability_hero_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyDark)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = p.namaProyek,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Lokasi: ${p.lokasi} • Total: ${p.totalUnits} Kavling",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentGoldLight
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (p.roiPercent >= 20.0) StatusGreen else AccentGold
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("ROI PROYEK", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                            Text(
                                                text = "${"%.1f".format(p.roiPercent)}%",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Proyeksi Pendapatan (Gross):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text(p.projectedSalesRevenue.toRupiah(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total HPP Proyek (Cost):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text(p.totalProjectCost.toRupiah(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Laba Bersih Proyeksi (Net):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text(p.netProfit.toRupiah(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreenLight)
                                        Text("Margin Laba Bersih: ${"%.1f".format(p.netProfitMargin)}%", style = MaterialTheme.typography.labelSmall, color = StatusGreenLight)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Titik Impas (BEP):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("${p.breakEvenUnits} dari ${p.totalUnits} Unit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = AccentGoldLight)
                                        Text("Kas Riil Masuk: ${p.cashReceivedRevenue.toRupiah()}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }

                    // Detailed Cost Breakdown Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rincian Struktur Biaya HPP Proyek",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                CostItemRow("1. Pembebasan Tanah & Legalitas Lahan (LND)", p.landCost, p.totalProjectCost)
                                CostItemRow("2. Cut & Fill, Drainase & Infrastruktur (INF)", p.infraCost, p.totalProjectCost)
                                CostItemRow("3. Konstruksi Bangunan Rumah (BLD)", p.buildingCost, p.totalProjectCost)
                                CostItemRow("4. Biaya Pemasaran, Komisi Agen & Iklan (MKT)", p.marketingCost, p.totalProjectCost)
                                CostItemRow("5. Gaji Lapangan, Absensi & Overhead (OVH)", p.overheadPayrollCost, p.totalProjectCost)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Laba Kotor (Gross Profit):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("${p.grossProfit.toRupiah()} (${"%.1f".format(p.grossProfitMargin)}%)", fontWeight = FontWeight.Bold, color = StatusGreen)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Piutang Konsumen Belum Tertagih:", style = MaterialTheme.typography.bodySmall)
                                    Text(p.uncollectedReceivables.toRupiah(), fontWeight = FontWeight.SemiBold, color = if (p.uncollectedReceivables > 0) StatusRed else StatusGreen)
                                }
                            }
                        }
                    }

                    // Cost Optimization & ROI Insights Section
                    item {
                        SectionHeader(
                            title = "Wawasan Optimasi Biaya & Profit (Insights)",
                            subtitle = "Strategi Penghematan HPP & Percepatan Arus Kas"
                        )
                    }

                    items(p.costOptimizationInsights) { ins ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (ins.priority) {
                                    "TINGGI" -> StatusRedLight.copy(alpha = 0.5f)
                                    "SEDANG" -> AccentGoldLight.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ins.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NavyDark
                                    )
                                    StatusBadge(status = ins.priority)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bidang: ${ins.category} • ${ins.potentialSavingOrImpact}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ins.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val reportText = "=== LAPORAN ANALISIS PROFITABILITAS & ROI PROYEK ===\n" +
                                        "Proyek: ${p.namaProyek} (${p.lokasi})\n" +
                                        "Total Unit: ${p.totalUnits} (Terjual: ${p.unitSold}, Dipesan: ${p.unitBooked}, Tersedia: ${p.unitAvailable})\n" +
                                        "--------------------------------------------------\n" +
                                        "Proyeksi Pendapatan Penjualan: ${p.projectedSalesRevenue.toRupiah()}\n" +
                                        "Realisasi Kas Masuk Konsumen: ${p.cashReceivedRevenue.toRupiah()}\n" +
                                        "Piutang Belum Tertagih: ${p.uncollectedReceivables.toRupiah()}\n" +
                                        "Total Biaya HPP Proyek: ${p.totalProjectCost.toRupiah()}\n" +
                                        "Laba Kotor (Gross Profit): ${p.grossProfit.toRupiah()} (${"%.1f".format(p.grossProfitMargin)}%)\n" +
                                        "Laba Bersih (Net Profit): ${p.netProfit.toRupiah()} (${"%.1f".format(p.netProfitMargin)}%)\n" +
                                        "ROI (Return on Investment): ${"%.1f".format(p.roiPercent)}%\n" +
                                        "Break-Even Point (BEP): ${p.breakEvenUnits} Unit\n" +
                                        "=================================================="

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Laporan Profit Proyek", reportText)
                                clipboard.setPrimaryClip(clip)
                                viewModel.userFeedbackMessage.value = "Laporan Profitabilitas Proyek berhasil disalin!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Ringkasan Profitabilitas Proyek")
                        }
                    }
                }
            }
        } else {
            // TAB 2: KONTRAK PENJUALAN & PIUTANG CUSTOMER
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daftar Kontrak Konsumen",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Pelacakan booking fee, cicilan DP, pencairan KPR & piutang.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                selectedKontrakNo = kontrakList.firstOrNull()?.noKontrak ?: ""
                                showPaymentDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catat Bayar")
                        }
                    }
                }

                items(kontrakList) { ktr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${ktr.noKontrak} • Unit ${ktr.unitId}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                StatusBadge(status = ktr.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Customer: ${ktr.customerNama} • Skema: ${ktr.skema}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = "Harga Nett: ${ktr.hargaNett.toRupiah()} • Terbayar: ${ktr.terbayarValid.toRupiah()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Sisa Piutang: ${ktr.sisaTagihan.toRupiah()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (ktr.sisaTagihan <= 0) StatusGreen else StatusRed
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Tambah Unit Baru ke Inventori
    if (showAddUnitDialog) {
        var unitIdInput by remember { mutableStateOf("A-0${unitList.size + 1}") }
        var tipeRumahInput by remember { mutableStateOf("Tipe 36/72") }
        var luasTanahInput by remember { mutableStateOf("72") }
        var luasBangunanInput by remember { mutableStateOf("36") }
        var hargaJualInput by remember { mutableStateOf("295000000") }
        var statusFisikInput by remember { mutableStateOf("CONSTRUCTION") }
        var statusLegalInput by remember { mutableStateOf("SHM Pecah") }
        var statusJualInput by remember { mutableStateOf("AVAILABLE") }

        AlertDialog(
            onDismissRequest = { showAddUnitDialog = false },
            title = { Text("Tambah Unit Kavling Baru", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = unitIdInput,
                            onValueChange = { unitIdInput = it.uppercase() },
                            label = { Text("Nomor Blok / Unit (misal: A-05, B-12)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Pilih Tipe Rumah:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Tipe 36/72", "Tipe 45/90", "Tipe 54/108").forEach { tp ->
                                FilterChip(
                                    selected = tipeRumahInput == tp,
                                    onClick = {
                                        tipeRumahInput = tp
                                        if (tp.contains("36/72")) {
                                            luasBangunanInput = "36"
                                            luasTanahInput = "72"
                                            hargaJualInput = "295000000"
                                        } else if (tp.contains("45/90")) {
                                            luasBangunanInput = "45"
                                            luasTanahInput = "90"
                                            hargaJualInput = "395000000"
                                        } else if (tp.contains("54/108")) {
                                            luasBangunanInput = "54"
                                            luasTanahInput = "108"
                                            hargaJualInput = "495000000"
                                        }
                                    },
                                    label = { Text(tp, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = luasTanahInput,
                                onValueChange = { luasTanahInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Luas Tanah (m²)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = luasBangunanInput,
                                onValueChange = { luasBangunanInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Luas Bangunan (m²)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = hargaJualInput,
                            onValueChange = { hargaJualInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Jual Unit (Rp)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Status Fisik Pembangunan:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("READY", "CONSTRUCTION", "PLANNING").forEach { st ->
                                FilterChip(
                                    selected = statusFisikInput == st,
                                    onClick = { statusFisikInput = st },
                                    label = { Text(st, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                    item {
                        Text("Status Sertifikat Legalitas:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("SHM Pecah", "SHM Induk", "AJB").forEach { lg ->
                                FilterChip(
                                    selected = statusLegalInput == lg,
                                    onClick = { statusLegalInput = lg },
                                    label = { Text(lg, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val unit = MasterUnit(
                            id = unitIdInput.ifBlank { "UNIT-${System.currentTimeMillis() % 1000}" },
                            projectId = selectedProjectIdForAnalysis,
                            tipeRumah = tipeRumahInput,
                            luasTanahM2 = luasTanahInput.toDoubleOrNull() ?: 72.0,
                            luasBangunanM2 = luasBangunanInput.toDoubleOrNull() ?: 36.0,
                            statusPembangunan = statusFisikInput,
                            statusLegal = statusLegalInput,
                            statusPenjualan = statusJualInput,
                            hargaJual = hargaJualInput.toLongOrNull() ?: 295_000_000L
                        )
                        viewModel.addNewUnit(unit)
                        showAddUnitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan Unit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUnitDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Catat Pembayaran Customer
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Penerimaan Pembayaran Customer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih No Kontrak Unit:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        kontrakList.forEach { ktr ->
                            FilterChip(
                                selected = selectedKontrakNo == ktr.noKontrak,
                                onClick = { selectedKontrakNo = ktr.noKontrak },
                                label = { Text("${ktr.unitId} (${ktr.customerNama})") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = paymentNominalText,
                        onValueChange = { paymentNominalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal Pembayaran (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Jenis Pembayaran:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("BOOKING_FEE", "DP", "KPR_CAIR", "PELUNASAN").forEach { jp ->
                            FilterChip(
                                selected = paymentType == jp,
                                onClick = { paymentType = jp },
                                label = { Text(jp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nom = paymentNominalText.toLongOrNull() ?: 0L
                        val ktr = kontrakList.find { it.noKontrak == selectedKontrakNo }
                        val record = PembayaranCustomerRecord(
                            receiptId = "RCT-${System.currentTimeMillis() % 10000}",
                            tanggal = "2026-10-03",
                            noKontrak = selectedKontrakNo,
                            unitId = ktr?.unitId ?: "A-01",
                            customerNama = ktr?.customerNama ?: "Customer",
                            jenisPembayaran = paymentType,
                            nominal = nom,
                            rekeningTujuan = "BCA Operasional",
                            voucherTxId = "TX-${System.currentTimeMillis() % 10000}",
                            status = "VALID"
                        )
                        viewModel.recordCustomerPayment(record)
                        showPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan Penerimaan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CostItemRow(title: String, amount: Long, totalCost: Long) {
    val pct = if (totalCost > 0) (amount.toDouble() / totalCost.toDouble()) * 100.0 else 0.0
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("${amount.toRupiah()} (${"%.1f".format(pct)}%)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
        }
        LinearProgressIndicator(
            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = NavyPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
