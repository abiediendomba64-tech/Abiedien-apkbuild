# Excel / Google Sheets → Supabase

## Prinsip
Supabase PostgreSQL adalah source of truth untuk transaksi, budget, project, cost code, dan audit.
Excel / Google Sheets berfungsi sebagai:
- staging / import data
- template input
- rekonsiliasi
- reporting / export

Jangan memakai spreadsheet sebagai ledger authoritative.

## Import transaksi
1. Excel: simpan sebagai CSV UTF-8.
2. Google Sheets: File → Download → Comma-separated values (.csv).
3. Tempel CSV ke layar Import Transaksi di APK.
4. APK parse dan validasi setiap baris.
5. Setiap transaksi dikirim ke RPC `post_transaction`.
6. Server menetapkan `auth.uid()`, status `PENDING_APPROVAL`, validasi project/cost code/nominal/dokumen, dan membuat audit log.
7. Setelah import selesai APK refresh dari Supabase.

Dengan demikian import spreadsheet tidak dapat mengubah ledger lokal secara authoritative.

## Master data
Untuk tahap bootstrap, master project / cost code / budget dapat disiapkan dari Excel/Google Sheets lalu diekspor CSV dan dimuat ke tabel Supabase melalui jalur admin yang terkontrol.
Jangan memasukkan data seed/demo ke production.

## Format minimum
### projects
id,name,entity_name,unit_count,total_budget,location,status

### cost_codes
code,name,category,coa_default,active

### budgets
id,project_id,cost_code,budget_initial,revision_approved,commitment,actual_posted,notes

### transactions
id,tanggal,tipe,rekening_bank,project_id,cost_code,akun_efektif,pihak_id,pihak_nama,nominal,keterangan,doc_ref

## Aturan
- Tidak ada password atau service_role key di spreadsheet.
- Jangan menghapus/mengubah POSTED transaction dari spreadsheet.
- Koreksi POSTED harus memakai reversal RPC.
- Import hanya menerima data nyata yang diverifikasi.
