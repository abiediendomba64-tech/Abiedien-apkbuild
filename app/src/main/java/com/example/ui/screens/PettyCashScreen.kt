package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PettyCashAdvanceRecord
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun PettyCashScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val pettyCashList by viewModel.allPettyCashAdvances.collectAsStateWithLifecycle()
    val userList by viewModel.activeUsers.collectAsStateWithLifecycle()

    var showRequestDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf("Ahmad Fauzi, S.E.") }
    var requestAmountText by remember { mutableStateOf("5000000") }
    var keterangan by remember { mutableStateOf("Kas operasional lapangan & darurat") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANAJEMEN KAS KECIL (PETTY CASH)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Akun 1320 (Uang Muka Operasional).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 ATURAN KUNCI: Pengguna yang masih memiliki advance berstatus OPEN atau OVERDUE secara otomatis TERKUNCI (LOCKED) dan tidak dapat mencairkan advance baru sampai pertanggungjawaban nota diverifikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGoldLight
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Daftar Advance Kas Kecil (${pettyCashList.size})",
                subtitle = "Plafon, Realisasi & Sisa Uang Muka",
                actionButton = {
                    Button(
                        onClick = { showRequestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajukan Kas Kecil")
                    }
                }
            )
        }

        items(pettyCashList) { adv ->
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
                            text = "${adv.advanceId} • ${adv.penerimaNama}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        StatusBadge(status = adv.statusSettlement)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Jumlah Dicairkan: ${adv.jumlahAdvance.toRupiah()} (Plafon: ${adv.plafonUser.toRupiah()})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Terpakai Terverifikasi: ${adv.terpakaiTerverifikasi.toRupiah()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                    Text(
                        text = "Sisa Pertanggungjawaban (Outstanding): ${adv.outstanding.toRupiah()}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (adv.outstanding > 0) StatusRed else StatusGreen
                    )
                    Text(
                        text = "Jatuh Tempo: ${adv.jatuhTempo} • Ref: ${adv.voucherTxId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Uraian: ${adv.keterangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal: Ajukan Petty Cash Baru
    if (showRequestDialog) {
        val userMatch = userList.find { it.nama == selectedUser }
        val plafon = userMatch?.plafonPettyCash ?: 10_000_000L

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Pengajuan Advance Kas Kecil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Penerima Staf/Pelaksana:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        userList.take(3).forEach { u ->
                            FilterChip(
                                selected = selectedUser == u.nama,
                                onClick = { selectedUser = u.nama },
                                label = { Text(u.nama) }
                            )
                        }
                    }
                    Text(
                        text = "Plafon Maksimal: ${plafon.toRupiah()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = requestAmountText,
                        onValueChange = { requestAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Diminta (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = { keterangan = it },
                        label = { Text("Kebutuhan Operasional") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nom = requestAmountText.toLongOrNull() ?: 0L
                        val adv = PettyCashAdvanceRecord(
                            advanceId = "ADV-2026-${System.currentTimeMillis() % 1000}",
                            tanggal = "2026-10-03",
                            penerimaNama = selectedUser,
                            projectId = "PRJ-001",
                            jumlahAdvance = nom,
                            plafonUser = plafon,
                            terpakaiTerverifikasi = 0L,
                            dikembalikan = 0L,
                            jatuhTempo = "2026-10-17",
                            voucherTxId = "TX-${System.currentTimeMillis() % 10000}",
                            statusSettlement = "OPEN",
                            keterangan = keterangan
                        )
                        viewModel.requestPettyCash(adv)
                        showRequestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Proses Pengajuan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
