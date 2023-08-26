package com.example.cmd.data.mappers

import android.net.Uri
import com.example.cmd.data.db.MyFileDbModel
import com.example.cmd.domain.entities.MyFileDomain
import javax.inject.Inject

class MyFileMapper @Inject constructor() {
  private fun mapDbToDtModel(myFileDbModel: MyFileDbModel) =
    MyFileDomain(
      size = myFileDbModel.size,
      path = myFileDbModel.path,
      priority = myFileDbModel.priority,
      uri = Uri.parse(myFileDbModel.uri),
      fileType = myFileDbModel.fileType,
      sizeFormated = myFileDbModel.sizeFormated
    )

  fun mapDbListToDtList(dbList: List<MyFileDbModel>): List<MyFileDomain> =
    dbList.map { mapDbToDtModel(it) }

}
