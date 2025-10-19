package org.secuso.privacyfriendlydicer.database

import androidx.room.TypeConverter
import org.secuso.privacyfriendlydicer.dicer.Dicer

class Converters {
    @TypeConverter
    fun toDiceSortingOption(value: String) = enumValueOf<Dicer.SortOptions>(value)

    @TypeConverter
    fun fromDiceSortingOption(value: Dicer.SortOptions) = value.name
}