package com.example.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudProjectDto(
    val id: String,
    val name: String,
    @SerialName("entity_name") val entityName: String,
    @SerialName("unit_count") val unitCount: Int,
    @SerialName("total_budget") val totalBudget: Long,
    val location: String,
    val status: String
)

@Serializable
data class CloudCostCodeDto(
    val code: String,
    val name: String,
    val category: String,
    @SerialName("coa_default") val coaDefault: String? = null,
    val active: Boolean
)

@Serializable
data class CloudBudgetDto(
    val id: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("cost_code") val costCode: String,
    @SerialName("budget_initial") val budgetInitial: Long,
    @SerialName("revision_approved") val revisionApproved: Long,
    val commitment: Long,
    @SerialName("actual_posted") val actualPosted: Long,
    val notes: String
)

class CloudMasterRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun fetchProjects(): List<MasterProyek> =
        supabase.from("projects")
            .select()
            .decodeList<CloudProjectDto>()
            .map {
                MasterProyek(
                    id = it.id,
                    nama = it.name,
                    entityId = it.entityName,
                    unitCount = it.unitCount,
                    totalBudget = it.totalBudget,
                    lokasi = it.location,
                    status = when (it.status) {
                        "RUNNING" -> "BERJALAN"
                        "COMPLETED" -> "SELESAI"
                        "CANCELLED" -> "DIBATALKAN"
                        else -> "PERENCANAAN"
                    }
                )
            }

    suspend fun fetchCostCodes(): List<MasterCostCode> =
        supabase.from("cost_codes")
            .select()
            .decodeList<CloudCostCodeDto>()
            .map {
                MasterCostCode(
                    kode = it.code,
                    nama = it.name,
                    kategori = it.category,
                    akunCoaDefault = it.coaDefault ?: "",
                    aktif = it.active
                )
            }

    suspend fun fetchBudgets(): List<AnggaranProyek> =
        supabase.from("budgets")
            .select()
            .decodeList<CloudBudgetDto>()
            .map {
                AnggaranProyek(
                    id = it.id,
                    projectId = it.projectId,
                    costCode = it.costCode,
                    costCodeNama = "",
                    budgetAwal = it.budgetInitial,
                    revisiApproved = it.revisionApproved,
                    commitment = it.commitment,
                    actualPosted = it.actualPosted,
                    notes = it.notes
                )
            }
}
