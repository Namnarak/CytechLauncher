package com.movtery.zalithlauncher.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.movtery.zalithlauncher.info.InfoDistributor
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object CryptoManager {
    private const val KEY_SIZE = 256
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/CBC/PKCS7Padding"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        return if (keyStore.containsAlias(InfoDistributor.CRYPTO_KEY)) {
            (keyStore.getEntry(InfoDistributor.CRYPTO_KEY, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        InfoDistributor.CRYPTO_KEY,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setKeySize(KEY_SIZE)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }.generateKey()
        }
    }

    @JvmStatic
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(iv, Base64.NO_WRAP) + 
               ":" + 
               Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    @JvmStatic
    fun decrypt(encryptedData: String): String {
        val parts = encryptedData.split(":")
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
        
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}