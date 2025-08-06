/*
 * TORUS VPN - Custom VPN Client
 * Copyright (c) 2025 TheTorusProject
 * 
 * This file is part of TORUS VPN, based on WG Tunnel by Zane Schepke.
 * Original work Copyright (c) 2023-2025 Zane Schepke
 * Licensed under the MIT License.
 */

package com.zaneschepke.wireguardautotunnel.data.network

import android.content.Context
import android.util.Log
import com.zaneschepke.wireguardautotunnel.data.entity.ApiHealth
import com.zaneschepke.wireguardautotunnel.data.entity.SupportRequest
import com.zaneschepke.wireguardautotunnel.data.entity.SupportResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class KtorUserSupportApi @Inject constructor(
    private val client: HttpClient,
    @param:ApplicationContext private val context: Context
) : UserSupportApi {
    
    companion object {
        private const val TAG = "UserSupportApi"
        // TODO: Replace with actual support API base URL from configuration
        private const val BASE_URL = "https://support.your-domain.com"
    }
    
    override suspend fun sendSupportSignal(): Result<SupportResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i(TAG, "Sending anonymous support signal")
            
            // Send anonymous support signal (no email required)
            val response: SupportResponse = client.post("$BASE_URL/api/support") {
                contentType(ContentType.Application.Json)
                setBody(SupportRequest(email = "anonymous-signal@torus.local"))
            }.body()
            
            Log.i(TAG, "Support signal sent successfully")
            Result.success(response)
            
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Support signal failed with status: ${e.response.status}")
            when (e.response.status) {
                HttpStatusCode.TooManyRequests -> 
                    Result.failure(Exception("Rate limit exceeded. Please try again later."))
                HttpStatusCode.BadRequest -> 
                    Result.failure(Exception("Invalid request format"))
                else -> Result.failure(Exception("Server error: ${e.response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error sending support signal", e)
            Result.failure(Exception("Network error. Signal will be queued for retry."))
        }
    }
    
    override suspend fun submitEmailForUpdates(email: String): Result<SupportResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i(TAG, "Submitting email for updates: ${email.take(3)}***")
            
            val response: SupportResponse = client.post("$BASE_URL/api/support") {
                contentType(ContentType.Application.Json)
                setBody(SupportRequest(email = email))
            }.body()
            
            Log.i(TAG, "Email submitted for updates successfully")
            Result.success(response)
            
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Email submission failed with status: ${e.response.status}")
            when (e.response.status) {
                HttpStatusCode.TooManyRequests -> 
                    Result.failure(Exception("Rate limit exceeded. Please try again later."))
                HttpStatusCode.BadRequest -> 
                    Result.failure(Exception("Invalid email format"))
                else -> Result.failure(Exception("Server error: ${e.response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error submitting email", e)
            Result.failure(Exception("Network error. Please check your connection and try again."))
        }
    }
    
    override suspend fun submitNodeOperatorEmail(email: String): Result<SupportResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i(TAG, "Submitting node operator interest: ${email.take(3)}***")
            
            val response: SupportResponse = client.post("$BASE_URL/api/node-operator") {
                contentType(ContentType.Application.Json)
                setBody(SupportRequest(email = email))
            }.body()
            
            Log.i(TAG, "Node operator interest submitted successfully")
            Result.success(response)
            
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Node operator submission failed with status: ${e.response.status}")
            when (e.response.status) {
                HttpStatusCode.TooManyRequests -> 
                    Result.failure(Exception("Rate limit exceeded. Please try again later."))
                HttpStatusCode.BadRequest -> 
                    Result.failure(Exception("Invalid email format"))
                else -> Result.failure(Exception("Server error: ${e.response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error submitting node operator interest", e)
            Result.failure(Exception("Network error. Please check your connection and try again."))
        }
    }
    
    override suspend fun checkApiHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i(TAG, "Checking API health")
            
            val response: ApiHealth = client.get("$BASE_URL/api/health").body()
            val isHealthy = response.status == "healthy"
            
            Log.i(TAG, "API health check: ${if (isHealthy) "healthy" else "unhealthy"}")
            Result.success(isHealthy)
            
        } catch (e: ClientRequestException) {
            Log.w(TAG, "API health check failed with status: ${e.response.status}")
            Result.success(false) // API is down but not a critical failure
        } catch (e: Exception) {
            Log.w(TAG, "Network error checking API health", e)
            Result.success(false) // Assume API is down
        }
    }
}