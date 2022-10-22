package com.example.cmd.model

import android.content.Context
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

//Почти весь функционал, требующий контекста
class PreferencesModel(private val context: Context) {
  val pref by lazy {
    context.getSharedPreferences(
      "preferences",
      AppCompatActivity.MODE_PRIVATE
    )
  }
  private val masterKeyAlias by lazy {
    MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build();
  }
  private val passwordsEncrypted by lazy {
    EncryptedSharedPreferences.create(
      context,
      "passwordPrefs",
      masterKeyAlias,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }

  private val edit by lazy { pref.edit() }
  private val privEdit by lazy { passwordsEncrypted.edit() }

  private val iv by lazy { IvParameterSpec(getPassword("init").toByteArray()) }

  private var day = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
  val keySpec by lazy { SecretKeySpec(getPassword("ironKey").toByteArray(), "AES") }


  //Функции получения/изменения данных из Shared Preferences
  fun getBoolean(name: String): Boolean = pref.getBoolean(name, false)


  fun getPassword(pass: String): String = passwordsEncrypted.getString(pass, "")!!


  fun putBoolean(name: String, flag: Boolean) {
    edit.putBoolean(name, flag)
    edit.apply()
  }

  fun getLong(name: String): Long = pref.getLong(name, 2)

  fun putCurrentTime(name: String) {
    edit.putLong(name, System.currentTimeMillis())
    edit.apply()
  }

  fun putLong(name: String, long: Long) {
    edit.putLong(name, long)
    edit.apply()
  }


  fun setPassword(pass: String, new: String?) {
    privEdit.putString(pass, new)
    privEdit.apply()
  }

  fun getString(name: String): String = pref.getString(name, "")!!

  fun putString(name: String, new: String) {
    edit.putString(name, new)
    edit.apply()
  }

  //получить директорию с пользовательскими файлами программы
  fun getFilesDir() = context.filesDir.path

  //Работа с логами
  private fun encodeString(input: String): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
    val crypted = cipher.doFinal(input.toByteArray())
    val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
    return String(encodedByte)
  }

  fun decodeString(input: String): String {
    val decodedByte: ByteArray = Base64.decode(input, Base64.DEFAULT)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
    val output = cipher.doFinal(decodedByte)
    return String(output)
  }

  fun clearLogs() {
    File("${context.filesDir.path}/Log").listFiles()?.forEach {
      if (LocalDate.parse(it.name).toEpochDays() < java.time.LocalDate.now().toEpochDay() - getLong(
          "logs_autoremove"
        )
      )
        it.delete()
    }
  }

  fun writeLog(ciphered: Boolean, id: Int, vararg format: Any) {
    val current = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    if (current != day) {
      day = current
      File("${context.filesDir.path}/Log/$day").createNewFile()
    }
    var message = context.getString(id, *format)
    if (ciphered) {
      message = encodeString(message) + " C" //шифрованные логи
    } else
      message += " E" //не шифрованные логи
    FileOutputStream(File("${context.filesDir.path}/Log/$day"), true).bufferedWriter().use {
      it.write(
        "${
          java.time.LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
          )
        } $message\n"
      )
    }
  }

}
