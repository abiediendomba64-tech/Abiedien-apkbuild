package com.example.domain

object TaxEngine {

    /**
     * PPh Final Pasal 4 Ayat 2 atas Pengalihan Hak atas Tanah dan/atau Bangunan (2.5% dari Nilai Pengalihan)
     */
    fun hitungPphFinalPengalihan(hargaTransaksi: Long): Long {
        return (hargaTransaksi * 0.025).toLong()
    }

    /**
     * BPHTB (Bea Perolehan Hak atas Tanah dan Bangunan)
     * Tarif 5% x (Nilai Transaksi - NPOPTKP)
     * Standar NPOPTKP Rumah Tinggal = Rp 80.000.000 (dapat disesuaikan perda)
     */
    fun hitungBphtb(hargaTransaksi: Long, npoptkp: Long = 80_000_000L): Long {
        val dasarPengenaan = maxOf(0L, hargaTransaksi - npoptkp)
        return (dasarPengenaan * 0.05).toLong()
    }

    /**
     * PPN Properti (Tarif Standar 11%)
     */
    fun hitungPpn(hargaNett: Long, tarifPersen: Double = 11.0): Long {
        return (hargaNett * (tarifPersen / 100.0)).toLong()
    }

    /**
     * Simulasi Insentif PPN DTP (Pemerintah Menanggung Pajak)
     * Fasilitas hingga pagu Rp 2.000.000.000 (DTP 100% atau 50%)
     */
    data class PpnDtpResult(
        val ppnTotal: Long,
        val ppnDitanggungPemerintah: Long,
        val ppnDibayarCustomer: Long,
        val statusInsentif: String
    )

    fun hitungPpnDtp(hargaRumah: Long, persentaseDtp: Double = 100.0): PpnDtpResult {
        val ppnTotal = hitungPpn(hargaRumah)
        return if (hargaRumah <= 2_000_000_000L) {
            val dtp = (ppnTotal * (persentaseDtp / 100.0)).toLong()
            PpnDtpResult(
                ppnTotal = ppnTotal,
                ppnDitanggungPemerintah = dtp,
                ppnDibayarCustomer = ppnTotal - dtp,
                statusInsentif = "Memenuhi Syarat PPN DTP ${persentaseDtp.toInt()}% (Rumah < Rp 2 Miliar)"
            )
        } else if (hargaRumah <= 5_000_000_000L) {
            // PPN DTP hanya sampai batas Rp 2 M pertama
            val dasarDtp = 2_000_000_000L
            val dtp = (hitungPpn(dasarDtp) * (persentaseDtp / 100.0)).toLong()
            PpnDtpResult(
                ppnTotal = ppnTotal,
                ppnDitanggungPemerintah = dtp,
                ppnDibayarCustomer = ppnTotal - dtp,
                statusInsentif = "Sebagian DTP (Rp 2 Miliar Pertama dari Rp 5 Miliar)"
            )
        } else {
            PpnDtpResult(
                ppnTotal = ppnTotal,
                ppnDitanggungPemerintah = 0L,
                ppnDibayarCustomer = ppnTotal,
                statusInsentif = "Tidak Memenuhi Syarat (Harga > Rp 5 Miliar)"
            )
        }
    }
}
