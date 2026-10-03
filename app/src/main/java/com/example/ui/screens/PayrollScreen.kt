package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.data.AttendanceRecord
import com.example.data.PayrollBuruhRecord
import com.example.data.TransaksiKasBankRecord
import com.example.domain.TerbilangHelper
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun PayrollScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val payrollList by viewModel.allPayroll.collectAsStateWithLifecycle()
    val attendanceList by viewModel.allAttendance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Slip Gaji & Rincian, 1: Absensi Lapangan, 2: Sync Spreadsheet CSV
    var showAddAttendanceDialog by remember { mutableStateOf(false) }
    var showCsvSyncDialog by remember { mutableStateOf(false) }
    var showPayslipDialog by remember { mutableStateOf(false) }
    var selectedWorkerForPayslip by remember { mutableStateOf<PayrollBuruhRecord?>(null) }
    var csvInputText by remember { mutableStateOf("") }

    val totalSudahBayar = payrollList.filter { it.status == "Bayar" }.sumOf { it.totalGaji }
    val totalPending = payrollList.filter { it.status == "Pending" }.sumOf { it.totalGaji }

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
                text = { Text("Slip & Gaji (${payrollList.size})") },
                icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Presensi Absensi (${attendanceList.size})") },
                icon = { Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Sync Spreadsheet") },
                icon = { Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // Tab 0: Payroll List with WA Slip & Detailed Breakdown
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                text = "PAYROLL & ABSENSI TENAGA KERJA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Data terintegrasi ke Akun 6100 (Beban Overhead) & Pos Anggaran OVH-001.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Total Terbayar:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(totalSudahBayar.toRupiah(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreenLight)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Masih Pending:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    Text(totalPending.toRupiah(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AccentGoldLight)
                                }
                            }
                        }
                    }
                }

                // Batch Posting Banner
                if (totalPending > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentGoldLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ada ${payrollList.count { it.status == "Pending" }} Upah Pending (${totalPending.toRupiah()})", fontWeight = FontWeight.Bold, color = AccentGold)
                                    Text("Posting batch upah lapangan langsung ke Buku Kas/Bank.", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = {
                                        val txId = "TX-20261003-${(System.currentTimeMillis() % 100000).toString().padStart(6, '0')}"
                                        val tx = TransaksiKasBankRecord(
                                            id = txId,
                                            tanggal = "2026-10-03",
                                            tipe = "KELUAR",
                                            rekeningBank = "Kas Kecil",
                                            projectId = "PRJ-001",
                                            costCode = "OVH-001",
                                            akunEfektif = "6100",
                                            pihakId = "PHK-001",
                                            pihakNama = "Buruh Lapangan & Staf",
                                            nominal = totalPending,
                                            keterangan = "Pembayaran Batch Gaji Buruh Lapangan Pending",
                                            docRef = "PPB-BATCH-PAYROLL",
                                            dibuatOleh = "Operator",
                                            disetujuiOleh1 = "Ahmad Fauzi, S.E.",
                                            statusInput = "SUBMITTED"
                                        )
                                        viewModel.submitTransaksi(tx)
                                        payrollList.filter { it.status == "Pending" }.forEach {
                                            viewModel.updatePayrollStatus(it.id, "Bayar")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                    modifier = Modifier.testTag("btn_posting_batch_payroll")
                                ) {
                                    Text("Bayar Semua")
                                }
                            }
                        }
                    }
                }

                items(payrollList) { worker ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_card_${worker.nama}"),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = worker.nama,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = worker.pekerjaan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Skema: ${worker.jenisGaji} • Tarif: ${worker.gajiPerHari.toRupiah()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = worker.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rincian Detail Anggaran & Gaji Bersih
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Hari Kerja: ${if (worker.hariKerja % 1.0 == 0.0) worker.hariKerja.toInt().toString() else worker.hariKerja.toString()} Hari", style = MaterialTheme.typography.bodySmall)
                                        Text("Upah Kotor: ${worker.totalGaji.toRupiah()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    if (worker.potonganKasbon > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Potongan Kasbon:", style = MaterialTheme.typography.bodySmall)
                                            Text("- ${worker.potonganKasbon.toRupiah()}", style = MaterialTheme.typography.bodySmall, color = StatusRed)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("PPh 21 / Pajak:", style = MaterialTheme.typography.bodySmall)
                                        Text("Rp 0 (Bebas PPh)", style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Gaji Bersih Diterima:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        Text(worker.gajiBersih.toRupiah(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = NavyPrimary)
                                    }
                                }
                            }

                            if (worker.catatan.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catatan: ${worker.catatan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (worker.noWa.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val slipWa = "*SLIP GAJI RESMI PROYEK PROPERTI*\n" +
                                                    "PT GEMA ABADI NUGRAHA\n" +
                                                    "Proyek: Perumahan Gunung Padang, Ciamis\n" +
                                                    "-----------------------------------\n" +
                                                    "Nama: ${worker.nama}\n" +
                                                    "Posisi: ${worker.pekerjaan} (${worker.jenisGaji})\n" +
                                                    "Tanggal: ${worker.tanggal}\n" +
                                                    "-----------------------------------\n" +
                                                    "Hari Kerja: ${worker.hariKerja} Hari x ${worker.gajiPerHari.toRupiah()}\n" +
                                                    "Upah Kotor: ${worker.totalGaji.toRupiah()}\n" +
                                                    "Potongan Kasbon: ${worker.potonganKasbon.toRupiah()}\n" +
                                                    "PPh 21: Rp 0\n" +
                                                    "-----------------------------------\n" +
                                                    "*TOTAL GAJI BERSIH: ${worker.gajiBersih.toRupiah()}*\n" +
                                                    "Status: ${worker.status}\n" +
                                                    "Alokasi Pos: OVH-001 (Upah Lapangan & Operasional)\n" +
                                                    (if (worker.catatan.isNotBlank()) "Catatan: ${worker.catatan}\n" else "") +
                                                    "-----------------------------------\n" +
                                                    "Terima kasih atas dedikasi dan kerja keras Anda."

                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${worker.noWa}&text=${Uri.encode(slipWa)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kirim WA Dok")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                FilledTonalButton(
                                    onClick = {
                                        val nextStatus = if (worker.status == "Bayar") "Pending" else "Bayar"
                                        viewModel.updatePayrollStatus(worker.id, nextStatus)
                                    },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (worker.status == "Bayar") "Set Pending" else "Tandai Bayar")
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Tab 1: Attendance Tracking
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
                                text = "Presensi & Absensi Lapangan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Data presensi harian mengalir otomatis ke perhitungan hari kerja.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showAddAttendanceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catat Hadir")
                        }
                    }
                }

                items(attendanceList) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = att.nama, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = att.status)
                                }
                                Text(
                                    text = "Tanggal: ${att.tanggal} • Jam: ${att.jamMasuk} - ${att.jamKeluar}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (att.jamLembur > 0) {
                                    Text(
                                        text = "Lembur: ${att.jamLembur} Jam",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentGold
                                    )
                                }
                                if (att.catatan.isNotBlank()) {
                                    Text(text = "Uraian: ${att.catatan}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 2: Spreadsheet Sync & GitHub CONTROL Repository Integration
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
                                text = "SINKRONISASI SPREADSHEET (CSV)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Repositori Kontrol: https://github.com/barubayu001-ux/CONTROL",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentGoldLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sinkronkan basis data lokal Room dengan skema berkas Excel / CSV secara instan dua arah tanpa kehilangan data historis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val csv = viewModel.exportPayrollCsv()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Payroll CSV", csv)
                                clipboard.setPrimaryClip(clip)
                                viewModel.userFeedbackMessage.value = "Data CSV berhasil disalin ke Clipboard!"
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin CSV")
                        }

                        Button(
                            onClick = { showCsvSyncDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import CSV")
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Preview Data CSV Saat Ini",
                        subtitle = "Format Sesuai Berkas Excel Developer"
                    )
                    val currentCsv = viewModel.exportPayrollCsv()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = currentCsv,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Modal: Tambah Presensi Hadir
    if (showAddAttendanceDialog) {
        var namaInput by remember { mutableStateOf("Eeng") }
        var statusHadirInput by remember { mutableStateOf("Hadir") }
        var jamLemburInput by remember { mutableStateOf("0") }
        var catatanInput by remember { mutableStateOf("Pekerjaan struktur") }

        AlertDialog(
            onDismissRequest = { showAddAttendanceDialog = false },
            title = { Text("Catat Presensi Tenaga Kerja", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Pekerja:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Eeng", "Rohman", "Abah", "eteh", "Bayu").forEach { p ->
                            FilterChip(
                                selected = namaInput == p,
                                onClick = { namaInput = p },
                                label = { Text(p) }
                            )
                        }
                    }
                    Text("Status Kehadiran:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Hadir", "Lembur", "Izin", "Sakit").forEach { st ->
                            FilterChip(
                                selected = statusHadirInput == st,
                                onClick = { statusHadirInput = st },
                                label = { Text(st) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = jamLemburInput,
                        onValueChange = { jamLemburInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jam Lembur (Jam)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catatanInput,
                        onValueChange = { catatanInput = it },
                        label = { Text("Uraian Pekerjaan / Lokasi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val att = AttendanceRecord(
                            workerId = 1L,
                            nama = namaInput,
                            tanggal = "2026-10-03",
                            status = statusHadirInput,
                            jamMasuk = "08:00",
                            jamKeluar = "17:00",
                            jamLembur = jamLemburInput.toDoubleOrNull() ?: 0.0,
                            catatan = catatanInput
                        )
                        viewModel.recordAttendance(att)
                        showAddAttendanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan Presensi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAttendanceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Import CSV Dialog
    if (showCsvSyncDialog) {
        AlertDialog(
            onDismissRequest = { showCsvSyncDialog = false },
            title = { Text("Sinkronisasi / Import Spreadsheet CSV", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tempel teks CSV spreadsheet untuk mengimpor atau memperbarui data secara otomatis:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = { csvInputText = it },
                        label = { Text("Teks CSV") },
                        placeholder = { Text("id,kode,nama,pekerjaan,jenis_gaji,gaji_per_hari,hari_kerja,total_gaji,tanggal,status,no_wa,catatan...") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importPayrollFromCsv(csvInputText)
                        showCsvSyncDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Sinkronkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvSyncDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
