package org.secuso.privacyfriendlydicer.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.secuso.privacyfriendlydicer.dicer.Dicer

@Entity(tableName = "dice_modes", indices = [Index(value = ["name"], unique = true)])
data class DiceMode(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dices: List<Dicer.Dice>,
    val rounds: Int,
    val sortingOption: Dicer.SortOptions
)