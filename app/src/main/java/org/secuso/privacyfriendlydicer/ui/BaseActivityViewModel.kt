package org.secuso.privacyfriendlydicer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlydicer.database.DicerDatabase

class BaseActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DicerDatabase.getInstance(application)
    fun isValidDiceMode(diceMode: Int) = repository.diceModeDao().isValidDiceMode(diceMode)
    fun getDiceModeName(diceMode: Int) = repository.diceModeDao().getDiceMode(diceMode).name
}
