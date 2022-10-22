package com.example.cmd.helpers

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.isTreeDocumentFile
import com.anggrayudi.storage.file.getAbsolutePath


class DirectoryFileHelper(private val context: Context) {
  //Преобразование URI в полный путь. Частично взято отсюда: https://stackoverflow.com/a/62180319/19544384
  fun getPath(uri: Uri, isDirectory: Boolean): String? {
    val df = DocumentFile.fromSingleUri(context,uri)
    Log.w("directory",isDirectory.toString())
    val path1 = if (isDirectory) {
      DocumentFile.fromTreeUri(context,uri)!!.getAbsolutePath(context)
    } else {
      df!!.getAbsolutePath(context)
    }
    if (path1.isNotEmpty()) return path1
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        return if ("primary".equals(type, ignoreCase = true)) {
          Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        } else { // non-primary volumes e.g sd card
          var filePath = "non"
          //getExternalMediaDirs() added in API 21
          val extenal = context.externalMediaDirs
          for (f in extenal) {
            filePath = f.absolutePath
            if (filePath.contains(type)) {
              val endIndex = filePath.indexOf("Android")
              filePath = filePath.substring(0, endIndex) + split[1]
            }
          }
          filePath
        }
      } else if (isDownloadsDocument(uri)) {
        val id = DocumentsContract.getDocumentId(uri)
        val contentUri = ContentUris.withAppendedId(
          Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
        return getDataColumn(context, contentUri, null, null)
      } else if (isMediaDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
        if ("image" == type) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if ("video" == type) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if ("audio" == type) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(
          split[1]
        )
        return getDataColumn(context, contentUri, selection, selectionArgs)
      }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
      return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
      return uri.path
    }
    return null
  }

  private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                            selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
      column
    )
    try {
      cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
        null)
      if (cursor != null && cursor.moveToFirst()) {
        val column_index = cursor.getColumnIndexOrThrow(column)
        return cursor.getString(column_index)
      }
    } catch (e: java.lang.Exception) {
    } finally {
      cursor?.close()
    }
    return null
  }

  private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
  }

  private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
  }

  private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
  }
  fun isDirectory(uri: Uri) = uri.isTreeDocumentFile

  fun toDocumentFile(uri: Uri, isDirectory: Boolean): DocumentFile = if (isDirectory) {
    DocumentFile.fromTreeUri(context,uri)!!
  } else {
    DocumentFile.fromSingleUri(context,uri)!!
  }
}
