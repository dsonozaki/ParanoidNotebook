package com.example.cmd.domain.entities

import android.net.Uri

data class MyFileDomain(val size: Long, val name: String, val priority: Int, val uri: Uri, val fileType: FileType, val sizeFormatted: String)
