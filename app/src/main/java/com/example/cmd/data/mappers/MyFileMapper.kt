package com.example.cmd.data.mappers

import com.example.cmd.data.db.MyFileDbModel
import com.example.cmd.domain.entities.MyFileDomain
import javax.inject.Inject

class MyFileMapper @Inject constructor() {
  fun mapDbToDtModel(myFileDbModel: MyFileDbModel) =
    MyFileDomain(
      size = myFileDbModel.size,
      path = myFileDbModel.path,
      priority = myFileDbModel.priority,
      name = myFileDbModel.name
    )

  fun mapDtToDbModel(myFileDomain: MyFileDomain) =
    MyFileDbModel(
      size = myFileDomain.size,
      path = myFileDomain.path,
      priority = myFileDomain.priority,
      name = myFileDomain.name
    )
}
