package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AnggaranProyek
import com.example.data.RevisiAnggaranRecord
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun AnggaranScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val anggaranList by viewModel.allAnggaran.collectAsStateWithLifecycle()
    val revisiList by viewModel.allRevisi.collectAsStateWithLifecycle()
    val userList by viewModel.activeUsers.collectAsStateWithLifecycle()

    var showRevisiDialog by remember { mutableStateOf(false) }
    var selectedAnggaranForRevisi by remember { mutableStateOf<AnggaranProyek?>(null) }
    var newAmountText by remember { mutableStateOf("") }
    var revisiReason by remember { mutableStateOf("") }
    var revisiDocRef by remember { mutableStateOf("REV-DOC-01") }
    var selectedApprover by remember { mutableStateOf("H. Bambang Nugraha") }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KONTROL BUDGET & RAB PROYEK",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Standar SAK EP: Actual hanya dihitung dari transaksi berstatus POSTED. Budget berlaku tidak boleh ditimpa manual tanpa pengajuan Revisi Anggaran resmi berjenjang.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Summary Totals
            item {
                val totalBudget = anggaranList.sumOf { it.budgetBerlaku }
                val totalActual = anggaranList.sumOf { it.actualPosted }
                val totalCommitment = anggaranList.sumOf { it.commitment }
                val totalAvailable = totalBudget - totalActual - totalCommitment

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Rekapitulasi Konsolidasi Proyek",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Budget Berlaku:", style = MaterialTheme.typography.bodySmall)
                            Text(totalBudget.toRupiah(), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Realisasi Actual (POSTED):", style = MaterialTheme.typography.bodySmall)
                            Text(totalActual.toRupiah(), fontWeight = FontWeight.Bold, color = AccentGold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Commitment PO Terbuka:", style = MaterialTheme.typography.bodySmall)
                            Text(totalCommitment.toRupiah(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sisa Budget Tersedia (Available):", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                totalAvailable.toRupiah(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (totalAvailable >= 0) StatusGreen else StatusRed
                            )
                        }
                    }
                }
            }

            // List Pos Anggaran
            item {
                SectionHeader(
                    title = "Rincian Pos Biaya Proyek (Cost Code)",
                    subtitle = "Budget vs Realisasi Fisik"
                )
            }

            items(anggaranList) { ang ->
                val progress = if (ang.budgetBerlaku > 0) {
                    (ang.actualPosted.toFloat() / ang.budgetBerlaku.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_anggaran_${ang.costCode}"),
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
                                    text = "${ang.costCode} • ${ang.costCodeNama}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Proyek: ${ang.projectId} • ${ang.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = ang.statusBudget)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (ang.statusBudget) {
                                "OVER BUDGET" -> StatusRed
                                "WARNING" -> StatusYellow
                                else -> StatusGreen
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget: ${ang.budgetBerlaku.toRupiah()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Actual: ${ang.actualPosted.toRupiah()} (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sisa: ${ang.availableBudget.toRupiah()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (ang.availableBudget >= 0) StatusGreen else StatusRed
                            )

                            // Action: Ajukan Revisi Anggaran
                            TextButton(
                                onClick = {
                                    selectedAnggaranForRevisi = ang
                                    newAmountText = ang.budgetBerlaku.toString()
                                    showRevisiDialog = true
                                },
                                modifier = Modifier.testTag("btn_revisi_${ang.costCode}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajukan Revisi")
                            }
                        }
                    }
                }
            }

            // Histori Revisi Anggaran
            item {
                SectionHeader(
                    title = "Histori Revisi Anggaran (${revisiList.size})",
                    subtitle = "Jejak Perubahan Anggaran Resmi"
                )
            }

            if (revisiList.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada revisi anggaran yang diajukan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(revisiList) { rev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${rev.revisionId} • ${rev.costCode}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                StatusBadge(status = rev.status)
                            }
                            Text(
                                text = "Lama: ${rev.oldAmount.toRupiah()} ➔ Baru: ${rev.newAmount.toRupiah()} (Selisih: ${rev.difference.toRupiah()})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Alasan: ${rev.reason} • Disetujui oleh: ${rev.approvedBy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Ajukan Revisi Anggaran
    if (showRevisiDialog && selectedAnggaranForRevisi != null) {
        val ang = selectedAnggaranForRevisi!!
        val newAmt = newAmountText.toLongOrNull() ?: ang.budgetBerlaku
        val selisih = newAmt - ang.budgetBerlaku

        AlertDialog(
            onDismissRequest = { showRevisiDialog = false },
            title = {
                Text("Revisi Anggaran: ${ang.costCode}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sesuai SAK EP, perubahan pagu anggaran harus memiliki audit trail tertulis dan hanya dapat disetujui Direktur (Pangkat 3) atau Owner (Pangkat 4).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Budget Saat Ini: ${ang.budgetBerlaku.toRupiah()}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Budget Baru (Rp)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_budget_amount")
                    )
                    Text(
                        text = "Penyesuaian Selisih: ${if (selisih >= 0) "+" else ""}${selisih.toRupiah()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selisih >= 0) StatusGreen else StatusRed
                    )
                    OutlinedTextField(
                        value = revisiReason,
                        onValueChange = { revisiReason = it },
                        label = { Text("Alasan Perubahan (Wajib)") },
                        placeholder = { Text("Contoh: Kenaikan harga material semen & besi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Pejabat Approver (Pangkat >= 3):", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        userList.filter { it.pangkat >= 3 }.forEach { user ->
                            FilterChip(
                                selected = selectedApprover == user.nama,
                                onClick = { selectedApprover = user.nama },
                                label = { Text(user.nama) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val record = RevisiAnggaranRecord(
                            revisionId = "REV-2026-${(System.currentTimeMillis() % 1000).toString().padStart(3, '0')}",
                            tanggal = "2026-10-03",
                            budgetId = ang.id,
                            costCode = ang.costCode,
                            projectId = ang.projectId,
                            oldAmount = ang.budgetBerlaku,
                            newAmount = newAmt,
                            difference = selisih,
                            reason = revisiReason.ifBlank { "Penyesuaian spesifikasi fisik proyek" },
                            requestedBy = "Ir. Hendra",
                            approvedBy = selectedApprover,
                            approvalDate = "2026-10-03",
                            docRef = revisiDocRef,
                            status = "APPROVED"
                        )
                        viewModel.submitRevisiAnggaran(record)
                        showRevisiDialog = false
                        selectedAnggaranForRevisi = null
                    },
                    modifier = Modifier.testTag("btn_submit_revisi_anggaran"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Setujui & Simpan Revisi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisiDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
