/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.network.documents

import com.digital.sofia.data.BuildConfig.URL_DOCUMENT_AUTHENTICATION
import com.digital.sofia.data.BuildConfig.URL_GET_DOCUMENTS
import com.digital.sofia.data.BuildConfig.URL_REQUEST_IDENTITY
import com.digital.sofia.data.BuildConfig.URL_SEND_SIGNED_DOCUMENT
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
    @GET
    suspend fun downloadFile(
        @Url documentUrl: String
    ): Response<ResponseBody>

    @GET(URL_SEND_SIGNED_DOCUMENT)
    suspend fun sendOpenedDocument(
        @Path("evrotrustTransactionId") evrotrustTransactionId: String,
    ): Response<DocumentStatusResponse>


    @GET(URL_REQUEST_IDENTITY)
    suspend fun requestIdentity(
        @Path("personalIdentificationNumber") personalIdentificationNumber: String,
    ): Response<DocumentResponse>

    @POST(URL_DOCUMENT_AUTHENTICATION)
    suspend fun authenticateDocument(
        @Path("evrotrustTransactionId") evrotrustTransactionId: String,
        @Body requestBody: DocumentAuthenticationRequestBody
    ): Response<EmptyResponse>
}
