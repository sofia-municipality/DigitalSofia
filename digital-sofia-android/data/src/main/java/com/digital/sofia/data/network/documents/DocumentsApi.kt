/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.documents

import com.digital.sofia.data.BuildConfig.URL_CHECK_DELIVERED_DOCUMENT
import com.digital.sofia.data.BuildConfig.URL_CHECK_SIGNED_DOCUMENT
import com.digital.sofia.data.BuildConfig.URL_DOCUMENT_AUTHENTICATION
import com.digital.sofia.data.BuildConfig.URL_DOWNLOAD_DOCUMENT
import com.digital.sofia.data.BuildConfig.URL_GET_DOCUMENTS
import com.digital.sofia.data.BuildConfig.URL_REQUEST_IDENTITY
import com.digital.sofia.data.models.network.base.EmptyResponse
import com.digital.sofia.data.models.network.documents.DocumentAuthenticationRequestBody
import com.digital.sofia.data.models.network.documents.DocumentResponse
import com.digital.sofia.data.models.network.documents.DocumentStatusResponse
import com.digital.sofia.data.models.network.documents.DocumentsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DocumentsApi {

    @GET(URL_GET_DOCUMENTS)
    suspend fun getDocument(
        @Query("cursor") cursor: String?,
        @Query("status") status: String?,
    ): Response<DocumentsResponse>

    @Streaming
    @GET(URL_DOWNLOAD_DOCUMENT)
    suspend fun downloadFile(
        @Path("documentFormIOId") documentFormIOId: String
    ): Response<ResponseBody>

    @GET(URL_CHECK_SIGNED_DOCUMENT)
    suspend fun checkSignedDocumentStatus(
        @Path("evrotrustTransactionId") evrotrustTransactionId: String,
    ): Response<DocumentStatusResponse>

    @GET(URL_CHECK_DELIVERED_DOCUMENT)
    suspend fun checkDeliveredDocumentStatus(
        @Path("evrotrustThreadId") evrotrustThreadId: String,
    ): Response<DocumentStatusResponse>


    @GET(URL_REQUEST_IDENTITY)
    suspend fun requestIdentity(
        @Path("personalIdentificationNumber") personalIdentificationNumber: String,
        @Query("lang") language: String,
    ): Response<DocumentResponse>

    @POST(URL_DOCUMENT_AUTHENTICATION)
    suspend fun authenticateDocument(
        @Path("evrotrustTransactionId") evrotrustTransactionId: String,
        @Body requestBody: DocumentAuthenticationRequestBody
    ): Response<EmptyResponse>
}
