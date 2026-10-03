package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.AppDao
import com.example.data.PaymentProofRecord
import com.example.data.ProofType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

interface ImageUploadRepository {
    suspend fun saveImageLocally(
        bitmap: Bitmap,
        transactionId: String,
        proofType: ProofType,
        uploadedBy: String = "Operator",
        notes: String = ""
    ): Result<PaymentProofRecord>

    fun getProofsForTransaction(transactionId: String): Flow<List<PaymentProofRecord>>
    fun getAllProofs(): Flow<List<PaymentProofRecord>>
    suspend fun deleteProof(proofId: String): Result<Boolean>
    suspend fun loadBitmapFromPath(filePath: String): Bitmap?
}

class ImageUploadManager(
    private val context: Context,
    private val dao: AppDao
) : ImageUploadRepository {

    private val storageDir: File by lazy {
        File(context.filesDir, "payment_proofs").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveImageLocally(
        bitmap: Bitmap,
        transactionId: String,
        proofType: ProofType,
        uploadedBy: String,
        notes: String
    ): Result<PaymentProofRecord> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val typePrefix = when (proofType) {
                ProofType.RECEIPT -> "RCT"
                ProofType.SIGNATURE -> "SIG"
                ProofType.INVOICE -> "INV"
                ProofType.SPK -> "SPK"
                ProofType.BAST -> "BST"
            }
            val fileName = "${typePrefix}_${transactionId}_${timestamp}.jpg"
            val targetFile = File(storageDir, fileName)

            val digest = MessageDigest.getInstance("SHA-256")
            val outputStream = FileOutputStream(targetFile)

            // Compress and write
            val format = if (proofType == ProofType.SIGNATURE) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val quality = if (proofType == ProofType.SIGNATURE) 100 else 85
            bitmap.compress(format, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            // Compute SHA-256
            val fileBytes = targetFile.readBytes()
            val hashBytes = digest.digest(fileBytes)
            val sha256 = hashBytes.joinToString("") { "%02x".format(it) }

            val proofRecord = PaymentProofRecord(
                id = "PRF-$timestamp",
                transactionId = transactionId,
                proofType = proofType.name,
                fileName = fileName,
                filePath = targetFile.absolutePath,
                fileUri = Uri.fromFile(targetFile).toString(),
                fileSizeBytes = targetFile.length(),
                mimeType = if (proofType == ProofType.SIGNATURE) "image/png" else "image/jpeg",
                sha256Checksum = sha256,
                capturedAt = timestamp,
                uploadedBy = uploadedBy,
                notes = notes
            )

            dao.insertProof(proofRecord)
            Result.success(proofRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getProofsForTransaction(transactionId: String): Flow<List<PaymentProofRecord>> {
        return dao.getProofsForTransaction(transactionId)
    }

    override fun getAllProofs(): Flow<List<PaymentProofRecord>> {
        return dao.getAllPaymentProofs()
    }

    override suspend fun deleteProof(proofId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val record = dao.getProofById(proofId) ?: return@withContext Result.failure(Exception("Proof not found"))
            val file = File(record.filePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deleteProofById(proofId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadBitmapFromPath(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
