package com.example.domain

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Throwable) {
            ""
        }

    suspend fun generateAccountingAdvice(
        prompt: String,
        systemInstruction: String = "Anda adalah Asisten Pakar Akuntansi Keuangan Pengembang Properti dan Real Estate (SAK EP Indonesia) untuk PT Gema Abadi Nugraha & PT Setia Surya Nugraha. Berikan jawaban yang tepat, lugas, profesional, dan gunakan Bahasa Awam yang mudah dimengerti manajemen lapangan."
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Fallback to rich built-in knowledge response
                return@withContext Result.success(getFallbackResponse(prompt))
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(getFallbackResponse(prompt))
                }
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.getJSONObject(0)?.optString("text") ?: ""
                    Result.success(text)
                } else {
                    Result.success(getFallbackResponse(prompt))
                }
            }
        } catch (e: Exception) {
            Result.success(getFallbackResponse(prompt))
        }
    }

    suspend fun analyzeReceipt(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.success(
                    "Simulasi Analisis Kwitansi / Nota:\n" +
                    "- Tanggal Terdeteksi: 2026-10-02\n" +
                    "- Pihak / Toko: Toko Material Bangunan Setia\n" +
                    "- Perkiraan Nilai: Rp 4.500.000\n" +
                    "- Rekomendasi Cost Code: BLD-002 (Dinding, Plester & Atap)\n" +
                    "- Rekomendasi Akun: 1410 (WIP Konstruksi Bangunan Dalam Proses)\n" +
                    "- Status Fisik: Sah (terdapat stempel toko & tanda tangan penerima)."
                )
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

            val prompt = "Periksa dokumen kwitansi/nota/invoice proyek properti ini. Ekstrak data: 1) Tanggal, 2) Nama Toko/Penerima, 3) Jumlah Nominal (Rp), 4) Uraian Barang/Jasa, 5) Rekomendasi Cost Code (LND, INF, BLD, OVH), 6) Rekomendasi Akun COA (WIP 1410/1420 atau Beban 6100), 7) Keabsahan dokumen (tanda tangan/stempel)."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP Error ${response.code}"))
                }
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0).optJSONObject("content")
                        ?.optJSONArray("parts")?.getJSONObject(0)?.optString("text") ?: ""
                    Result.success(text)
                } else {
                    Result.failure(Exception("Gagal mengurai respons Gemini"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun researchPriceWithAi(
        itemName: String,
        nominal: Long,
        volume: Double = 1.0,
        costCode: String = ""
    ): PriceResearchAnalysis = withContext(Dispatchers.IO) {
        val baseAnalysis = AiPriceAuditor.audit(
            itemName = itemName,
            totalNominal = nominal,
            volume = volume,
            costCode = costCode
        )

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext baseAnalysis
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = "Lakukan riset harga pasar konstruksi dan properti Indonesia (wilayah Jawa Barat / Priangan Timur 2026) untuk: '$itemName' sebanyak $volume dengan total harga Rp $nominal (harga satuan Rp ${baseAnalysis.unitPriceNominal}). Apakah harga ini wajar dan masuk akal? Jika tidak wajar atau terindikasi markup kemahalan, berikan batas harga wajar dan rekomendasi audit."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Anda adalah Auditor Estimator RAB dan Pengadaan Properti SAK EP. Analisa kewajaran harga pasar dengan kritis dan objektif.")
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0).optJSONObject("content")
                            ?.optJSONArray("parts")?.getJSONObject(0)?.optString("text") ?: ""
                        if (text.isNotBlank()) {
                            return@withContext baseAnalysis.copy(
                                researchSummary = text.take(350) + "...",
                                referenceStandard = "Gemini AI Market Intelligence & SNI 2026"
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Keep baseAnalysis
        }

        return@withContext baseAnalysis
    }

    suspend fun analyzeRabExpenseAnomaliesWithAi(
        transactions: List<com.example.data.TransaksiKasBankRecord>,
        anggaranList: List<com.example.data.AnggaranProyek>
    ): AnomalyAuditReport = withContext(Dispatchers.IO) {
        val baseReport = AiExpenseAnomalyAuditor.auditTransactions(transactions, anggaranList)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || baseReport.anomaliesCount == 0) {
            return@withContext baseReport
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val anomaliesBrief = baseReport.items.take(5).joinToString("; ") {
                "${it.costCode}: Rp ${it.nominal} (${it.itemDescription}) - ${it.anomalyType}"
            }
            val prompt = "Sebagai Auditor Forensik Anggaran Properti SAK EP, evaluasi anomali belanja proyek berikut: $anomaliesBrief. Berikan ringkasan audit singkat, risiko kebocoran kas, dan arahan pengendalian internal."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0).optJSONObject("content")
                            ?.optJSONArray("parts")?.getJSONObject(0)?.optString("text") ?: ""
                        if (text.isNotBlank()) {
                            return@withContext baseReport.copy(
                                auditSummary = "🧠 AI Gemini Audit: " + text.take(300) + "..."
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Keep baseReport
        }

        return@withContext baseReport
    }

    private fun getFallbackResponse(query: String): String {
        val q = query.lowercase()
        return when {
            "wip" in q || "kapitalisasi" in q || "aset" in q -> {
                "💡 **Penjelasan Logika Akuntansi SAK EP (Bahasa Awam):**\n\n" +
                "Pengeluaran pembangunan rumah (semen, pasir, upah tukang fisik) **TIDAK langsung diakui sebagai Beban/Rugi**, melainkan dicatat sebagai **WIP (Work In Progress / Pekerjaan Dalam Proses - Akun 1410)** yang merupakan ASET LANCAR.\n\n" +
                "WIP ini nantinya akan berubah menjadi **Persediaan Rumah Siap Huni (Akun 1510)** ketika fisik 100% jadi, dan baru menjadi **HPP (Harga Pokok Penjualan - Akun 5100)** saat unit diserahterimakan (BAST) kepada pembeli."
            }
            "intercompany" in q || "anak" in q || "induk" in q -> {
                "🏢 **Aturan Intercompany (Induk vs Anak PT):**\n\n" +
                "Dana yang ditransfer dari PT Gema Abadi Nugraha (Induk) ke PT Setia Surya Nugraha (Anak) **BUKAN BEBAN** dan **BUKAN PENDAPATAN**.\n\n" +
                "- **PT Induk mencatat:** Dr 1230 Piutang Intercompany / Cr 1110 Bank\n" +
                "- **PT Anak mencatat:** Dr Aset/WIP / Cr 2430 Hutang Intercompany\n" +
                "- Saat dana dikembalikan (Settlement): Hanya pemulihan kas (Dr Bank / Cr 1230), tidak boleh diakui sebagai laba usaha!"
            }
            "petty cash" in q || "kas kecil" in q -> {
                "💰 **Aturan Kontrol Kas Kecil (Petty Cash):**\n\n" +
                "1. Kas kecil dicairkan dengan akun 1320 (Uang Muka Operasional).\n" +
                "2. Setiap pengeluaran wajib disertai nomor nota fisik dan diverifikasi.\n" +
                "3. **KONTROL KUNCI:** Staf yang masih memiliki advance berstatus OPEN/OVERDUE tidak boleh diberikan advance baru (LOCKED) sampai advance sebelumnya diselesaikan (SETTLED)."
            }
            "pajak" in q || "pph" in q || "ppn" in q -> {
                "🏛️ **Kepatuhan Pajak Developer Properti:**\n\n" +
                "- **PPh Final Pasal 4(2):** 2,5% dari nilai bruto penjualan/pengalihan hak tanah & bangunan (bersifat final, dibayar sebelum AJB).\n" +
                "- **PPN Properti:** 11% (tersedia fasilitas PPN DTP 100% atau 50% untuk rumah tapak harga s/d Rp 2 Miliar).\n" +
                "- **BPHTB:** 5% x (Nilai Transaksi - NPOPTKP) menjadi tanggungan pembeli."
            }
            else -> {
                "Sistem Pembukuan Developer Properti SAK EP memisahkan arus kas riil dari pengakuan laba/rugi. Pastikan setiap transaksi memiliki Cost Code yang sah, disetujui sesuai matriks kepangkatan, dan didukung bukti dokumen fisik resmi di Dokumen Registry."
            }
        }
    }
}
