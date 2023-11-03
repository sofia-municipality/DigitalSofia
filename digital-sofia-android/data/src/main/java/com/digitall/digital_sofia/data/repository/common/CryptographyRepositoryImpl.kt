package com.digitall.digital_sofia.data.repository.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.MasterKey
import com.digitall.digital_sofia.data.ANDROID_KEYSTORE
import com.digitall.digital_sofia.data.MASTER_BIOMETRIC_KEY_ALIAS
import com.digitall.digital_sofia.data.MASTER_PREFERENCES_KEY_ALIAS
import com.digitall.digital_sofia.data.MASTER_SYMMETRIC_KEY_ALIAS
import com.digitall.digital_sofia.domain.repository.common.CryptographyRepository
import com.digitall.digital_sofia.domain.utils.LogUtil.logError
import okhttp3.internal.platform.Platform
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/

class CryptographyRepositoryImpl(
    private val context: Context
) : CryptographyRepository {

    companion object {
        private const val TAG = "CryptographyRepositoryImplTag"

        // Symmetric encryption
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val SYMMETRIC_KEY_SIZE = 256

        // Asymmetric encryption
        private const val AE_ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
        private const val AE_ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
        private const val AE_ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
        private const val ASYMMETRIC_KEY_SIZE = 2048
        private const val IV_SEPARATOR = "###"
    }

    private lateinit var keystore: KeyStore

    init {
        initializeKeystore()
    }

    private fun initializeKeystore() {
        keystore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keystore.load(null)
    }

    override fun encrypt(text: String, cipher: Cipher?): String {
        val result = StringBuilder()
        val encryptionCipher = cipher ?: getInitializedCipherForEncryption()
        // Add IV
        val ivString = Base64.encodeToString(encryptionCipher.iv, Base64.NO_WRAP)
        result.append(ivString + IV_SEPARATOR)
        // Encrypt
        val ciphertext = encryptionCipher.doFinal(text.toByteArray())
        val encodedString = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        result.append(encodedString)
        return result.toString()
    }

    override fun decrypt(text: String, cipher: Cipher?): String {
        // Get IV from text
        val split = text.split(IV_SEPARATOR)
        val ivString = split[0]
        val encodedString = split[1]
        val initializationVector = Base64.decode(ivString, Base64.NO_WRAP)
        val decryptionCipher = cipher ?: getInitializedCipherForDecryption(initializationVector)
        // Decrypt
        val decodedString = Base64.decode(encodedString, Base64.NO_WRAP)
        val result = decryptionCipher.doFinal(decodedString)
        return String(result)
    }

    override fun getOrCreateSharedPreferencesKey(): String {
        // Create a shared preferences key
        MasterKey.Builder(context, MASTER_PREFERENCES_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(true)
            .build()
        return MASTER_PREFERENCES_KEY_ALIAS
    }

    override fun createDatabaseKey(): String {
        return UUID.randomUUID().toString()
    }

    override fun getBiometricCipherForEncryption(): Cipher? {
        return try {
            val cipher = getSymmetricCipher()
            val secretKey = getOrCreateBiometricSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            cipher
        } catch (e: Exception) {
            logError("getBiometricCipherForEncryption Exception: ${e.message}", e, TAG)
            null
        }
    }

    override fun getBiometricCipherForDecryption(initializationVector: ByteArray): Cipher? {
        return try {
            val cipher = getSymmetricCipher()
            val secretKey = getOrCreateBiometricSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
            cipher
        } catch (e: Exception) {
            logError("getBiometricCipherForDecryption Exception: ${e.message}", e, TAG)
            null
        }
    }

    override fun getInitializationVectorFromString(text: String): ByteArray {
        val split = text.split(IV_SEPARATOR)
        val ivString = split[0]
        return Base64.decode(ivString, Base64.NO_WRAP)
    }

    override fun generatePublicPrivateKeyPair(alias: String): KeyPair {
        val generator = KeyPairGenerator.getInstance(AE_ENCRYPTION_ALGORITHM, ANDROID_KEYSTORE)
        val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val builder = KeyGenParameterSpec.Builder(alias, purpose)
            .setBlockModes(AE_ENCRYPTION_BLOCK_MODE)
            .setEncryptionPaddings(AE_ENCRYPTION_PADDING)
            .setKeySize(ASYMMETRIC_KEY_SIZE)
        generator.initialize(builder.build())
        return generator.generateKeyPair()
    }

    override fun decryptWithPrivateKey(privateKey: PrivateKey, text: String): String {
        val cipher = Cipher.getInstance(privateKey.algorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        // Decrypt
        val decodedString = Base64.decode(text, Base64.NO_WRAP)
        val result = cipher.doFinal(decodedString)
        return String(result)
    }

    override fun encryptWithPublicServerKey(publicKey: String, text: String): String {
        val normPublicKey = normalizePublicKeyWhenNeeded(publicKey)
        val publicBytes = Base64.decode(normPublicKey, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance(AE_ENCRYPTION_ALGORITHM)
        val finalPublicKey = keyFactory.generatePublic(keySpec)
        val transformation =
            "$AE_ENCRYPTION_ALGORITHM/$AE_ENCRYPTION_BLOCK_MODE/$AE_ENCRYPTION_PADDING"
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, finalPublicKey)
        // Encrypt
        val ciphertext = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun normalizePublicKeyWhenNeeded(publicKey: String): String {
        return publicKey.replace("\\r".toRegex(), "")
            .replace("\\n".toRegex(), "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
    }

    override fun generateSslContextAndTrustManager(
        certificate: InputStream,
        password: String
    ): Pair<SSLContext?, X509TrustManager?> {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(certificate, password.toCharArray())
        val kmf: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        kmf.init(keyStore, UUID.randomUUID().toString().toCharArray())
        val trustManager = Platform.get().platformTrustManager()
        val sslContext = Platform.get().newSSLContext()
        sslContext.init(kmf.keyManagers, arrayOf(trustManager), null)
        certificate.close()

        return sslContext to trustManager
    }

    private fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getSymmetricCipher()
        val secretKey = getOrCreateMasterSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    private fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = getSymmetricCipher()
        val secretKey = getOrCreateMasterSecretKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    private fun getSymmetricCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateMasterSecretKey(): SecretKey {
        keystore.getKey(MASTER_SYMMETRIC_KEY_ALIAS, null)?.let {
            return it as SecretKey
        }
        // if you reach here, then a new SecretKey must be generated for that keyName
        val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val paramsBuilder = KeyGenParameterSpec.Builder(MASTER_SYMMETRIC_KEY_ALIAS, purpose).apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(SYMMETRIC_KEY_SIZE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                    setIsStrongBoxBacked(true)
                }
            }
        }
        val keyGenParams = paramsBuilder.build()
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    private fun getOrCreateBiometricSecretKey(): SecretKey {
        keystore.getKey(MASTER_BIOMETRIC_KEY_ALIAS, null)?.let {
            return it as SecretKey
        }
        // if you reach here, then a new SecretKey must be generated for that keyName
        val purpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val paramsBuilder = KeyGenParameterSpec.Builder(MASTER_BIOMETRIC_KEY_ALIAS, purpose).apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(SYMMETRIC_KEY_SIZE)
            setUserAuthenticationRequired(true)
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or
                            KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else {
                // Deprecated but do not have any alternative for lower APIs as usual.
                setUserAuthenticationValidityDurationSeconds(-1)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setUnlockedDeviceRequired(true)
                if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                    setIsStrongBoxBacked(true)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setInvalidatedByBiometricEnrollment(true)
            }
        }
        val keyGenParams = paramsBuilder.build()
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }
}