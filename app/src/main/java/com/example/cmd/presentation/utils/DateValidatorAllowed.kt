package com.example.cmd.presentation.utils

import android.os.Parcel
import android.os.Parcelable.Creator
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//DateValidator для Calendar Picker. Позволяет проверять, находится ли выбранная дата в множестве разрешённых дат.
class DateValidatorAllowed(private val allowed: Set<String>) : DateValidator {
  override fun isValid(date: Long): Boolean {
    val sdf = SimpleDateFormat("uuuu-MM-dd", Locale.US)
    val day = sdf.format(Date(date))
    return day in allowed
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {}
  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    return other is DateValidatorAllowed
  }

  override fun hashCode(): Int {
    val hashedFields = arrayOf<Any>()
    return hashedFields.contentHashCode()
  }

  companion object {
    @JvmField
    val CREATOR: Creator<DateValidatorAllowed?> = object : Creator<DateValidatorAllowed?> {
      override fun createFromParcel(source: Parcel): DateValidatorAllowed {
        return DateValidatorAllowed(setOf())
      }

      override fun newArray(size: Int): Array<DateValidatorAllowed?> {
        return arrayOfNulls(size)
      }
    }
  }
}
