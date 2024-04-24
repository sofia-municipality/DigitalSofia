/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.domain.repository.common

import java.io.InputStream
import java.security.KeyPair
import java.security.PrivateKey
import javax.crypto.Cipher
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

interface CryptographyRepository {

    fun encrypt(text: String, cipher: Cipher? = null): String

    fun decrypt(text: String, cipher: Cipher? = null): String

    fun getOrCreateSharedPreferencesKey(): String

    fun createDatabaseKey(): String

    fun getBiometricCipherForEncryption(): Cipher?

    fun getBiometricCipherForDecryption(initializationVector: ByteArray): Cipher?

    fun getInitializationVectorFromString(text: String): ByteArray

    /**
     * Generates a pair of public and private keys with unique [alias].
     * This keys could be used for the exchange and then you can decrypt the
     * content with private key in [decryptWithPrivateKey].
     */
    fun generatePublicPrivateKeyPair(alias: String): KeyPair

    fun decryptWithPrivateKey(privateKey: PrivateKey, text: String): String

    /**
     * Encrypt the [text] with [publicKey] from server and returns
     * the Base64 string to send to server. Usually, uses the same algorithms as
     * [decryptWithPrivateKey] and [generatePublicPrivateKeyPair].
     */
    fun encryptWithPublicServerKey(publicKey: String, text: String): String

    /**
     * Generate SSL context form incoming certificate. Certificate should be in .p12 format.
     * Note that the input stream will be closed after ssl context generation.
     */
    fun generateSslContextAndTrustManager(
        certificate: InputStream,
        password: String
    ): Pair<SSLContext?, X509TrustManager?>

}