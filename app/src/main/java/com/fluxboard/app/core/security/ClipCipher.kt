package com.fluxboard.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cifra el contenido de los recortes antes de abandonar el dispositivo.
 *
 * Usa AES-256/GCM con una clave no exportable custodiada por el AndroidKeyStore.
 * El texto en claro vive ÚNICAMENTE en Room (Offline-First); lo que viaja a
 * Cloudflare KV es siempre el resultado de [encrypt].
 *
 * Regla de privacidad: ninguna función de esta clase registra en logs la
 * entrada ni la salida.
 */
@Singleton
class ClipCipher @Inject constructor() {

    /**
     * Cifra [plainText] y devuelve `Base64(IV || ciphertext || tag)`, listo para
     * transportar como cadena.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(iv.size + cipherText.size).also {
            iv.copyInto(it, 0)
            cipherText.copyInto(it, iv.size)
        }
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    /** Descifra una carga generada por [encrypt]. */
    fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = payload.copyOfRange(0, IV_LENGTH)
        val cipherText = payload.copyOfRange(IV_LENGTH, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    /** Hash SHA-256 (hex) del texto, usado como `content_hash` de la metadata. */
    fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    /** Tamaño en bytes UTF-8 del texto, usado como `size_bytes` de la metadata. */
    fun sizeBytes(value: String): Int = value.toByteArray(Charsets.UTF_8).size

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { entry ->
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "fluxboard_clip_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
        const val KEY_SIZE_BITS = 256
    }
}
