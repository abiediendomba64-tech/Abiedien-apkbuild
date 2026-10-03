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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TaxFilingRecord
import com.example.domain.TaxEngine
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun PajakScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val taxFilings by viewModel.allTaxFilings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Kalkulator Pajak, 1: Laporan SPT & Setor Pajak
    var hargaRumahText by remember { mutableStateOf("") }
    var selectedPersenDtp by remember { mutableStateOf(100.0) }
    var showAddTaxDialog by remember { mutableStateOf(false) }

    val hargaRumah = hargaRumahText.toLongOrNull() ?: 0L
    val pphFinal = TaxEngine.hitungPphFinalPengalihan(hargaRumah)
    val bphtb = TaxEngine.hitungBphtb(hargaRumah)
    val ppnDtpResult = TaxEngine.hitungPpnDtp(hargaRumah, selectedPersenDtp)

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
                text = { Text("Kalkulator Pajak") },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Laporan SPT & Riwayat (${taxFilings.size})") },
                icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "KEPATUHAN PAJAK DEVELOPER (SAK EP)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kalkulasi otomatis PPh Final Pengalihan Hak (2,5%), BPHTB Notaris, PPN 11%, dan Insentif PPN DTP Pemerintah.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Interactive Tax Calculator Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kalkulator_pajak_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Kalkulator Pajak Unit Rumah",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = hargaRumahText,
                                onValueChange = { hargaRumahText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Harga Rumah Transaksi (Rp)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fasilitas Insentif PPN DTP:", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedPersenDtp == 100.0,
                                    onClick = { selectedPersenDtp = 100.0 },
                                    label = { Text("DTP 100%") }
                                )
                                FilterChip(
                                    selected = selectedPersenDtp == 50.0,
                                    onClick = { selectedPersenDtp = 50.0 },
                                    label = { Text("DTP 50%") }
                                )
                                FilterChip(
                                    selected = selectedPersenDtp == 0.0,
                                    onClick = { selectedPersenDtp = 0.0 },
                                    label = { Text("Non DTP (0%)") }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Calculation outputs
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PPh Final Pasal 4(2) [2.5%]:", style = MaterialTheme.typography.bodySmall)
                                Text(pphFinal.toRupiah(), fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                            Text(
                                text = "(Beban Penjual/Developer disetor ke Kas Negara sebelum AJB)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Perkiraan BPHTB Pembeli [5%]:", style = MaterialTheme.typography.bodySmall)
                                Text(bphtb.toRupiah(), fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "(Dasar pengenaan: Harga - NPOPTKP Rp 80.000.000)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PPN Total [11%]:", style = MaterialTheme.typography.bodySmall)
                                Text(ppnDtpResult.ppnTotal.toRupiah(), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PPN Ditanggung Pemerintah (DTP):", style = MaterialTheme.typography.bodySmall)
                                Text("- ${ppnDtpResult.ppnDitanggungPemerintah.toRupiah()}", fontWeight = FontWeight.Bold, color = StatusGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PPN Dibayar Konsumen:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(ppnDtpResult.ppnDibayarCustomer.toRupiah(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                            }
                            Text(
                                text = "Status: ${ppnDtpResult.statusInsentif}",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // SAK EP Guidance
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Ketentuan Pelaporan Akhir Periode Fiskal:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• PPh Final 2,5% disetorkan paling lambat tanggal 10 bulan berikutnya dan dilaporkan pada SPT Masa PPh Final.\n" +
                                        "• PPN Masa dilaporkan melalui e-Faktur dengan lampiran faktur pajak berkode 010 (Standar) atau 070 (PPN DTP).\n" +
                                        "• PBB Proyek dilunasi sebelum jatuh tempo SPPT dan dicatat pada akun WIP Aset Lahan (1420).",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            // Tab 1: Laporan SPT Masa & Riwayat Pembayaran Pajak
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Arsip & Riwayat Setor SPT Pajak",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Bukti Penerimaan Negara (NTPN) & Pelaporan DJP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showAddTaxDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rekam SPT")
                        }
                    }
                }

                items(taxFilings) { tax ->
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
                                        text = "${tax.id} • ${tax.jenisPajak}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "Masa: ${tax.masaPajak} • Entitas: ${tax.entityId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = tax.statusSetor)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("DPP (Dasar Pengenaan):", style = MaterialTheme.typography.bodySmall)
                                Text(tax.dasarPengenaanPajak.toRupiah(), fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tarif Pajak:", style = MaterialTheme.typography.bodySmall)
                                Text("${tax.tarifPersen}%", fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pajak Disetor:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(tax.nominalPajak.toRupiah(), fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kode Billing: ${tax.kodeBilling} • NTPN: ${tax.ntpn}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tgl Bayar: ${tax.tanggalBayar} • Tgl Lapor: ${tax.tanggalLapor} • Status DJP: ${tax.statusLapor}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Rekam Setor Pajak & SPT Baru
    if (showAddTaxDialog) {
        var jenisPajakInput by remember { mutableStateOf("PPh Final Pasal 4(2)") }
        var masaPajakInput by remember { mutableStateOf("Oktober 2026") }
        var dppInputText by remember { mutableStateOf("285000000") }
        var tarifInputText by remember { mutableStateOf("2.5") }
        var kodeBillingInput by remember { mutableStateOf("019283749911") }
        var ntpnInput by remember { mutableStateOf("99AABBCCDDEE8877") }

        val dpp = dppInputText.toLongOrNull() ?: 0L
        val tarif = tarifInputText.toDoubleOrNull() ?: 2.5
        val nominal = (dpp * (tarif / 100.0)).toLong()

        AlertDialog(
            onDismissRequest = { showAddTaxDialog = false },
            title = { Text("Rekam Setoran Pajak & SPT Masa", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Jenis Pajak:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("PPh Final 4(2)", "PPN Properti", "PBB Proyek", "PPh 21 Buruh").forEach { jp ->
                            FilterChip(
                                selected = jenisPajakInput == jp,
                                onClick = {
                                    jenisPajakInput = jp
                                    tarifInputText = when (jp) {
                                        "PPh Final 4(2)" -> "2.5"
                                        "PPN Properti" -> "11.0"
                                        "PBB Proyek" -> "0.5"
                                        else -> "5.0"
                                    }
                                },
                                label = { Text(jp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = masaPajakInput,
                        onValueChange = { masaPajakInput = it },
                        label = { Text("Masa Pajak (Bulan / Tahun)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dppInputText,
                        onValueChange = { dppInputText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Dasar Pengenaan Pajak / DPP (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Nominal Pajak Terutang: ${nominal.toRupiah()} (${tarif}%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                    OutlinedTextField(
                        value = kodeBillingInput,
                        onValueChange = { kodeBillingInput = it },
                        label = { Text("Kode Billing Setor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ntpnInput,
                        onValueChange = { ntpnInput = it },
                        label = { Text("Nomor Transaksi Penerimaan Negara (NTPN)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val record = TaxFilingRecord(
                            id = "SPT-${System.currentTimeMillis() % 10000}",
                            jenisPajak = jenisPajakInput,
                            masaPajak = masaPajakInput,
                            tahunPajak = 2026,
                            entityId = "PT-001",
                            dasarPengenaanPajak = dpp,
                            tarifPersen = tarif,
                            nominalPajak = nominal,
                            kodeBilling = kodeBillingInput,
                            ntpn = ntpnInput,
                            statusSetor = "LUNAS",
                            statusLapor = "SUDAH_LAPOR",
                            tanggalBayar = "2026-10-03",
                            tanggalLapor = "2026-10-03",
                            buktiDocRef = "BPN-$ntpnInput"
                        )
                        viewModel.submitTaxFiling(record)
                        showAddTaxDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan Setoran & SPT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaxDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
