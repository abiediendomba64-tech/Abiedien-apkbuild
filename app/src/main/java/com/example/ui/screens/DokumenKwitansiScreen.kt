package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DokumenRegistryRecord
import com.example.domain.TerbilangHelper
import com.example.ui.components.CapStempelResmi
import com.example.ui.components.KopSuratResmi
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun DokumenKwitansiScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dokumenList by viewModel.allDokumen.collectAsStateWithLifecycle()
    val ptList by viewModel.allPT.collectAsStateWithLifecycle()

    var showAddDocDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Generator Kwitansi Resmi, 1: Dokumen Registry

    // Generator Kwitansi State
    var kwNo by remember { mutableStateOf("KW-2026/10/005") }
    var kwTerimaDari by remember { mutableStateOf("Bpk. Hendra Gunawan") }
    var kwNominalText by remember { mutableStateOf("25000000") }
    var kwUntukPembayaran by remember { mutableStateOf("Uang Muka (DP) Unit A-01 Perumahan Gunung Padang Ciamis") }
    var kwKotaTanggal by remember { mutableStateOf("Ciamis, 03 Oktober 2026") }
    var kwPenerima by remember { mutableStateOf("Siti Rahma (Kasir)") }
    var tampilkanKop by remember { mutableStateOf(true) }
    var tampilkanCap by remember { mutableStateOf(true) }

    val kwNominal = kwNominalText.toLongOrNull() ?: 0L
    val terbilang = TerbilangHelper.konversi(kwNominal)

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
                text = { Text("Kwitansi Resmi") },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Dokumen Registry (${dokumenList.size})") },
                icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Official Indonesian Kwitansi Preview & Print View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Generator Kwitansi Developer Properti (SAK EP)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                    Text(
                        text = "Dilengkapi format legal kop surat, terbilang otomatis Bahasa Indonesia, dan tanda tangan kasir/manajemen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Toggles Kop & Cap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tampilkanKop,
                            onClick = { tampilkanKop = !tampilkanKop },
                            label = { Text("Kop Surat PT GAN", fontSize = 11.sp) },
                            leadingIcon = {
                                if (tampilkanKop) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        FilterChip(
                            selected = tampilkanCap,
                            onClick = { tampilkanCap = !tampilkanCap },
                            label = { Text("Cap Stempel Resmi", fontSize = 11.sp) },
                            leadingIcon = {
                                if (tampilkanCap) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }

                // Official Kwitansi Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("official_kwitansi_preview_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD4AF37)))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (tampilkanKop) {
                                KopSuratResmi(
                                    subJudul = "DEVELOPER PERUMAHAN & REAL ESTATE",
                                    nomorDokumen = kwNo
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "KWITANSI RESMI",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "No: $kwNo",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentGold
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = NavyPrimary)
                            }

                            // Fields
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Sudah Terima Dari",
                                    modifier = Modifier.width(130.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(text = ": $kwTerimaDari", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Uang Sejumlah",
                                    modifier = Modifier.width(130.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = ": # $terbilang #",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium),
                                    color = NavyDark
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Untuk Pembayaran",
                                    modifier = Modifier.width(130.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(text = ": $kwUntukPembayaran", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Nominal Box & Signature with Official Stamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NavyDark,
                                    modifier = Modifier.border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = "Jumlah: ${kwNominal.toRupiah()}",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AccentGoldLight
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = kwKotaTanggal, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(38.dp))
                                        Text(
                                            text = "($kwPenerima)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(text = "Finance / Kasir Proyek", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    if (tampilkanCap) {
                                        CapStempelResmi(
                                            modifier = Modifier.offset(x = 6.dp, y = 2.dp),
                                            rotationDegrees = -10f,
                                            labelStatus = "ASLI & SAH",
                                            namaApprover = kwPenerima
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit Kwitansi Input Form
                item {
                    SectionHeader(title = "Kustomisasi Data Kwitansi")
                    OutlinedTextField(
                        value = kwNo,
                        onValueChange = { kwNo = it },
                        label = { Text("Nomor Kwitansi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = kwTerimaDari,
                        onValueChange = { kwTerimaDari = it },
                        label = { Text("Diterima Dari (Nama Pembayar)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = kwNominalText,
                        onValueChange = { kwNominalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Uang (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = kwUntukPembayaran,
                        onValueChange = { kwUntukPembayaran = it },
                        label = { Text("Untuk Keperluan Pembayaran") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Button(
                        onClick = {
                            // Register kwitansi to Dokumen Registry automatically
                            val doc = DokumenRegistryRecord(
                                id = "DOC-${System.currentTimeMillis() % 10000}",
                                noDokumen = kwNo,
                                jenisDokumen = "KWITANSI",
                                peruntukan = kwUntukPembayaran,
                                pihakTerkait = kwTerimaDari,
                                status = "VALID",
                                tanggal = "2026-10-03",
                                verifiedBy = kwPenerima,
                                notes = "Kwitansi Resmi Dicetak Senilai ${kwNominal.toRupiah()}"
                            )
                            viewModel.addDocumentRegistry(doc)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_simpan_arsip_kwitansi"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan & Arsipkan ke Dokumen Registry")
                    }
                }
            }
        } else {
            // Dokumen Registry Screen
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arsip Dokumen Proyek",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { showAddDocDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Daftar Dokumen")
                        }
                    }
                }

                items(dokumenList) { doc ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (doc.jenisDokumen) {
                                            "KWITANSI" -> Icons.Default.Receipt
                                            "SPK_KONTRAKTOR" -> Icons.AutoMirrored.Filled.Assignment
                                            "INVOICE_NOTA" -> Icons.Default.Description
                                            else -> Icons.Default.Folder
                                        },
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = doc.noDokumen,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NavyPrimary
                                    )
                                }
                                StatusBadge(status = doc.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = doc.peruntukan, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = "Pihak: ${doc.pihakTerkait} • Tanggal: ${doc.tanggal} • Verifikator: ${doc.verifiedBy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (doc.notes.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${doc.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Daftarkan Dokumen Baru
    if (showAddDocDialog) {
        var noDocInput by remember { mutableStateOf("") }
        var jenisDocInput by remember { mutableStateOf("INVOICE_NOTA") }
        var peruntukanInput by remember { mutableStateOf("") }
        var pihakInput by remember { mutableStateOf("") }
        var statusDocInput by remember { mutableStateOf("VALID") }

        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("Daftarkan Dokumen ke Registry", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noDocInput,
                        onValueChange = { noDocInput = it },
                        label = { Text("Nomor Dokumen (Fisik)") },
                        placeholder = { Text("Contoh: INV-9912 / SPK-02") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = peruntukanInput,
                        onValueChange = { peruntukanInput = it },
                        label = { Text("Peruntukan / Keterangan Dokumen") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pihakInput,
                        onValueChange = { pihakInput = it },
                        label = { Text("Pihak Terkait (Vendor / Pembeli)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Jenis Dokumen:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("INVOICE_NOTA", "SPK_KONTRAKTOR", "BAST", "KWITANSI").forEach { jn ->
                            FilterChip(
                                selected = jenisDocInput == jn,
                                onClick = { jenisDocInput = jn },
                                label = { Text(jn, fontSize = 10.sp) }
                            )
                        }
                    }
                    Text("Status Verifikasi:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("VALID", "PENDING", "REQUIRED").forEach { st ->
                            FilterChip(
                                selected = statusDocInput == st,
                                onClick = { statusDocInput = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val record = DokumenRegistryRecord(
                            id = "DOC-${(System.currentTimeMillis() % 10000)}",
                            noDokumen = noDocInput.ifBlank { "DOC-${System.currentTimeMillis() % 1000}" },
                            jenisDokumen = jenisDocInput,
                            peruntukan = peruntukanInput.ifBlank { "Dokumen operasional" },
                            pihakTerkait = pihakInput.ifBlank { "Vendor" },
                            status = statusDocInput,
                            tanggal = "2026-10-03",
                            verifiedBy = "Operator",
                            notes = "Diinput via Android Registry"
                        )
                        viewModel.addDocumentRegistry(record)
                        showAddDocDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan ke Registry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
