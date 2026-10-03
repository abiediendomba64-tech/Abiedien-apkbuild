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
fun IntercompanyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val icList by viewModel.allIntercompany.collectAsStateWithLifecycle()

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
                        text = "INTERCOMPANY TRANSAKSI (INDUK - ANAK PT)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "PT Gema Abadi Nugraha (Induk) ➔ PT Setia Surya Nugraha (Anak).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aturan SAK EP: Aliran dana antar-entitas BUKAN BEBAN dan BUKAN LABA. Menggunakan Akun 1230 (Piutang Intercompany) di Induk dan 2430 (Hutang Intercompany) di Anak. Pelunasan/Settlement adalah pemulihan kas murni.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGoldLight
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Register Piutang / Hutang Intercompany (${icList.size})",
                subtitle = "Pencocokan Voucher Dua Sisi"
            )
        }

        items(icList) { ic ->
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
                            text = ic.id,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        StatusBadge(status = ic.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Sumber: ${ic.entitySumber}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(text = "Tujuan: ${ic.entityTujuan} (Proyek ${ic.projectId})", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Plafon Talangan: ${ic.nominal.toRupiah()}", style = MaterialTheme.typography.bodySmall)
                        Text("Telah Settle: ${ic.settlementTotal.toRupiah()}", style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                    }
                    Text(
                        text = "Sisa Outstanding: ${ic.outstanding.toRupiah()}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (ic.outstanding > 0) StatusYellow else StatusGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Voucher Sumber: ${ic.voucherSumberTxId} • Voucher Tujuan: ${ic.voucherTujuanTxId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Keterangan: ${ic.keterangan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
