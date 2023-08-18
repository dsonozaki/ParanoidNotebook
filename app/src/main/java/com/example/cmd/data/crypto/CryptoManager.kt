package com.example.cmd.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

//Класс, отвечающий за шифрование и дешифрование данных. Сделать синглтоном
class CryptoManager @Inject constructor() {
  private val keystore = KeyStore.getInstance("AndroidKeyStore").apply {
    load(null)
  }

  private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
    init(Cipher.ENCRYPT_MODE, getKey())
  }

  private fun getKey(): SecretKey {
    val existingKey = keystore.getEntry(ALIAS,null) as? KeyStore.SecretKeyEntry
    return existingKey?.secretKey ?: createKey()
  }

  private fun createKey() : SecretKey {
    return KeyGenerator.getInstance(ALGORITHM).apply {
      init(KeyGenParameterSpec.Builder(ALIAS, PURPOSES).setBlockModes(BLOCK_MODE)
        .setEncryptionPaddings(PADDING)
        .setRandomizedEncryptionRequired(true)
        .setUserAuthenticationRequired(false)
        .build())
    }.generateKey()
  }

  private fun decryptCipherForIv(iv: ByteArray): Cipher {
    return Cipher.getInstance(TRANSFORMATION).apply {
      init(Cipher.DECRYPT_MODE,getKey(), IvParameterSpec(iv))
    }
  }

  fun encryptToFile(bytes: ByteArray, outputStream: OutputStream): ByteArray {
    val encryptedBytes = encryptCipher.doFinal(bytes)
    outputStream.use {
      it.write(encryptCipher.iv.size)
      it.write(encryptCipher.iv)
      it.write(encryptedBytes.size)
      it.write(encryptedBytes)
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
      decryptCipherForIv(iv).doFinal(encryptedBytes)
    }
  }

  fun encryptString(input: String): String = buildString {
    val bytes = input.toByteArray()
    val encryptedBytes = encryptCipher.doFinal(bytes)
    val encoder = Base64.getEncoder()
    append(encoder.encodeToString(encryptCipher.iv))
    append(DELIMITER)
    append(encoder.encodeToString(encryptedBytes))
  }

  fun decryptString(input: String): String = buildString {
    val (ivEncoded, passwordEncoded) = input.split(DELIMITER)
    val decoder = Base64.getDecoder()
    val iv = decoder.decode(ivEncoded)
    val passwordCiphered = decoder.decode(passwordEncoded)
    append(decryptCipherForIv(iv).doFinal(passwordCiphered))
  }





  companion object {
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val PURPOSES = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    private const val ALIAS = "NotepadKey"
    private const val DELIMITER = "|"
  }


}
