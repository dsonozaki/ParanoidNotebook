package com.example.cmd.data.mappers

import com.example.cmd.data.db.MyNoteDbModel
import com.example.cmd.domain.entities.MyNoteDomain
import javax.inject.Inject

class MyNoteMapper @Inject constructor() {
  private fun mapDbToDtModel(myNoteDbModel: MyNoteDbModel) =
    MyNoteDomain(
      id = myNoteDbModel.id,
      title = myNoteDbModel.title,
      text = myNoteDbModel.text,
      date = myNoteDbModel.date
    )

  fun mapDtToDbModel(myNoteDomain: MyNoteDomain) =
    MyNoteDbModel(
      id = myNoteDomain.id,
      title = myNoteDomain.title,
      text = myNoteDomain.text,
      date = myNoteDomain.date
    )

  fun mapDbListToDtList(dbList: List<MyNoteDbModel>): List<MyNoteDomain> =
    dbList.map { mapDbToDtModel(it) }

}
