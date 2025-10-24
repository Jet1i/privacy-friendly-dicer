package org.secuso.privacyfriendlydicer.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import org.secuso.privacyfriendlydicer.dicer.Dicer

class Converters {
    @TypeConverter
    fun toDiceSortingOption(value: String) = enumValueOf<Dicer.SortOptions>(value)

    @TypeConverter
    fun fromDiceSortingOption(value: Dicer.SortOptions) = value.name

    @TypeConverter
    fun toDiceList(value: String): List<Dicer.Dice> = Json.decodeFromString<List<Dicer.Dice>>(value)

    @TypeConverter
    fun fromDiceList(value: List<Dicer.Dice>) = Json.encodeToString(value)
}