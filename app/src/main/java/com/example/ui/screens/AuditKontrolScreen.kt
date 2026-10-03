package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun AuditKontrolScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val auditChecks by viewModel.auditChecks.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    val totalCheck = auditChecks.size
    val totalOk = auditChecks.count { it.isOk }
    val totalCek = totalCheck - totalOk

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Audit Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audit_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (totalCek == 0) NavyDark else Color(0xFF7F1D1D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUDIT & KONTROL SISTEM SAK EP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        StatusBadge(status = if (totalCek == 0) "100% LOLOS AUDIT" else "$totalCek PERHATIAN")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Evaluasi otomatis terhadap 12 gerbang kontrol ledger, limit RAB, otorisasi berjenjang, dan keabsahan dokumen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Gerbang Kontrol: $totalCheck", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Lolos: $totalOk", color = StatusGreenLight, fontWeight = FontWeight.Bold)
                        Text("Perlu Dicek: $totalCek", color = AccentGoldLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Checks List
        item {
            SectionHeader(
                title = "Daftar Gerbang Kontrol Aktif",
                subtitle = "Pemeriksaan Real-Time Terhadap Basis Data"
            )
        }

        items(auditChecks) { check ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (check.isOk) MaterialTheme.colorScheme.surface else StatusRedLight.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${check.no}. ${check.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (check.isOk) MaterialTheme.colorScheme.onSurface else StatusRed
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kondisi: ${check.countOrValue}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (check.isOk) StatusGreen else StatusRed
                        )
                        Text(
                            text = check.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = if (check.isOk) "OK" else "CEK")
                }
            }
        }

        // Audit Logs
        item {
            SectionHeader(
                title = "Jejak Audit Aktivitas (Audit Trail)",
                subtitle = "Riwayat Pembuatan, Posting & Koreksi"
            )
        }

        items(auditLogs.take(10)) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${log.action} • ${log.recordId}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Text(
                            text = "Oleh: ${log.user}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
