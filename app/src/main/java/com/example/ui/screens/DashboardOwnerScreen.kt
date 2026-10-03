package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AnomalySeverity
import com.example.domain.AnomalyType
import com.example.ui.components.CapitalDistributionDashboard
import com.example.ui.components.MetricCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@Composable
fun DashboardOwnerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val postedTx by viewModel.postedTransaksi.collectAsStateWithLifecycle()
    val allAnggaran by viewModel.allAnggaran.collectAsStateWithLifecycle()
    val allProyek by viewModel.allProyek.collectAsStateWithLifecycle()
    val auditChecks by viewModel.auditChecks.collectAsStateWithLifecycle()
    val consolidated by viewModel.consolidatedSummary.collectAsStateWithLifecycle()
    val selectedEntity by viewModel.selectedConsolidationEntity.collectAsStateWithLifecycle()
    val investorList by viewModel.allInvestors.collectAsStateWithLifecycle()
    val anomalyReport by viewModel.expenseAnomalyReport.collectAsStateWithLifecycle()
    val aiDeepAnomalyInsight by viewModel.aiDeepAnomalyInsight.collectAsStateWithLifecycle()
    val isDeepAnomalyLoading by viewModel.isDeepAnomalyLoading.collectAsStateWithLifecycle()

    val openAlarms = auditChecks.filter { !it.isOk }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Entity Filter Tabs (Consolidation Selector)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Entitas Pelaporan Keuangan:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedEntity == "ALL",
                            onClick = { viewModel.selectedConsolidationEntity.value = "ALL" },
                            label = { Text("Konsolidasi Grup", maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedEntity == "PT-001",
                            onClick = { viewModel.selectedConsolidationEntity.value = "PT-001" },
                            label = { Text("Induk (GAN)", maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedEntity == "PT-002",
                            onClick = { viewModel.selectedConsolidationEntity.value = "PT-002" },
                            label = { Text("Anak (SSN)", maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Hero Card: PT Gema Abadi Nugraha & PT Setia Surya Nugraha
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_dashboard_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedEntity) {
                                    "PT-001" -> "PT GEMA ABADI NUGRAHA (INDUK)"
                                    "PT-002" -> "PT SETIA SURYA NUGRAHA (ANAK PT)"
                                    else -> "KONSOLIDASI GRUP PROPERTI"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = when (selectedEntity) {
                                    "PT-001" -> "Proyek: Perumahan Gunung Padang, Ciamis"
                                    "PT-002" -> "Proyek: Green Surya Residence"
                                    else -> "Induk (PT-001) + Anak Usaha (PT-002)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGold
                        ) {
                            Text(
                                text = "SAK EP 2026",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (selectedEntity == "ALL") "Total Saldo Kas & Bank Konsolidasi" else "Saldo Kas & Bank Entitas",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (selectedEntity == "ALL") consolidated.totalKasBankGroup.toRupiah() else metrics.totalKasBank.toRupiah(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Laba Bersih Konsolidasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (selectedEntity == "ALL") consolidated.labaBersihGroup.toRupiah() else (metrics.totalKasBank / 2).toRupiah(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusGreenLight
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eliminasi Intercompany",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = consolidated.eliminasiIntercompany.toRupiah(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentGoldLight
                            )
                        }
                    }
                }
            }
        }

        // Consolidated Subsidiary Breakdown Cards (when ALL is selected)
        if (selectedEntity == "ALL" && consolidated.perEntityList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Perbandingan Keuangan Entitas Anak & Induk",
                    subtitle = "Laba/Rugi, Kas & Intercompany"
                )
            }
            items(consolidated.perEntityList) { ent ->
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
                            Column {
                                Text(
                                    text = ent.namaPT,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Status: ${ent.peran} • ID: ${ent.entityId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = if (ent.labaBersih >= 0) "PROFITABLE" else "DEFICIT")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kas/Bank:", style = MaterialTheme.typography.bodySmall)
                            Text(ent.saldoKasBank.toRupiah(), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pendapatan Unit:", style = MaterialTheme.typography.bodySmall)
                            Text(ent.pendapatanUnit.toRupiah(), fontWeight = FontWeight.SemiBold, color = StatusGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("WIP Konstruksi (Aset):", style = MaterialTheme.typography.bodySmall)
                            Text(ent.pengeluaranWipKonstruksi.toRupiah(), fontWeight = FontWeight.SemiBold)
                        }
                        if (ent.piutangIntercompany > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Piutang Intercompany (ke Anak):", style = MaterialTheme.typography.bodySmall)
                                Text(ent.piutangIntercompany.toRupiah(), fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                        }
                        if (ent.hutangIntercompany > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hutang Intercompany (ke Induk):", style = MaterialTheme.typography.bodySmall)
                                Text(ent.hutangIntercompany.toRupiah(), fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Laba Bersih:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text(ent.labaBersih.toRupiah(), fontWeight = FontWeight.Bold, color = if (ent.labaBersih >= 0) StatusGreen else StatusRed)
                        }
                    }
                }
            }
        }

        // Multi-Layered Capital Distribution Dashboard (Induk vs Anak vs Investor)
        item {
            CapitalDistributionDashboard(
                investorList = investorList,
                onAddInvestorClick = { viewModel.navigateTo(Screen.Transaksi) }
            )
        }

        // AI-Driven Expense Anomaly & RAB Category Auditor (Gemini AI)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_ai_expense_anomaly_dashboard"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (anomalyReport.criticalCount > 0) Color(0xFFFFF0F0) else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (anomalyReport.criticalCount > 0) StatusRed else NavyPrimary
                    ),
                    width = if (anomalyReport.criticalCount > 0) 1.5.dp else 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (anomalyReport.criticalCount > 0) Icons.Default.Warning else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (anomalyReport.criticalCount > 0) StatusRed else AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AUDIT ANOMALI BIAYA RAB (AI)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (anomalyReport.criticalCount > 0) StatusRed else NavyDark
                                )
                                Text(
                                    text = "Deteksi salah pos, overbudget & splitting tagihan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (anomalyReport.criticalCount > 0) StatusRedLight else StatusGreenLight
                        ) {
                            Text(
                                text = if (anomalyReport.criticalCount > 0) "${anomalyReport.criticalCount} KRITIS" else "${anomalyReport.anomaliesCount} Anomali",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (anomalyReport.criticalCount > 0) StatusRed else StatusGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = anomalyReport.auditSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.85f),
                        fontSize = 11.5.sp
                    )

                    if (aiDeepAnomalyInsight != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NavyLight
                        ) {
                            Text(
                                text = aiDeepAnomalyInsight!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = NavyDark,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Button to trigger Deep Forensic Analysis with Gemini
                    OutlinedButton(
                        onClick = { viewModel.requestDeepAiAnomalyAnalysis() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                    ) {
                        if (isDeepAnomalyLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NavyPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini AI Sedang Mengaudit Anomali...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analisa Forensik RAB dengan Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Anomaly Flagged Items List
                    if (anomalyReport.items.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            anomalyReport.items.take(4).forEach { item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (item.severity == AnomalySeverity.CRITICAL) StatusRedLight else Color(0xFFFFFBEA)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.costCode}: ${item.nominal.toRupiah()}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (item.severity == AnomalySeverity.CRITICAL) StatusRed else Color(0xFFB45309)
                                            )
                                            Text(
                                                text = item.anomalyType.name,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.severity == AnomalySeverity.CRITICAL) StatusRed else Color(0xFFB45309)
                                            )
                                        }
                                        Text(text = item.explanation, fontSize = 10.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Aksi: ${item.recommendedAction}",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (item.severity == AnomalySeverity.CRITICAL) StatusRed else Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Alarm Banner (If any check fails)
        if (openAlarms.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_banner_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusRedLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alarm",
                                tint = StatusRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alarm Kontrol Sistem (${openAlarms.size} Perhatian)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        openAlarms.take(3).forEach { alarm ->
                            Text(
                                text = "• ${alarm.title}: ${alarm.countOrValue} — ${alarm.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusRed,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { viewModel.navigateTo(Screen.AuditKontrol) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Buka Audit & Kontrol Lengkap", fontWeight = FontWeight.Bold, color = StatusRed)
                        }
                    }
                }
            }
        }

        // Project Quick Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Sisa Budget (RAB)",
                    value = metrics.totalAvailableBudget.toRupiah(),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = StatusGreen,
                    caption = "Available to spend",
                    modifier = Modifier.weight(1f),
                    testTag = "metric_sisa_budget"
                )
                MetricCard(
                    title = "Stok Rumah Unit",
                    value = "${metrics.unitSold}/${metrics.unitTotal} Terjual",
                    icon = Icons.Default.Home,
                    accentColor = NavyPrimary,
                    caption = "${metrics.unitAvailable} Unit Ready/Avail",
                    modifier = Modifier.weight(1f),
                    testTag = "metric_stok_rumah"
                )
            }
        }

        // Quick Modules Shortcuts
        item {
            SectionHeader(title = "Akses Cepat Modul")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(Screen.Transaksi) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_transaksi")
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kas/Bank", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(Screen.Anggaran) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_anggaran")
                ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RAB", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(Screen.Payroll) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_payroll")
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payroll", maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.DokumenKwitansi) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_kwitansi")
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kwitansi", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Jurnal) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_jurnal")
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jurnal", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.AiAdvisor) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_ai")
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Advisor", maxLines = 1)
                }
            }
        }

        // Recent Real-Time Ledger Transactions
        item {
            SectionHeader(
                title = "Transaksi Terkunci (Buku Kas & Bank)",
                subtitle = "Hanya status POSTED yang mempengaruhi saldo riil",
                actionButton = {
                    TextButton(onClick = { viewModel.navigateTo(Screen.Transaksi) }) {
                        Text("Lihat Semua")
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            )
        }

        items(postedTx.take(5)) { tx ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_item_${tx.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tx.id,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = tx.statusSistem)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tx.keterangan,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${tx.tanggal} • ${tx.rekeningBank} • ${tx.pihakNama}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = (if (tx.tipe == "MASUK") "+ " else "- ") + tx.nominal.toRupiah(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (tx.tipe == "MASUK") StatusGreen else StatusRed
                    )
                }
            }
        }
    }
}
