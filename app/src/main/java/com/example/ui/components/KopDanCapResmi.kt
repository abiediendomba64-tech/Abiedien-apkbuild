package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.PriceResearchAnalysis
import com.example.ui.theme.*

@Composable
fun KopSuratResmi(
    modifier: Modifier = Modifier,
    subJudul: String = "DEVELOPER PERUMAHAN & REAL ESTATE",
    nomorDokumen: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("kop_surat_resmi")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Corporate Logo PT GAN
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.5.dp, Color(0xFFB0BEC5), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_pt_gan),
                    contentDescription = "Logo PT Gema Abadi Nugraha",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PT GEMA ABADI NUGRAHA",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = NavyDark,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subJudul,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentGold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Gunung Padang, Ciamis • NIB: 0123456789012 • NPWP: 01.234.567.8-442.000",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Jl. Raya Ciamis - Banjar No. 88, Ciamis • Telp: (0265) 771234 • Email: finance@gemaabadi.id",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }

        if (nomorDokumen.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Ref: $nomorDokumen",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NavyPrimary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Double Horizontal Divider for official letterhead
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(NavyDark)
        )
        Spacer(modifier = Modifier.height(1.5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AccentGold)
        )
    }
}

@Composable
fun CapStempelResmi(
    modifier: Modifier = Modifier,
    rotationDegrees: Float = -12f,
    labelStatus: String = "ASLI & TERVERIFIKASI",
    namaApprover: String = "H. Bambang Nugraha"
) {
    Box(
        modifier = modifier
            .rotate(rotationDegrees)
            .testTag("cap_stempel_resmi"),
        contentAlignment = Alignment.Center
    ) {
        // Authentic Circular Stamp
        Surface(
            modifier = Modifier
                .size(92.dp)
                .border(2.dp, Color(0xFF1E5288).copy(alpha = 0.85f), CircleShape),
            shape = CircleShape,
            color = Color(0xFF1E5288).copy(alpha = 0.04f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_cap_stempel_resmi),
                    contentDescription = "Cap Stempel Resmi",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    alpha = 0.88f
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = Color(0xFF1E5288).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = labelStatus,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = namaApprover,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E5288),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun AiNominalAlarmCard(
    analysis: PriceResearchAnalysis,
    modifier: Modifier = Modifier,
    onIgnore: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_ai_nominal_alarm"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (analysis.isAlarmTriggered) Color(0xFFFFF0F0) else if (analysis.alarmSeverity == "PERINGATAN_MARGINAL") Color(0xFFFFFBEA) else Color(0xFFF0FDF4)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (analysis.isAlarmTriggered) StatusRed else if (analysis.alarmSeverity == "PERINGATAN_MARGINAL") AccentGold else StatusGreen
            ),
            width = if (analysis.isAlarmTriggered) 2.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (analysis.isAlarmTriggered) Icons.Default.Warning else if (analysis.alarmSeverity == "PERINGATAN_MARGINAL") Icons.Default.Info else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (analysis.isAlarmTriggered) StatusRed else if (analysis.alarmSeverity == "PERINGATAN_MARGINAL") AccentGold else StatusGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.statusLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (analysis.isAlarmTriggered) StatusRed else if (analysis.alarmSeverity == "PERINGATAN_MARGINAL") Color(0xFFB45309) else Color(0xFF15803D)
                    )
                    Text(
                        text = "Riset Pasar: ${analysis.referenceStandard}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Benchmark Comparison Matrix
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Batas Bawah Pasar", fontSize = 10.sp, color = Color.Gray)
                        Text(analysis.benchmarkMin.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Harga Wajar Rata2", fontSize = 10.sp, color = Color.Gray)
                        Text(analysis.benchmarkAvg.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Batas Maksimal PU", fontSize = 10.sp, color = Color.Gray)
                        Text(analysis.benchmarkMax.toRupiah(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (analysis.isAlarmTriggered) StatusRed else Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = analysis.researchSummary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.85f),
                fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (analysis.isAlarmTriggered) StatusRed.copy(alpha = 0.12f) else NavyLight
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = if (analysis.isAlarmTriggered) StatusRed else NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rekomendasi AI: ${analysis.recommendation}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (analysis.isAlarmTriggered) StatusRed else NavyDark
                    )
                }
            }
        }
    }
}
