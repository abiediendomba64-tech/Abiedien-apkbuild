package com.example.data

object DataSeeder {
    fun getInitialPT(): List<MasterPT> = listOf(
        MasterPT(
            id = "PT-001",
            nama = "PT Gema Abadi Nugraha",
            nib = "0123456789012",
            npwp = "01.234.567.8-441.000",
            alamat = "Jl. Raya Ciamis - Banjar No. 88, Ciamis, Jawa Barat",
            rekeningBank = "BCA 1480029381 (BCA Operasional)",
            peran = "Induk"
        ),
        MasterPT(
            id = "PT-002",
            nama = "PT Setia Surya Nugraha",
            nib = "9876543210987",
            npwp = "02.345.678.9-441.000",
            alamat = "Jl. Jendral Sudirman No. 45, Tasikmalaya",
            rekeningBank = "BCA 1480099211 (BCA Proyek)",
            peran = "Anak"
        )
    )

    fun getInitialProyek(): List<MasterProyek> = listOf(
        MasterProyek(
            id = "PRJ-001",
            nama = "Perumahan Gunung Padang, Ciamis",
            entityId = "PT-001",
            unitCount = 120,
            totalBudget = 18_500_000_000L,
            lokasi = "Gunung Padang, Ciamis, Jawa Barat",
            status = "BERJALAN"
        ),
        MasterProyek(
            id = "PRJ-002",
            nama = "Green Surya Residence",
            entityId = "PT-002",
            unitCount = 65,
            totalBudget = 9_200_000_000L,
            lokasi = "Cikoneng, Ciamis",
            status = "PERENCANAAN"
        )
    )

    fun getInitialLahan(): List<MasterLahan> = listOf(
        MasterLahan(
            id = "LND-001",
            projectId = "PRJ-001",
            pemilikAsal = "H. Dede Sutrisno",
            luasM2 = 4500.0,
            hargaPembebasan = 1_350_000_000L,
            statusLegalitas = "SHM - Atas Nama PT",
            pphPenjual = 33_750_000L,
            bphtb = 67_500_000L,
            statusKwitansi = "LUNAS"
        ),
        MasterLahan(
            id = "LND-002",
            projectId = "PRJ-001",
            pemilikAsal = "Ibu Sutija",
            luasM2 = 3200.0,
            hargaPembebasan = 960_000_000L,
            statusLegalitas = "AJB Notaris",
            pphPenjual = 24_000_000L,
            bphtb = 48_000_000L,
            statusKwitansi = "DP"
        )
    )

    fun getInitialUnits(): List<MasterUnit> = listOf(
        MasterUnit("A-01", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "READY", "SHM Pecah", "SOLD", 285_000_000L),
        MasterUnit("A-02", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "READY", "SHM Pecah", "BOOKED", 285_000_000L),
        MasterUnit("A-03", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "CONSTRUCTION", "SHM Induk", "AVAILABLE", 290_000_000L),
        MasterUnit("A-04", "PRJ-001", "Tipe 36/72", 72.0, 36.0, "CONSTRUCTION", "SHM Induk", "AVAILABLE", 290_000_000L),
        MasterUnit("B-01", "PRJ-001", "Tipe 45/90", 90.0, 45.0, "READY", "SHM Pecah", "SOLD", 395_000_000L),
        MasterUnit("B-02", "PRJ-001", "Tipe 45/90", 90.0, 45.0, "CONSTRUCTION", "SHM Induk", "BOOKED", 395_000_000L),
        MasterUnit("B-03", "PRJ-001", "Tipe 45/90", 90.0, 45.0, "PLANNING", "SHM Induk", "AVAILABLE", 410_000_000L),
        MasterUnit("C-01", "PRJ-001", "Tipe 54/108", 108.0, 54.0, "READY", "SHM Pecah", "AVAILABLE", 495_000_000L),
        MasterUnit("C-02", "PRJ-001", "Tipe 54/108", 108.0, 54.0, "CONSTRUCTION", "SHM Induk", "AVAILABLE", 510_000_000L)
    )

    fun getInitialPihak(): List<MasterPihak> = listOf(
        MasterPihak("PHK-001", "Toko Besi & Bangunan Sumber Berkah", "VENDOR", "08122345678", "BCA 889218273", "AKTIF"),
        MasterPihak("PHK-002", "CV Mega Karya Konstruksi", "KONTRAKTOR", "08139988776", "Mandiri 13100982731", "AKTIF"),
        MasterPihak("PHK-003", "Kantor Notaris & PPAT Hj. Siti Rahmawati, S.H., M.Kn.", "NOTARIS_PPAT", "0811223344", "BNI 098765432", "AKTIF"),
        MasterPihak("PHK-004", "Bapak Hendra Gunawan", "CUSTOMER", "081318575529", "-", "AKTIF"),
        MasterPihak("PHK-005", "Ibu Rina Marlina", "CUSTOMER", "085299881122", "-", "AKTIF"),
        MasterPihak("PHK-006", "PT Semen Tiga Roda Utama", "VENDOR", "082122338899", "BCA 123984711", "AKTIF")
    )

    fun getInitialCOA(): List<CoaAccount> = listOf(
        CoaAccount("1110", "Kas & Bank - BCA Operasional", "Aset Lancar", "Debit", true),
        CoaAccount("1120", "Kas & Bank - BCA Proyek", "Aset Lancar", "Debit", true),
        CoaAccount("1130", "Kas Kecil (Petty Cash)", "Aset Lancar", "Debit", true),
        CoaAccount("1210", "Piutang Usaha Penjualan Unit", "Aset Lancar", "Debit", true),
        CoaAccount("1230", "Piutang Intercompany (Induk ke Anak)", "Aset Lancar", "Debit", true),
        CoaAccount("1320", "Uang Muka Operasional & Belanja", "Aset Lancar", "Debit", true),
        CoaAccount("1410", "WIP - Konstruksi Bangunan Dalam Proses", "Aset Lancar", "Debit", true),
        CoaAccount("1420", "WIP - Lahan & Infrastruktur Dalam Proses", "Aset Lancar", "Debit", true),
        CoaAccount("1510", "Persediaan Unit Rumah Siap Huni", "Aset Lancar", "Debit", true),
        CoaAccount("1610", "Aset Tetap - Kendaraan & Alat Berat", "Aset Tetap", "Debit", true),
        CoaAccount("2110", "Hutang Usaha / Vendor & Kontraktor", "Liabilitas", "Kredit", true),
        CoaAccount("2200", "Uang Muka Customer / Booking Fee", "Liabilitas", "Kredit", true),
        CoaAccount("2430", "Hutang Intercompany (Anak ke Induk)", "Liabilitas", "Kredit", true),
        CoaAccount("3100", "Modal Disetor", "Ekuitas", "Kredit", true),
        CoaAccount("4100", "Pendapatan Penjualan Unit Properti", "Pendapatan", "Kredit", true),
        CoaAccount("5100", "HPP - Beban Pokok Penjualan Unit", "HPP", "Debit", true),
        CoaAccount("6100", "Beban Operasional, Gaji & Lapangan", "Beban Operasional", "Debit", true)
    )

    fun getInitialCostCode(): List<MasterCostCode> = listOf(
        MasterCostCode("LND-001", "Pembebasan & Legalitas Tanah", "Lahan", "1420", true),
        MasterCostCode("INF-001", "Cut & Fill dan Pematangan Lahan", "Infrastruktur", "1420", true),
        MasterCostCode("INF-002", "Saluran Drainase, Gorong-gorong & Jalan", "Infrastruktur", "1420", true),
        MasterCostCode("BLD-001", "Struktur & Pondasi Rumah", "Bangunan", "1410", true),
        MasterCostCode("BLD-002", "Dinding, Plester, Atap & Plafon", "Bangunan", "1410", true),
        MasterCostCode("BLD-003", "Finishing, Keramik, Cat & MEP Sanitasi", "Bangunan", "1410", true),
        MasterCostCode("OVH-001", "Upah Buruh Lapangan, Absensi & Gaji Staf", "Overhead", "6100", true)
    )

    fun getInitialAnggaran(): List<AnggaranProyek> = listOf(
        AnggaranProyek("ANG-001", "PRJ-001", "LND-001", "Pembebasan Tanah Blok A & B", 2_500_000_000L, 0L, 0L, 1_350_000_000L, "Pembebasan 2 sertifikat"),
        AnggaranProyek("ANG-002", "PRJ-001", "INF-001", "Pematangan Lahan Cut & Fill", 850_000_000L, 0L, 50_000_000L, 420_000_000L, "Alat berat excavator 2 unit"),
        AnggaranProyek("ANG-003", "PRJ-001", "INF-002", "Jalan Paving & Saluran Drainase", 950_000_000L, 0L, 0L, 310_000_000L, "Saluran U-Ditch 40x40"),
        AnggaranProyek("ANG-004", "PRJ-001", "BLD-001", "Pondasi & Struktur Rumah Blok A", 1_800_000_000L, 0L, 120_000_000L, 950_000_000L, "20 Unit Blok A"),
        AnggaranProyek("ANG-005", "PRJ-001", "BLD-002", "Dinding & Rangka Atap Baja Ringan", 1_500_000_000L, 0L, 80_000_000L, 620_000_000L, "Bata merah & genteng beton"),
        AnggaranProyek("ANG-006", "PRJ-001", "BLD-003", "Finishing Keramik & Sanitasi", 1_200_000_000L, 0L, 0L, 380_000_000L, "Keramik 50x50 mulia"),
        AnggaranProyek("ANG-007", "PRJ-001", "OVH-001", "Upah Tukang & Operasional Lapangan", 600_000_000L, 0L, 0L, 185_000_000L, "Payroll mingguan harian")
    )

    fun getInitialUsers(): List<UserAppEntity> = listOf(
        UserAppEntity("Operator", "Admin Finance", 0, 10_000_000L, true),
        UserAppEntity("Ir. Hendra", "Site Manager", 1, 15_000_000L, true),
        UserAppEntity("Ahmad Fauzi, S.E.", "Supervisor", 1, 15_000_000L, true),
        UserAppEntity("Siti Rahma, M.Ak.", "Head Finance", 2, 25_000_000L, true),
        UserAppEntity("H. Bambang Nugraha", "Director", 3, 50_000_000L, true),
        UserAppEntity("Drs. H. Surya Abadi", "Owner", 4, 100_000_000L, true)
    )

    fun getInitialPayroll(): List<PayrollBuruhRecord> = listOf(
        PayrollBuruhRecord(1, "E", "Eeng", "Buruh Lapangan", "Harian", 120_000L, 6.5, 780_000L, "2026-09-19", "Bayar", "6281318575529", "6 hari kerja periode 13-19 Sep"),
        PayrollBuruhRecord(2, "R", "Rohman", "Buruh Lapangan", "Harian", 120_000L, 6.0, 720_000L, "2026-09-13", "Bayar", "6281318575529", "6 hari kerja periode 13-19 Sep"),
        PayrollBuruhRecord(3, "A", "Abah", "Buruh Lapangan", "Harian", 120_000L, 7.0, 840_000L, "2026-09-13", "Bayar", "6281318575529", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(4, "E", "eteh", "Buruh Lapangan", "Harian", 50_000L, 7.0, 350_000L, "2026-09-19", "Bayar", "", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(5, "D", "Dafid", "Buruh Lapangan", "Bulanan", 1_000_000L, 30.0, 1_000_000L, "2026-08-06", "Bayar", "", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(6, "O", "Ompong", "Buruh Lapangan", "Bulanan", 700_000L, 30.0, 700_000L, "2026-09-19", "Bayar", "6281318575529", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(7, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-13", "Bayar", "", ""),
        PayrollBuruhRecord(8, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-13", "Pending", "", "Gaji periode berjalan"),
        PayrollBuruhRecord(9, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-13", "Bayar", "", ""),
        PayrollBuruhRecord(10, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-13", "Pending", "", ""),
        PayrollBuruhRecord(11, "O", "Ompong", "Buruh Lapangan", "Bulanan", 700_000L, 30.0, 700_000L, "2026-09-19", "Pending", "6281318575529", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(12, "D", "Dafid", "Buruh Lapangan", "Bulanan", 1_000_000L, 30.0, 1_000_000L, "2026-08-06", "Pending", "", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(13, "A", "Abah", "Buruh Lapangan", "Harian", 120_000L, 5.0, 600_000L, "2026-09-13", "Pending", "6281318575529", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(14, "R", "Rohman", "Buruh Lapangan", "Harian", 120_000L, 5.0, 600_000L, "2026-09-13", "Pending", "6281318575529", "6 hari kerja periode 13-19 Sep"),
        PayrollBuruhRecord(15, "E", "eteh", "Buruh Lapangan", "Harian", 50_000L, 5.0, 250_000L, "2026-09-19", "Pending", "", "Belum masuk kerja periode ini"),
        PayrollBuruhRecord(16, "A", "Abah", "Buruh Lapangan", "Harian", 120_000L, 5.0, 600_000L, "2026-09-20", "Pending", "6281318575529", ""),
        PayrollBuruhRecord(17, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-01", "Pending", "", ""),
        PayrollBuruhRecord(18, "B", "Bayu", "STAF", "Bulanan", 3_000_000L, 0.0, 3_000_000L, "2026-09-16", "Pending", "", ""),
        PayrollBuruhRecord(19, "O", "Ompong", "Buruh Lapangan", "Bulanan", 700_000L, 30.0, 700_000L, "2026-09-06", "Pending", "6281318575529", ""),
        PayrollBuruhRecord(20, "E", "eteh", "Buruh Lapangan", "Harian", 50_000L, 5.0, 250_000L, "2026-09-20", "Pending", "", ""),
        PayrollBuruhRecord(21, "D", "Dafid", "Buruh Lapangan", "Bulanan", 1_000_000L, 30.0, 1_000_000L, "2026-08-16", "Pending", "", ""),
        PayrollBuruhRecord(22, "O", "Ompong", "Buruh Lapangan", "Bulanan", 700_000L, 30.0, 700_000L, "2026-09-06", "Pending", "6281318575529", "")
    )

    fun getInitialAttendance(): List<AttendanceRecord> = listOf(
        AttendanceRecord(1, 1, "Eeng", "2026-10-02", "Hadir", "08:00", "17:00", 0.0, "Pekerjaan dinding Blok A"),
        AttendanceRecord(2, 2, "Rohman", "2026-10-02", "Lembur", "08:00", "19:00", 2.0, "Pengecoran balok dak"),
        AttendanceRecord(3, 3, "Abah", "2026-10-02", "Hadir", "08:00", "17:00", 0.0, "Finishing plesteran"),
        AttendanceRecord(4, 4, "eteh", "2026-10-02", "Hadir", "07:30", "16:30", 0.0, "Dapur konsumsi pekerja"),
        AttendanceRecord(5, 7, "Bayu", "2026-10-02", "Hadir", "08:00", "17:00", 0.0, "Admin site & logistik")
    )

    fun getInitialTaxFilings(): List<TaxFilingRecord> = listOf(
        TaxFilingRecord(
            id = "SPT-PPH42-202609",
            jenisPajak = "PPh Final Pasal 4(2)",
            masaPajak = "September 2026",
            tahunPajak = 2026,
            entityId = "PT-001",
            dasarPengenaanPajak = 665_000_000L,
            tarifPersen = 2.5,
            nominalPajak = 16_625_000L,
            kodeBilling = "019283746501",
            ntpn = "8899AABBCCDDEE11",
            statusSetor = "LUNAS",
            statusLapor = "SUDAH_LAPOR",
            tanggalBayar = "2026-09-25",
            tanggalLapor = "2026-09-26",
            buktiDocRef = "BPN-PPH-202609"
        ),
        TaxFilingRecord(
            id = "SPT-PPN-202609",
            jenisPajak = "PPN Properti & DTP",
            masaPajak = "September 2026",
            tahunPajak = 2026,
            entityId = "PT-001",
            dasarPengenaanPajak = 285_000_000L,
            tarifPersen = 11.0,
            nominalPajak = 31_350_000L,
            kodeBilling = "098712345678",
            ntpn = "1122CCDDEEFFAABB",
            statusSetor = "LUNAS",
            statusLapor = "SUDAH_LAPOR",
            tanggalBayar = "2026-09-28",
            tanggalLapor = "2026-09-29",
            buktiDocRef = "F-PAJAK-010-001"
        ),
        TaxFilingRecord(
            id = "SPT-PBB-2026",
            jenisPajak = "PBB Proyek Gunung Padang",
            masaPajak = "Tahun Buku 2026",
            tahunPajak = 2026,
            entityId = "PT-001",
            dasarPengenaanPajak = 1_850_000_000L,
            tarifPersen = 0.5,
            nominalPajak = 9_250_000L,
            kodeBilling = "011233445566",
            ntpn = "3344556677889900",
            statusSetor = "LUNAS",
            statusLapor = "SUDAH_LAPOR",
            tanggalBayar = "2026-08-15",
            tanggalLapor = "2026-08-20",
            buktiDocRef = "SPPT-PBB-2026"
        )
    )

    fun getInitialDokumen(): List<DokumenRegistryRecord> = listOf(
        DokumenRegistryRecord("DOC-001", "KW-2026/09/001", "KWITANSI", "Pelunasan Pembebasan Tanah LND-001", "H. Dede Sutrisno", "VALID", "2026-09-10", "Siti Rahma", "Asli bermaterai Rp10.000"),
        DokumenRegistryRecord("DOC-002", "SPK-001/BLD/2026", "SPK_KONTRAKTOR", "SPK Pembangunan Struktur 20 Unit Blok A", "CV Mega Karya Konstruksi", "VALID", "2026-09-01", "H. Bambang Nugraha", "Kontrak Rp 1,8 M"),
        DokumenRegistryRecord("DOC-003", "INV-8871/SB", "INVOICE_NOTA", "Besi Beton & Semen 200 Sak", "Toko Besi & Bangunan Sumber Berkah", "VALID", "2026-09-15", "Ahmad Fauzi", "Sesuai BAST fisik"),
        DokumenRegistryRecord("DOC-004", "PPB-2026/09/04", "SURAT_PENGANTAR", "Permohonan Pembayaran Upah Buruh Lapangan", "Buruh Lapangan", "VALID", "2026-09-19", "Ir. Hendra", "Daftar hadir terlampir")
    )

    fun getInitialTransaksi(): List<TransaksiKasBankRecord> = listOf(
        TransaksiKasBankRecord(
            id = "TX-20260901-000001",
            tanggal = "2026-09-01",
            tipe = "MASUK",
            rekeningBank = "BCA Operasional",
            projectId = "PRJ-001",
            costCode = "",
            akunEfektif = "3100",
            pihakId = "PHK-001",
            pihakNama = "Drs. H. Surya Abadi (Owner)",
            nominal = 3_000_000_000L,
            keterangan = "Setoran Modal Awal Proyek Perumahan Gunung Padang",
            docRef = "DOC-001",
            dibuatOleh = "Operator",
            disetujuiOleh1 = "H. Bambang Nugraha",
            disetujuiOleh2 = "Drs. H. Surya Abadi",
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            isPosted = true
        ),
        TransaksiKasBankRecord(
            id = "TX-20260910-000002",
            tanggal = "2026-09-10",
            tipe = "KELUAR",
            rekeningBank = "BCA Operasional",
            projectId = "PRJ-001",
            costCode = "LND-001",
            akunEfektif = "1420",
            pihakId = "PHK-001",
            pihakNama = "H. Dede Sutrisno",
            nominal = 1_350_000_000L,
            keterangan = "Pembayaran Pelunasan Lahan Blok A (4.500 m2)",
            docRef = "KW-2026/09/001",
            dibuatOleh = "Operator",
            disetujuiOleh1 = "H. Bambang Nugraha",
            disetujuiOleh2 = "Drs. H. Surya Abadi",
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            candidateActual = 1_350_000_000L,
            isPosted = true
        ),
        TransaksiKasBankRecord(
            id = "TX-20260915-000003",
            tanggal = "2026-09-15",
            tipe = "KELUAR",
            rekeningBank = "BCA Operasional",
            projectId = "PRJ-001",
            costCode = "BLD-001",
            akunEfektif = "1410",
            pihakId = "PHK-001",
            pihakNama = "Toko Besi & Bangunan Sumber Berkah",
            nominal = 150_000_000L,
            keterangan = "Pembelian Besi Beton 10mm & 8mm untuk Struktur Blok A",
            docRef = "INV-8871/SB",
            dibuatOleh = "Operator",
            disetujuiOleh1 = "H. Bambang Nugraha",
            disetujuiOleh2 = "Drs. H. Surya Abadi",
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            candidateActual = 150_000_000L,
            isPosted = true
        ),
        TransaksiKasBankRecord(
            id = "TX-20260919-000004",
            tanggal = "2026-09-19",
            tipe = "KELUAR",
            rekeningBank = "Kas Kecil",
            projectId = "PRJ-001",
            costCode = "OVH-001",
            akunEfektif = "6100",
            pihakId = "PHK-001",
            pihakNama = "Eeng & Buruh Lapangan",
            nominal = 3_690_000L,
            keterangan = "Pembayaran Gaji Mingguan Buruh Lapangan (Periode 13-19 Sep)",
            docRef = "PPB-2026/09/04",
            dibuatOleh = "Operator",
            disetujuiOleh1 = "Ahmad Fauzi, S.E.",
            disetujuiOleh2 = "",
            statusInput = "SUBMITTED",
            statusSistem = "POSTED",
            candidateActual = 3_690_000L,
            isPosted = true
        )
    )

    fun getInitialKontrak(): List<PenjualanUnitContract> = listOf(
        PenjualanUnitContract(
            noKontrak = "KTR-2026-001",
            tanggal = "2026-09-05",
            unitId = "A-01",
            projectId = "PRJ-001",
            customerNama = "Bapak Hendra Gunawan",
            customerNik = "3207011985050001",
            skema = "KPR",
            hargaBruto = 285_000_000L,
            diskon = 5_000_000L,
            bookingFee = 5_000_000L,
            totalDp = 25_000_000L,
            kprPlafond = 250_000_000L,
            terbayarValid = 30_000_000L,
            status = "ACTIVE"
        ),
        PenjualanUnitContract(
            noKontrak = "KTR-2026-002",
            tanggal = "2026-09-12",
            unitId = "B-01",
            projectId = "PRJ-001",
            customerNama = "Ibu Rina Marlina",
            customerNik = "3207011990100002",
            skema = "CASH_KERAS",
            hargaBruto = 395_000_000L,
            diskon = 15_000_000L,
            bookingFee = 10_000_000L,
            totalDp = 70_000_000L,
            kprPlafond = 0L,
            terbayarValid = 380_000_000L,
            status = "LUNAS"
        )
    )

    fun getInitialKontraktor(): List<KontraktorSpkRecord> = listOf(
        KontraktorSpkRecord(
            spkId = "SPK-001",
            kontraktorNama = "CV Mega Karya Konstruksi",
            projectId = "PRJ-001",
            jenisPekerjaan = "Pembangunan Struktur & Dinding Blok A (20 Unit)",
            nilaiKontrak = 1_800_000_000L,
            uangMuka = 360_000_000L,
            retensiPersen = 5.0,
            progressFisikPersen = 58.5,
            terbayarTermin = 850_000_000L,
            status = "BERJALAN"
        ),
        KontraktorSpkRecord(
            spkId = "SPK-002",
            kontraktorNama = "CV Sinar Mandiri Pradana",
            projectId = "PRJ-001",
            jenisPekerjaan = "Cut & Fill Lahan Zona Barat",
            nilaiKontrak = 450_000_000L,
            uangMuka = 90_000_000L,
            retensiPersen = 5.0,
            progressFisikPersen = 95.0,
            terbayarTermin = 405_000_000L,
            status = "BERJALAN"
        )
    )

    fun getInitialIntercompany(): List<IntercompanyRecord> = listOf(
        IntercompanyRecord(
            id = "IC-2026-001",
            tanggal = "2026-09-08",
            entitySumber = "PT Gema Abadi Nugraha",
            entityTujuan = "PT Setia Surya Nugraha",
            projectId = "PRJ-002",
            voucherSumberTxId = "TX-20260908-IC01",
            voucherTujuanTxId = "TX-20260908-IC02",
            nominal = 150_000_000L,
            settlementTotal = 50_000_000L,
            status = "PARTIAL",
            keterangan = "Talangan dana operasional pembukaan site Green Surya Residence"
        )
    )

    fun getInitialPettyCash(): List<PettyCashAdvanceRecord> = listOf(
        PettyCashAdvanceRecord(
            advanceId = "ADV-2026-001",
            tanggal = "2026-09-18",
            penerimaNama = "Ahmad Fauzi, S.E.",
            projectId = "PRJ-001",
            jumlahAdvance = 10_000_000L,
            plafonUser = 15_000_000L,
            terpakaiTerverifikasi = 6_500_000L,
            dikembalikan = 0L,
            jatuhTempo = "2026-10-05",
            voucherTxId = "TX-20260918-ADV1",
            statusSettlement = "OPEN",
            keterangan = "Uang Muka Operasional Kas Lapangan & Material Darurat"
        )
    )

    fun getInitialInvestors(): List<MitraInvestorRecord> = listOf(
        MitraInvestorRecord(
            id = "INV-001",
            namaInvestor = "Ir. H. Rahmat Hidayat (Konsorsium Priangan)",
            projectId = "PRJ-001",
            modalDisetor = 2_500_000_000L,
            persentaseBagiHasil = 25.0,
            totalDividenDiterima = 250_000_000L,
            status = "AKTIF",
            telepon = "08119922881",
            rekeningBank = "BCA 8820019283",
            tanggalBergabung = "2026-01-10",
            keterangan = "Penyertaan modal kerja proyek Gunung Padang Tahap 1"
        ),
        MitraInvestorRecord(
            id = "INV-002",
            namaInvestor = "Hj. Endah Sulistyowati",
            projectId = "PRJ-001",
            modalDisetor = 1_000_000_000L,
            persentaseBagiHasil = 10.0,
            totalDividenDiterima = 100_000_000L,
            status = "AKTIF",
            telepon = "081234567890",
            rekeningBank = "Mandiri 1320098127",
            tanggalBergabung = "2026-03-01",
            keterangan = "Investor modal usaha infrastruktur & kavling premium"
        )
    )
}
