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
import com.example.ui.components.toRupiah
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun KontraktorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val kontraktorList by viewModel.allKontraktor.collectAsStateWithLifecycle()

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
                        text = "MANAJEMEN KONTRAKTOR & SPK PROYEK",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Pengelolaan progress fisik lapangan, termin, retensi pemeliharaan 5%, dan BAST fisik.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Daftar SPK Pekerjaan Konstruksi (${kontraktorList.size})",
                subtitle = "Kontrak & Progress Fisik"
            )
        }

        items(kontraktorList) { spk ->
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
                            text = "${spk.spkId} • ${spk.kontraktorNama}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        StatusBadge(status = spk.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = spk.jenisPekerjaan, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        text = "Nilai Kontrak: ${spk.nilaiKontrak.toRupiah()} • Uang Muka: ${spk.uangMuka.toRupiah()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress Fisik Lapangan:", style = MaterialTheme.typography.bodySmall)
                        Text("${spk.progressFisikPersen}%", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Retensi Pemeliharaan (5%):", style = MaterialTheme.typography.bodySmall)
                        Text(spk.retensiNilai.toRupiah(), fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Terbayar Termin:", style = MaterialTheme.typography.bodySmall)
                        Text(spk.terbayarTermin.toRupiah(), fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sisa Nilai Kontrak:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text(spk.sisaKontrak.toRupiah(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
