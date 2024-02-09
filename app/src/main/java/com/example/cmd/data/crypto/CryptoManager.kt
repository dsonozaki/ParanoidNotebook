package com.example.cmd.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
/**
 * Класс, отвечающий за шифрование и дешифрование данных.
**/
class CryptoManager @Inject constructor() {
  private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
    load(null)
  }

  private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
    init(Cipher.ENCRYPT_MODE, getKey())
  }

  private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
    return Cipher.getInstance(TRANSFORMATION).apply {
      init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
    }
  }

  private fun getKey(): SecretKey {
    val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
    return existingKey?.secretKey ?: createKey()
  }

  private fun createKey(): SecretKey {
    return KeyGenerator.getInstance(ALGORITHM).apply {
      init(
        KeyGenParameterSpec.Builder(
          "secret",
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
          .setBlockModes(BLOCK_MODE)
          .setEncryptionPaddings(PADDING)
          .setUserAuthenticationRequired(false)
          .setRandomizedEncryptionRequired(true)
          .build()
      )
    }.generateKey()
  }

  suspend fun encryptToFile(bytes: ByteArray, outputStream: OutputStream): ByteArray {
    val cypher = encryptCipher
    val encryptedBytes = cypher.doFinal(bytes)
    withContext(Dispatchers.IO) {
      outputStream.use {
        it.write(cypher.iv.size)
        it.write(cypher.iv)
        it.write(encryptedBytes.size)
        it.write(encryptedBytes)
      }
    }
    return encryptedBytes
  }

  fun decryptFromFile(inputStream: InputStream): ByteArray {
    return inputStream.use {
      val ivSize = it.read()
      val iv = ByteArray(ivSize)
      it.read(iv)
      val encryptedBytesSize = it.read()
      val encryptedBytes = ByteArray(encryptedBytesSize)
      it.read(encryptedBytes)
      getDecryptCipherForIv(iv).doFinal(encryptedBytes)
    }
  }

  fun encryptString(input: String): String = buildString {
    val bytes = input.toByteArray()
    val cypher = encryptCipher
    val encryptedBytes = cypher.doFinal(bytes)
    if (Build.VERSION.SDK_INT >= 26) {
    val encoder = Base64.getEncoder()
    append(encoder.encodeToString(cypher.iv))
    append(DELIMITER)
    append(encoder.encodeToString(encryptedBytes))
    } else {
      append(android.util.Base64.encodeToString(cypher.iv, 0))
      append(DELIMITER)
      append(android.util.Base64.encodeToString(encryptedBytes, 0))
    }
  }

  fun decryptString(input: String): String = buildString {
    val (ivEncoded, passwordEncoded) = input.split(DELIMITER)
    if (Build.VERSION.SDK_INT >= 26) {
      val decoder = Base64.getDecoder()
      val iv = decoder.decode(ivEncoded)
      val passwordCiphered = decoder.decode(passwordEncoded)
      val decryptedBytes = getDecryptCipherForIv(iv).doFinal(passwordCiphered)
      append(decryptedBytes.decodeToString())
    } else {
      val iv = android.util.Base64.decode(ivEncoded,0)
      val passwordCiphered = android.util.Base64.decode(passwordEncoded,0)
      val decryptedBytes = getDecryptCipherForIv(iv).doFinal(passwordCiphered)
      append(decryptedBytes.decodeToString())
    }
  }


  companion object {
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    private const val DELIMITER = "|"
  }


}
