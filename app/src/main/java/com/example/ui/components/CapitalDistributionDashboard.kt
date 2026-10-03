package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MitraInvestorRecord
import com.example.ui.theme.*

data class EntityStakeData(
    val entityName: String,
    val entityType: String, // "INDUK", "ANAK", "INVESTOR"
    val nominalCapital: Long,
    val stakePercentage: Double,
    val color: Color,
    val hakBagiHasil: String,
    val roleDescription: String
)

@Composable
fun CapitalDistributionDashboard(
    investorList: List<MitraInvestorRecord>,
    modifier: Modifier = Modifier,
    onAddInvestorClick: () -> Unit = {}
) {
    // Total Investor Capital
    val totalInvestorCapital = investorList.sumOf { it.modalDisetor }.let { if (it > 0) it else 3_500_000_000L }

    // Proportional Equity Stakes
    // PT Induk: Rp 8.250.000.000 (55%)
    // PT Anak: Rp 3.000.000.000 (20%)
    // Mitra Investor: Rp 3.750.000.000 (25%)
    val indukCapital = 8_250_000_000L
    val anakCapital = 3_000_000_000L
    val totalCapitalPool = indukCapital + anakCapital + totalInvestorCapital

    val indukPct = (indukCapital.toDouble() / totalCapitalPool.toDouble()) * 100.0
    val anakPct = (anakCapital.toDouble() / totalCapitalPool.toDouble()) * 100.0
    val investorPct = (totalInvestorCapital.toDouble() / totalCapitalPool.toDouble()) * 100.0

    val stakes = listOf(
        EntityStakeData(
            entityName = "PT Gema Abadi Nugraha (Induk)",
            entityType = "INDUK",
            nominalCapital = indukCapital,
            stakePercentage = indukPct,
            color = Color(0xFF1E3A8A), // Deep Navy
            hakBagiHasil = "55.0% Laba Bersih Konsolidasi",
            roleDescription = "Pemegang Saham Mayoritas & Penjamin Landbank Proyek"
        ),
        EntityStakeData(
            entityName = "PT Setia Surya Nugraha (Anak)",
            entityType = "ANAK",
            nominalCapital = anakCapital,
            stakePercentage = anakPct,
            color = Color(0xFF0284C7), // Sky Blue
            hakBagiHasil = "20.0% Laba Operasional Proyek",
            roleDescription = "Entitas Pelaksana Konstruksi & Pengelola Unit"
        ),
        EntityStakeData(
            entityName = "Konsorsium Mitra Investor",
            entityType = "INVESTOR",
            nominalCapital = totalInvestorCapital,
            stakePercentage = investorPct,
            color = Color(0xFFD97706), // Amber / Gold
            hakBagiHasil = "25.0% Deviden Bagi Hasil Proyek",
            roleDescription = "Penyertaan Modal Usaha & Likuiditas Infrastruktur"
        )
    )

    var showSimulation by remember { mutableStateOf(false) }
    var simulatedProfitText by remember { mutableStateOf("1000000000") } // Rp 1 Miliar
    val simulatedProfit = simulatedProfitText.toLongOrNull() ?: 1_000_000_000L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_capital_distribution_dashboard"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STRUKTUR MODAL & KEPEMILIKAN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NavyDark
                    )
                    Text(
                        text = "Distribusi Modal Induk, Anak PT & Mitra Investor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavyLight
                ) {
                    Text(
                        text = "SAK EP KONSOLIDASI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Layer 1: Total Capital Metric Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = NavyDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Modal Kerja Konsolidasi:",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text = totalCapitalPool.toRupiah(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = AccentGoldLight
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "3 Entitas Modal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${investorList.size} Investor Terdaftar",
                            fontSize = 10.sp,
                            color = StatusGreenLight
                        )
                    }
                }
            }

            // Layer 2: Multi-Layered Proportional Segmented Visualizer Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Visual Proporsi Kepemilikan (Stakes %):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.DarkGray
                    )
                    Text(
                        text = "100.0%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NavyPrimary
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val totalWidth = size.width
                    val indukWidth = (indukPct.toFloat() / 100f) * totalWidth
                    val anakWidth = (anakPct.toFloat() / 100f) * totalWidth
                    val investorWidth = totalWidth - (indukWidth + anakWidth)

                    // Draw Induk bar
                    drawRect(
                        color = Color(0xFF1E3A8A),
                        topLeft = Offset(0f, 0f),
                        size = Size(indukWidth, size.height)
                    )

                    // Draw Anak bar
                    drawRect(
                        color = Color(0xFF0284C7),
                        topLeft = Offset(indukWidth, 0f),
                        size = Size(anakWidth, size.height)
                    )

                    // Draw Investor bar
                    drawRect(
                        color = Color(0xFFD97706),
                        topLeft = Offset(indukWidth + anakWidth, 0f),
                        size = Size(investorWidth, size.height)
                    )
                }

                // Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stakes.forEach { stake ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(stake.color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stake.entityType}: ${String.format("%.1f", stake.stakePercentage)}%",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Layer 3: Proportional Stake Detail Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stakes.forEach { stake ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(stake.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stake.entityName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stake.roleDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Hak: ${stake.hakBagiHasil}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = stake.color
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stake.nominalCapital.toRupiah(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = stake.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", stake.stakePercentage)}% Stake",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = stake.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Layer 4: Interactive Simulation Section (Dividen & Pembagian Hasil)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulasi Pembagian Dividen / Laba",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NavyDark
                    )
                    TextButton(onClick = { showSimulation = !showSimulation }) {
                        Icon(
                            imageVector = if (showSimulation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(if (showSimulation) "Tutup" else "Buka Simulasi")
                    }
                }

                AnimatedVisibility(visible = showSimulation) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyLight, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Masukkan Target Laba Proyek yang Dibagikan:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyDark
                        )

                        OutlinedTextField(
                            value = simulatedProfitText,
                            onValueChange = { simulatedProfitText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Laba Bersih yang Dibagikan (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = "Total Dividen: ${simulatedProfit.toRupiah()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Calculated shares for each entity
                        val indukShare = (simulatedProfit * (indukPct / 100.0)).toLong()
                        val anakShare = (simulatedProfit * (anakPct / 100.0)).toLong()
                        val investorShare = (simulatedProfit * (investorPct / 100.0)).toLong()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1. PT Induk (GAN):", fontSize = 11.sp)
                            Text(indukShare.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2. PT Anak (SSN):", fontSize = 11.sp)
                            Text(anakShare.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("3. Mitra Investor Total:", fontSize = 11.sp)
                            Text(investorShare.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }

                        if (investorList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rincian per Investor Terdaftar:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            investorList.forEach { inv ->
                                val shareOfInvestorPool = if (totalInvestorCapital > 0) inv.modalDisetor.toDouble() / totalInvestorCapital.toDouble() else 0.0
                                val invDividend = (investorShare * shareOfInvestorPool).toLong()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${inv.namaInvestor} (${inv.persentaseBagiHasil}%):", fontSize = 10.sp)
                                    Text(invDividend.toRupiah(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
