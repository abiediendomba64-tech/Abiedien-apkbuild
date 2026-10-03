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
import com.example.data.TransaksiKasBankRecord
import com.example.domain.PriceResearchAnalysis
import com.example.ui.components.AiNominalAlarmCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transaksiList by viewModel.allTransaksi.collectAsStateWithLifecycle()
    val proyekList by viewModel.allProyek.collectAsStateWithLifecycle()
    val costCodeList by viewModel.allCostCode.collectAsStateWithLifecycle()
    val pihakList by viewModel.allPihak.collectAsStateWithLifecycle()
    val anggaranList by viewModel.allAnggaran.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showReversalDialog by remember { mutableStateOf(false) }
    var selectedTxForReversal by remember { mutableStateOf<TransaksiKasBankRecord?>(null) }
    var reversalReason by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("SEMUA") }
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importCsvText by remember { mutableStateOf("") }

    val filteredList = when (filterTab) {
        "POSTED" -> transaksiList.filter { it.statusSistem == "POSTED" }
        "PENDING_APPROVAL" -> transaksiList.filter { it.statusSistem == "PENDING_APPROVAL" }
        "REVERSED" -> transaksiList.filter { it.statusSistem == "REVERSED" }
        "MASUK" -> transaksiList.filter { it.tipe == "MASUK" }
        "KELUAR" -> transaksiList.filter { it.tipe == "KELUAR" }
        else -> transaksiList
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_tambah_transaksi")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Notice Banner: Ledger Terkunci & SAK EP Compliance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyLight.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ledger Terkunci: Transaksi berstatus POSTED bersifat immutable. Koreksi harus melalui mekanisme Reversal (RV).",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavyDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("SEMUA", "PENDING_APPROVAL", "POSTED", "MASUK", "KELUAR", "REVERSED").forEach { tab ->
                    FilterChip(
                        selected = filterTab == tab,
                        onClick = { filterTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Spreadsheet Sync & Migration Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daftar Transaksi (${filteredList.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                    Text(
                        text = "Sumber data: Supabase • Room = cache lokal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Impor", fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_tx_${tx.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.id,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = tx.statusSistem)
                                }
                                Text(
                                    text = (if (tx.tipe == "MASUK") "+ " else "- ") + tx.nominal.toRupiah(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (tx.tipe == "MASUK") StatusGreen else StatusRed
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = tx.keterangan,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = NavyLight) {
                                    Text(
                                        text = when (tx.kategoriEntitas) {
                                            "PT_ANAK" -> "Anak PT"
                                            "MITRA_INVESTOR" -> "Investor"
                                            "KASIR_LAPANGAN" -> "Kasir Lapangan"
                                            else -> "PT Induk"
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NavyPrimary
                                    )
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = tx.tipeTransaksiKhusus,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                if (tx.persentaseKomisiBagiHasil > 0.0) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = AccentGoldLight) {
                                        Text(
                                            text = "Bagi/Komisi: ${tx.persentaseKomisiBagiHasil}%",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentGold
                                        )
                                    }
                                }
                            }

                            if (tx.rekeningTujuan.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Geser Uang -> Tujuan: ${tx.rekeningTujuan} (Piutang Intercompany Terlacak)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = NavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rekening: ${tx.rekeningBank} • ${tx.tanggal}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tx.costCode.isNotBlank()) {
                                    Text(
                                        text = "Cost Code: ${tx.costCode}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = AccentGold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pihak: ${tx.pihakNama} • Dibuat: ${tx.dibuatOleh}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (tx.disetujuiOleh1.isNotBlank()) "Approver: ${tx.disetujuiOleh1}" else "Approver: ditentukan server dari akun terautentikasi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (tx.reversalOfId.isNotBlank()) {
                                Text(
                                    text = "Reversal dari Transaksi: ${tx.reversalOfId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusRed
                                )
                            }

                            if (tx.statusSistem == "PENDING_APPROVAL") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.approveTransaction(tx.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve dengan akun saya")
                                }
                            }

                            // Reversal Action for POSTED transactions
                            if (tx.statusSistem == "POSTED" && tx.reversalOfId.isBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedTxForReversal = tx
                                            showReversalDialog = true
                                        },
                                        modifier = Modifier.height(36.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Koreksi / Reversal")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Input Form Transaksi Baru (PPB / Kas Masuk / Keluar / Geser Uang)
    if (showCreateDialog) {
        var txType by remember { mutableStateOf("KELUAR") }
        var kategoriEntitas by remember { mutableStateOf("PT_INDUK") }
        var tipeTransaksiKhusus by remember { mutableStateOf("BELANJA_RAB") }
        var persentaseKomisiText by remember { mutableStateOf("0") }
        var selectedRekeningTujuan by remember { mutableStateOf("") }
        var nominalText by remember { mutableStateOf("") }
        var keterangan by remember { mutableStateOf("") }
        var selectedRekening by remember { mutableStateOf("") }
        var selectedProjectId by remember { mutableStateOf(proyekList.firstOrNull()?.id ?: "") }
        var selectedCostCode by remember { mutableStateOf(costCodeList.firstOrNull()?.kode ?: "") }
        var selectedPihakId by remember { mutableStateOf(pihakList.firstOrNull()?.id ?: "") }
        var docRef by remember { mutableStateOf("") }

        var volumeText by remember { mutableStateOf("1") }
        var satuanText by remember { mutableStateOf("Lot") }
        var priceAuditResult by remember { mutableStateOf<PriceResearchAnalysis?>(null) }

        val nominal = nominalText.toLongOrNull() ?: 0L
        val activeAnggaran = anggaranList.find { it.projectId == selectedProjectId && it.costCode == selectedCostCode }
        val availableBudget = activeAnggaran?.availableBudget ?: 0L
        val isOverBudget = txType == "KELUAR" && tipeTransaksiKhusus == "BELANJA_RAB" && nominal > availableBudget

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("Input Rincian Transaksi Keuangan Properti", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Arah Aliran Dana:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = txType == "KELUAR",
                                onClick = {
                                    txType = "KELUAR"
                                    if (tipeTransaksiKhusus == "MODAL_INVESTOR") tipeTransaksiKhusus = "BELANJA_RAB"
                                },
                                label = { Text("KELUAR", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = txType == "MASUK",
                                onClick = {
                                    txType = "MASUK"
                                    if (tipeTransaksiKhusus == "BELANJA_RAB") tipeTransaksiKhusus = "MODAL_INVESTOR"
                                },
                                label = { Text("MASUK", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = txType == "GESER_UANG",
                                onClick = {
                                    txType = "GESER_UANG"
                                    tipeTransaksiKhusus = "GESER_UANG_MUTASI"
                                },
                                label = { Text("GESER UANG", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text("Kategori Entitas Pembukuan:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "PT_INDUK" to "PT Induk (GAN)",
                                "PT_ANAK" to "PT Anak (SSN)",
                                "MITRA_INVESTOR" to "Mitra Investor",
                                "KASIR_LAPANGAN" to "Kasir Lapangan"
                            ).forEach { (code, label) ->
                                FilterChip(
                                    selected = kategoriEntitas == code,
                                    onClick = { kategoriEntitas = code },
                                    label = { Text(label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Tipe Pos & Keperluan Dana:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val options = when (txType) {
                                "MASUK" -> listOf("MODAL_INVESTOR" to "Setor Modal", "PENJUALAN_UNIT" to "Hasil Penjualan", "OPERASIONAL" to "Penerimaan Lain")
                                "GESER_UANG" -> listOf("GESER_UANG_MUTASI" to "Mutasi & Piutang Intercompany")
                                else -> listOf("BELANJA_RAB" to "Belanja RAB", "BAGI_HASIL_INVESTOR" to "Bagi Hasil/Dividen", "KOMISI_MARKETING" to "Komisi Marketing", "OPERASIONAL" to "Overhead")
                            }
                            options.forEach { (code, label) ->
                                FilterChip(
                                    selected = tipeTransaksiKhusus == code,
                                    onClick = { tipeTransaksiKhusus = code },
                                    label = { Text(label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    if (tipeTransaksiKhusus == "KOMISI_MARKETING" || tipeTransaksiKhusus == "BAGI_HASIL_INVESTOR") {
                        item {
                            OutlinedTextField(
                                value = persentaseKomisiText,
                                onValueChange = { persentaseKomisiText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Persentase Komisi / Bagi Hasil (%)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Contoh: 2.5 atau 20.0") }
                            )
                        }
                    }

                    if (txType == "GESER_UANG") {
                        item {
                            Text("Rekening Tujuan (Tracking Piutang Intercompany):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("BCA Proyek (Anak)", "BCA Operasional (Induk)", "Kas Kecil Lapangan", "Rekening Investor").forEach { rekTujuan ->
                                    FilterChip(
                                        selected = selectedRekeningTujuan == rekTujuan,
                                        onClick = { selectedRekeningTujuan = rekTujuan },
                                        label = { Text(rekTujuan, fontSize = 10.sp) }
                                    )
                                }
                            }
                            Text(
                                text = "Geser uang otomatis mencatat mutasi kas & tracking piutang/hutang intercompany (Akun 1230/2430).",
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyPrimary
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = nominalText,
                            onValueChange = {
                                nominalText = it.filter { ch -> ch.isDigit() }
                                priceAuditResult = null
                            },
                            label = { Text("Nominal Transaksi (Rp)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_nominal_transaksi"),
                            singleLine = true
                        )
                        if (nominal > 0) {
                            Text(
                                text = nominal.toRupiah(),
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = volumeText,
                                onValueChange = {
                                    volumeText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    priceAuditResult = null
                                },
                                label = { Text("Volume / Qty") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = satuanText,
                                onValueChange = {
                                    satuanText = it
                                    priceAuditResult = null
                                },
                                label = { Text("Satuan (sak/m3/btg)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = keterangan,
                            onValueChange = {
                                keterangan = it
                                priceAuditResult = null
                            },
                            label = { Text("Rincian Lengkap / Keterangan Pembayaran") },
                            modifier = Modifier.fillMaxWidth().testTag("input_keterangan_transaksi")
                        )
                    }

                    // AI Price Research & Nominal Alarm Action
                    if (nominal > 0) {
                        item {
                            OutlinedButton(
                                onClick = {
                                    val vol = volumeText.toDoubleOrNull() ?: 1.0
                                    priceAuditResult = viewModel.checkInstantPriceAudit(
                                        itemName = keterangan.ifBlank { selectedCostCode },
                                        nominal = nominal,
                                        volume = vol,
                                        costCode = selectedCostCode,
                                        satuanInput = satuanText
                                    )
                                    viewModel.performAiPriceAudit(
                                        itemName = keterangan.ifBlank { selectedCostCode },
                                        nominal = nominal,
                                        volume = vol,
                                        costCode = selectedCostCode,
                                        satuanInput = satuanText
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (priceAuditResult?.isAlarmTriggered == true) StatusRed else NavyPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (priceAuditResult?.isAlarmTriggered == true) Icons.Default.Warning else Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (priceAuditResult?.isAlarmTriggered == true) StatusRed else AccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (priceAuditResult != null) "Cek Ulang Riset Pasar AI" else "Analisa AI: Cek Kewajaran Harga Pasar & Alarm",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (priceAuditResult != null) {
                        item {
                            AiNominalAlarmCard(analysis = priceAuditResult!!)
                        }
                    }

                    if (txType == "KELUAR" && tipeTransaksiKhusus == "BELANJA_RAB") {
                        item {
                            // Budget Alert Preview
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOverBudget) StatusRedLight else StatusGreenLight
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Kontrol Budget SAK EP:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isOverBudget) StatusRed else StatusGreen
                                    )
                                    Text(
                                        text = "Pos: $selectedCostCode • Sisa Tersedia: ${availableBudget.toRupiah()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOverBudget) StatusRed else StatusGreen
                                    )
                                    if (isOverBudget) {
                                        Text(
                                            text = "⚠️ OVER BUDGET: Permohonan melebihi pagu anggaran! Pengajuan akan ditolak oleh sistem validator.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StatusRed
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("Pilih Cost Code Proyek:", style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                costCodeList.take(4).forEach { cc ->
                                    FilterChip(
                                        selected = selectedCostCode == cc.kode,
                                        onClick = { selectedCostCode = cc.kode },
                                        label = { Text(cc.kode, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = docRef,
                            onValueChange = { docRef = it },
                            label = { Text("Nomor Dokumen Ref (Nota/SPK/KW)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Rekening Kas/Bank:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("BCA Operasional", "BCA Proyek", "Kas Kecil").forEach { rek ->
                                FilterChip(
                                    selected = selectedRekening == rek,
                                    onClick = { selectedRekening = rek },
                                    label = { Text(rek) }
                                )
                            }
                        }
                    }

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nominal <= 0) {
                            viewModel.userFeedbackMessage.value = "Nominal transaksi harus lebih besar dari Rp 0."
                            viewModel.isErrorMessage.value = true
                            return@Button
                        }
                        if (txType == "KELUAR" && docRef.isBlank()) {
                            viewModel.userFeedbackMessage.value = "Transaksi dana keluar wajib memiliki Nomor Dokumen Ref."
                            viewModel.isErrorMessage.value = true
                            return@Button
                        }

                        val newId = "TX-20261003-${(System.currentTimeMillis() % 100000).toString().padStart(6, '0')}"
                        val pihakMatch = pihakList.find { it.id == selectedPihakId }
                        val effectiveAccount = when {
                            txType == "GESER_UANG" -> "1230"
                            txType == "MASUK" && tipeTransaksiKhusus == "MODAL_INVESTOR" -> "3100"
                            txType == "MASUK" -> "4100"
                            tipeTransaksiKhusus == "KOMISI_MARKETING" -> "6100"
                            tipeTransaksiKhusus == "BAGI_HASIL_INVESTOR" -> "3200"
                            else -> costCodeList.find { it.kode == selectedCostCode }?.akunCoaDefault ?: "1410"
                        }

                        val actualType = if (txType == "GESER_UANG") "KELUAR" else txType
                        val actualKeterangan = if (txType == "GESER_UANG") {
                            "Geser Uang ke $selectedRekeningTujuan: ${keterangan.ifBlank { "Mutasi Kas Intercompany" }}"
                        } else {
                            keterangan.ifBlank { "Pembayaran operasional proyek" }
                        }

                        val record = TransaksiKasBankRecord(
                            id = newId,
                            tanggal = "2026-10-03",
                            tipe = actualType,
                            rekeningBank = selectedRekening,
                            projectId = selectedProjectId,
                            costCode = if (actualType == "KELUAR" && tipeTransaksiKhusus == "BELANJA_RAB") selectedCostCode else "",
                            akunEfektif = effectiveAccount,
                            pihakId = selectedPihakId,
                            pihakNama = pihakMatch?.nama ?: "",
                            nominal = nominal,
                            keterangan = actualKeterangan,
                            docRef = docRef,
                            statusInput = "SUBMITTED",
                            kategoriEntitas = kategoriEntitas,
                            tipeTransaksiKhusus = tipeTransaksiKhusus,
                            rekeningTujuan = if (txType == "GESER_UANG") selectedRekeningTujuan else "",
                            persentaseKomisiBagiHasil = persentaseKomisiText.toDoubleOrNull() ?: 0.0
                        )
                        viewModel.submitTransaksi(record)
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("btn_submit_post_transaksi"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Kirim untuk Approval")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Reversal Dialog
    if (showReversalDialog && selectedTxForReversal != null) {
        val target = selectedTxForReversal!!
        AlertDialog(
            onDismissRequest = { showReversalDialog = false },
            title = { Text("Buat Reversal Transaksi ${target.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Sesuai SAK EP, transaksi POSTED tidak boleh dihapus. Sistem akan membuat transaksi cermin (reversal) untuk membatalkan dampak kas dan mengembalikan anggaran.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("Alasan Reversal") },
                        placeholder = { Text("Contoh: Salah alokasi cost code / nota dibatalkan") },
                        modifier = Modifier.fillMaxWidth().testTag("input_alasan_reversal")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createReversal(target.id, reversalReason.ifBlank { "Koreksi posting" }, "Operator")
                        showReversalDialog = false
                        selectedTxForReversal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Eksekusi Reversal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReversalDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Ekspor Spreadsheet (CSV & XLSX)
    if (showExportDialog) {
        val csvData = remember(transaksiList) { viewModel.exportTransactionsCsv() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Ekspor Transaksi Kas & Bank", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Total ${transaksiList.size} transaksi siap diekspor ke format Spreadsheet (Excel / CSV).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Skema Ekspor:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("ID, Tanggal, Tipe, Rekening, Proyek, CostCode, Akun, Pihak, Nominal, Keterangan, Ref, Approver, Status", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Transaksi CSV", csvData)
                                clipboard.setPrimaryClip(clip)
                                viewModel.userFeedbackMessage.value = "Data CSV (${transaksiList.size} baris) berhasil disalin ke Clipboard!"
                                viewModel.isErrorMessage.value = false
                                showExportDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin CSV", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val bundle = viewModel.exportTransactionsXlsx()
                                viewModel.userFeedbackMessage.value = "Berkas XLSX '${bundle.fileName}' (${bundle.rowCount} baris) berhasil dibuat!"
                                viewModel.isErrorMessage.value = false
                                showExportDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buat XLSX", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Modal: Impor Spreadsheet (Supabase)
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Impor / Migrasi Data Transaksi (Supabase)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tempel data CSV transaksi dari sumber data nyata yang telah diverifikasi. Sistem akan memvalidasi sebelum posting:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = importCsvText,
                        onValueChange = { importCsvText = it },
                        label = { Text("Teks CSV Transaksi") },
                        placeholder = { Text("ID,Tanggal,Tipe,Rekening,Proyek,CostCode,Akun,Pihak,Nominal,Keterangan,Ref...") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importCsvText.isNotBlank()) {
                            viewModel.importTransactionsFromCsv(importCsvText)
                            showImportDialog = false
                            importCsvText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Impor ke Buku Kas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
