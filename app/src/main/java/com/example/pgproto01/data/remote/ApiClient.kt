package com.example.pgproto01.data.remote

import com.example.pgproto01.data.remote.model.AttendanceLogRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun postAttendanceLog(
        apiUrl: String,
        log: AttendanceLogRequest
    ): Boolean {
        val body = json.encodeToString(log)
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
