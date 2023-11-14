package com.digitall.digital_sofia.data.network.documents

import com.digitall.digital_sofia.data.URL_GET_DOCUMENTS
import com.digitall.digital_sofia.data.URL_SEND_SIGNED_DOCUMENT
import com.digitall.digital_sofia.data.models.network.authorization.AuthorizationResponse
import com.digitall.digital_sofia.data.models.network.documents.DocumentsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

interface DocumentsApi {

    @GET(URL_GET_DOCUMENTS)
    suspend fun getDocument(): Response<DocumentsResponse>

    @GET(URL_SEND_SIGNED_DOCUMENT)
    suspend fun sendSignedDocument(
        @Path("evrotrustTransactionId") evrotrustTransactionId: String,
        @Query("refreshToken") refreshToken: String,
    ): Response<AuthorizationResponse>

}
