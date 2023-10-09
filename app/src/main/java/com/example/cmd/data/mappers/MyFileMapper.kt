package com.example.cmd.data.mappers

import android.net.Uri
import com.example.cmd.data.db.MyFileDbModel
import com.example.cmd.domain.entities.MyFileDomain
import javax.inject.Inject

class MyFileMapper @Inject constructor() {
  private fun mapDbToDtModel(myFileDbModel: MyFileDbModel) =
    MyFileDomain(
      size = myFileDbModel.size,
      name = myFileDbModel.name,
      priority = myFileDbModel.priority,
      uri = Uri.parse(myFileDbModel.uri),
      fileType = myFileDbModel.fileType,
      sizeFormatted = myFileDbModel.sizeFormatted
    )

  fun mapDbListToDtList(dbList: List<MyFileDbModel>): List<MyFileDomain> =
    dbList.map { mapDbToDtModel(it) }

}
