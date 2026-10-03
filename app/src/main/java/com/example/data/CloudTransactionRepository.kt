package com.example.data

import com.example.data.TransaksiKasBankRecord
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudTransactionDto(
    val id: String,
    val tanggal: String,
    val tipe: String,
    @SerialName("rekening_bank") val rekeningBank: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("cost_code") val costCode: String? = null,
    @SerialName("akun_efektif") val akunEfektif: String,
    @SerialName("pihak_id") val pihakId: String? = null,
    @SerialName("pihak_nama") val pihakNama: String,
    val nominal: Long,
    val keterangan: String,
    @SerialName("doc_ref") val docRef: String? = null,
    @SerialName("dibuat_oleh") val dibuatOleh: String,
    @SerialName("approver_1") val approver1: String? = null,
    @SerialName("approver_2") val approver2: String? = null,
    @SerialName("status_input") val statusInput: String,
    @SerialName("status_sistem") val statusSistem: String,
    @SerialName("rejection_reason") val rejectionReason: String = "",
    @SerialName("reversal_of_id") val reversalOfId: String? = null
)

@Serializable
data class PostTransactionParams(
    @SerialName("p_id") val id: String,
    @SerialName("p_tipe") val tipe: String,
    @SerialName("p_rekening_bank") val rekeningBank: String,
    @SerialName("p_project_id") val projectId: String,
    @SerialName("p_cost_code") val costCode: String,
    @SerialName("p_akun_efektif") val akunEfektif: String,
    @SerialName("p_pihak_id") val pihakId: String,
    @SerialName("p_pihak_nama") val pihakNama: String,
    @SerialName("p_nominal") val nominal: Long,
    @SerialName("p_keterangan") val keterangan: String,
    @SerialName("p_doc_ref") val docRef: String?
)

class CloudTransactionRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun fetchTransactions(): List<TransaksiKasBankRecord> {
        return supabase
            .from("transactions")
            .select()
            .decodeList<CloudTransactionDto>()
            .map(::toEntity)
    }

    suspend fun postTransaction(
        tx: TransaksiKasBankRecord
    ): TransaksiKasBankRecord {
        val dto = supabase.postgrest.rpc(
            "post_transaction",
            PostTransactionParams(
                id = tx.id,
                tipe = tx.tipe,
                rekeningBank = tx.rekeningBank,
                projectId = tx.projectId,
                costCode = tx.costCode,
                akunEfektif = tx.akunEfektif,
                pihakId = tx.pihakId,
                pihakNama = tx.pihakNama,
                nominal = tx.nominal,
                keterangan = tx.keterangan,
                docRef = tx.docRef.ifBlank { null }
            )
        ).decodeSingle<CloudTransactionDto>()

        return toEntity(dto)
    }

    suspend fun approveTransaction(transactionId: String): TransaksiKasBankRecord {
        val dto = supabase.postgrest.rpc(
            "approve_transaction",
            kotlinx.serialization.json.buildJsonObject {
                put("p_transaction_id", transactionId)
            }
        ).decodeSingle<CloudTransactionDto>()

        return toEntity(dto)
    }

    suspend fun reverseTransaction(
        transactionId: String,
        reason: String
    ): TransaksiKasBankRecord {
        val dto = supabase.postgrest.rpc(
            "reverse_transaction",
            kotlinx.serialization.json.buildJsonObject {
                put("p_transaction_id", transactionId)
                put("p_reason", reason)
            }
        ).decodeSingle<CloudTransactionDto>()

        return toEntity(dto)
    }

    private fun toEntity(dto: CloudTransactionDto): TransaksiKasBankRecord {
        return TransaksiKasBankRecord(
            id = dto.id,
            tanggal = dto.tanggal,
            tipe = dto.tipe,
            rekeningBank = dto.rekeningBank,
            projectId = dto.projectId,
            costCode = dto.costCode ?: "",
            akunEfektif = dto.akunEfektif,
            pihakId = dto.pihakId ?: "",
            pihakNama = dto.pihakNama,
            nominal = dto.nominal,
            keterangan = dto.keterangan,
            docRef = dto.docRef ?: "",
            dibuatOleh = dto.dibuatOleh,
            disetujuiOleh1 = dto.approver1 ?: "",
            disetujuiOleh2 = dto.approver2 ?: "",
            statusInput = dto.statusInput,
            statusSistem = dto.statusSistem,
            rejectionReason = dto.rejectionReason,
            reversalOfId = dto.reversalOfId ?: "",
            candidateActual = if (dto.statusSistem == "POSTED" && dto.tipe == "KELUAR") dto.nominal else 0L,
            isPosted = dto.statusSistem == "POSTED"
        )
    }
}
