package org.secuso.privacyfriendlydicer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.secuso.privacyfriendlydicer.database.model.DiceMode

@Dao
interface DiceModeDao {

    @Query("SELECT * FROM dice_modes")
    fun getAll(): Flow<List<DiceMode>>

    @Query("SELECT * FROM dice_modes")
    fun getAllSync(): List<DiceMode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(mode: DiceMode)

    @Delete
    fun delete(mode: DiceMode)
}