package com.example.cmd.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cmd.data.crypto.CryptoManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {
private lateinit var cryptoManager: CryptoManager
private val symbols = ('a'..'z') + ('а'..'я') + ('A'..'Z') + ('A'..'Я') + ('0'..'9')

  private fun generateRandomString(length: Int) =
    StringBuilder().apply {
      repeat(length) {
        this.append(symbols.random())
      }
    }.toString()


  @Before
  fun createCryptoManager() {
    cryptoManager=CryptoManager()
  }

  @Test
  fun decryptStringAfterEncryptStringRecoverOriginalString() {
    for (i in 10..100) {
      val string = generateRandomString(i)
      val encrypted = cryptoManager.encryptString(string)
      val decrypted = cryptoManager.decryptString(encrypted)
      Assert.assertEquals(string,decrypted)
    }
  }

  @Test
  fun decryptFileAfterEncryptFileRecoverOriginalFile() {

  }
}
