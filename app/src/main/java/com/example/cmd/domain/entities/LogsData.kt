@file:UseSerializers(CalendarAsLongSerializer::class)
package com.example.cmd.domain.entities

import com.example.cmd.domain.kserializer.CalendarAsLongSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Calendar


@Serializable
data class LogsData(val lastDayOfAutoDeletion: Long = 0L, val logsAutoRemovePeriod: Int = 7,val logDates: PersistentList<Calendar> = persistentListOf())
