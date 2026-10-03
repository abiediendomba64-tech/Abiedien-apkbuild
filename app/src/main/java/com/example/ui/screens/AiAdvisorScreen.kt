package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.PriceResearchAnalysis
import com.example.ui.components.AiNominalAlarmCard
import com.example.ui.components.CapStempelResmi
import com.example.ui.components.KopSuratResmi
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun AiAdvisorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val latestAnalysis by viewModel.latestPriceAnalysis.collectAsStateWithLifecycle()
    val alarmsList by viewModel.priceAlarmsList.collectAsStateWithLifecycle()
    val isPriceAuditLoading by viewModel.isPriceAuditLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Riset Harga & Alarm, 1: Tanya AI & Scan, 2: Kop & Cap Resmi

    // Price Research Audit State
    var itemNameInput by remember { mutableStateOf("Semen PCC 50kg") }
    var volumeInput by remember { mutableStateOf("100") }
    var satuanInput by remember { mutableStateOf("Sak") }
    var nominalInput by remember { mutableStateOf("6500000") }
    var selectedCostCode by remember { mutableStateOf("BLD-001") }

    // Chat input
    var inputQuery by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Apa bedanya uang keluar sebagai WIP Bangunan vs Beban?",
        "Bagaimana aturan transfer ke Anak PT (Intercompany)?",
        "Mengapa petty cash terkunci jika ada advance belum settle?",
        "Berapa tarif PPh Final 2.5% & syarat PPN DTP properti?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Tabs (Mobile-First AMP Responsif)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Riset Harga & Alarm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = {
                    Icon(
                        imageVector = if (alarmsList.isNotEmpty()) Icons.Default.Warning else Icons.Default.PriceCheck,
                        contentDescription = null,
                        tint = if (alarmsList.isNotEmpty()) StatusRed else NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Tanya AI & Scan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Kop & Cap Resmi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // TAB 0: AI PRICE RESEARCH & NOMINAL ALARM AUDITOR
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    tint = AccentGoldLight,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AI PRICE RESEARCH & NOMINAL ALARM",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Audit otomatis kewajaran harga rancangan belanja properti berbasis SNI & PUPR 2026.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Rancangan Harga
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Form Uji Rancangan Harga Pengadaan:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )

                            OutlinedTextField(
                                value = itemNameInput,
                                onValueChange = { itemNameInput = it },
                                label = { Text("Nama Komponen / Bahan / Upah / Jasa") },
                                modifier = Modifier.fillMaxWidth().testTag("input_audit_item_name"),
                                placeholder = { Text("Contoh: Semen PCC 50kg, Besi 13, Pasir, Upah Tukang") }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = volumeInput,
                                    onValueChange = { volumeInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    label = { Text("Volume / Qty") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = satuanInput,
                                    onValueChange = { satuanInput = it },
                                    label = { Text("Satuan (Sak/Btg/m3)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = nominalInput,
                                onValueChange = { nominalInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Total Nominal yang Diajukan (Rp)") },
                                modifier = Modifier.fillMaxWidth().testTag("input_audit_nominal")
                            )

                            val nom = nominalInput.toLongOrNull() ?: 0L
                            val vol = volumeInput.toDoubleOrNull() ?: 1.0
                            if (nom > 0 && vol > 0) {
                                val unitPrice = (nom / vol).toLong()
                                Text(
                                    text = "Total: ${nom.toRupiah()} • Harga Satuan: ${unitPrice.toRupiah()} / $satuanInput",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                            }

                            // Preset Quick Test Buttons
                            Text("Preset Uji Coba Cepat Pasar:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        itemNameInput = "Semen PCC 50kg"
                                        volumeInput = "100"
                                        satuanInput = "Sak"
                                        nominalInput = "6500000" // Rp 65.000 / sak (Wajar)
                                        viewModel.performAiPriceAudit("Semen PCC 50kg", 6500000, 100.0, "BLD-001", "Sak")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                                ) {
                                    Text("Semen Wajar", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        itemNameInput = "Semen PCC 50kg"
                                        volumeInput = "100"
                                        satuanInput = "Sak"
                                        nominalInput = "11500000" // Rp 115.000 / sak (ALARM MARKUP!)
                                        viewModel.performAiPriceAudit("Semen PCC 50kg", 11500000, 100.0, "BLD-001", "Sak")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                                ) {
                                    Text("Semen Alarm!", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        itemNameInput = "Besi Beton Ulir D13"
                                        volumeInput = "50"
                                        satuanInput = "Batang"
                                        nominalInput = "9500000" // Rp 190.000 / batang (ALARM MARKUP!)
                                        viewModel.performAiPriceAudit("Besi Beton Ulir D13", 9500000, 50.0, "BLD-001", "Batang")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                                ) {
                                    Text("Besi Alarm!", fontSize = 10.sp)
                                }
                            }

                            // Trigger Analysis Button
                            Button(
                                onClick = {
                                    val safeNom = nominalInput.toLongOrNull() ?: 0L
                                    val safeVol = volumeInput.toDoubleOrNull() ?: 1.0
                                    viewModel.performAiPriceAudit(
                                        itemName = itemNameInput,
                                        nominal = safeNom,
                                        volume = safeVol,
                                        costCode = selectedCostCode,
                                        satuanInput = satuanInput
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("btn_trigger_price_audit"),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                            ) {
                                if (isPriceAuditLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Riset Pasar Sedang Berjalan...", color = Color.White)
                                } else {
                                    Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentGoldLight)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Jalankan Riset AI & Deteksi Alarm", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Latest Analysis Card Result
                if (latestAnalysis != null) {
                    item {
                        Text("Hasil Analisis Riset Pasar Terkini:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        AiNominalAlarmCard(analysis = latestAnalysis!!)
                    }
                }

                // Historical / Active Nominal Alarms List
                if (alarmsList.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Alarm Nominal Terdeteksi (${alarmsList.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusRed
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusRedLight
                            ) {
                                Text(
                                    text = "PERLU AUDIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    items(alarmsList) { alarm ->
                        AiNominalAlarmCard(analysis = alarm)
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: TANYA AI & SCAN NOTA
            Column(modifier = Modifier.fillMaxSize()) {
                // Quick Suggestion Chips
                Text("Topik Populer (Bahasa Awam):", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickQuestions.take(2).forEach { q ->
                        AssistChip(
                            onClick = { viewModel.sendAiQuestion(q) },
                            label = { Text(q, maxLines = 1, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chat Message Thread
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { (text, isUser) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) NavyPrimary else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NavyPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang memproses analisa akuntansi...", style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                            }
                        }
                    }
                }

                // Input Bar & Photo Scanner Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val sampleReceipt = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).apply {
                                val canvas = Canvas(this)
                                canvas.drawColor(AndroidColor.WHITE)
                                val paint = Paint().apply {
                                    color = AndroidColor.BLACK
                                    textSize = 20f
                                }
                                canvas.drawText("KWITANSI MATERIAL", 20f, 40f, paint)
                                canvas.drawText("Toko Bangunan Sumber Berkah", 20f, 80f, paint)
                                canvas.drawText("Besi Beton 10mm & Semen", 20f, 120f, paint)
                                canvas.drawText("Rp 4.500.000,-", 20f, 160f, paint)
                            }
                            viewModel.analyzeReceipt(sampleReceipt)
                        },
                        modifier = Modifier.testTag("btn_scan_receipt_camera")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan Nota", tint = NavyPrimary)
                    }

                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Tanyakan pembukuan / aturan SAK...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_ai_query"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                viewModel.sendAiQuestion(inputQuery)
                                inputQuery = ""
                            }
                        },
                        modifier = Modifier.testTag("btn_send_ai_query")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim", tint = NavyPrimary)
                    }
                }
            }
        } else {
            // TAB 2: IDENTITAS RESMI (KOP & CAP RESMI PT GEMA ABADI NUGRAHA)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Master Identitas Legal & Korporasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NavyDark
                    )
                    Text(
                        text = "Standar resmi kop surat, logo heksagonal baja, dan cap stempel digital bersegel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Official Letterhead Preview Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PREVIEW KOP SURAT RESMI:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            KopSuratResmi(
                                subJudul = "DEVELOPER PERUMAHAN & REAL ESTATE",
                                nomorDokumen = "001/DIR-GAN/SPK/X/2026"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Perihal: Surat Perintah Kerja (SPK) & Otorisasi Pengadaan Material",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Berdasarkan hasil audit anggaran dan verifikasi kewajaran harga pasar oleh Komite Anggaran PT Gema Abadi Nugraha, dokumen ini dinyatakan SAH dan memenuhi standar akuntansi SAK EP.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Signature & Stamp Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Mengetahui,", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text("Ahmad Fauzi, S.E.", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("General Manager", fontSize = 10.sp, color = Color.Gray)
                                }

                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Ciamis, 03 Oktober 2026", fontSize = 11.sp)
                                        Text("Direktur Utama,", fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(40.dp))
                                        Text("H. Bambang Nugraha", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("PT Gema Abadi Nugraha", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    CapStempelResmi(
                                        modifier = Modifier.offset(x = 10.dp, y = 8.dp),
                                        rotationDegrees = -12f,
                                        labelStatus = "ASLI & TERDAFTAR",
                                        namaApprover = "H. Bambang Nugraha"
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Deskripsi Lambang & Segel:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NavyDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Logo PT Gema Abadi Nugraha: Rangka heksagonal baja monokrom melambangkan kekokohan fondasi properti, menara kembar melambangkan pertumbuhan kawasan hunian modern, serta gelombang oranye melambangkan gema kesejahteraan berkeadilan.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Cap Stempel Digital GM: Bersegel melingkar dengan pita UNGGAS (GM) & HALAL TERJAGA, menjamin keaslian bukti pembayaran, kwitansi serah terima, dan validitas transaksi sistem.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
