package org.secuso.privacyfriendlydicer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.secuso.privacyfriendlydicer.database.model.DiceMode

@Dao
interface DiceModeDao {

    @Query("SELECT * FROM dice_modes")
    fun all(): List<DiceMode>

    @Query("SELECT EXISTS (SELECT * FROM dice_modes WHERE id = :diceMode)")
    fun isValidDiceMode(diceMode: Int): Boolean

    @Query("SELECT * FROM dice_modes WHERE id = :diceMode")
    fun getDiceMode(diceMode: Int): DiceMode

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(mode: DiceMode)

    @Delete
    fun delete(mode: DiceMode)
}