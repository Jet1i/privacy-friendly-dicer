package org.secuso.privacyfriendlydicer.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlydicer.database.DicerDatabase
import org.secuso.privacyfriendlydicer.database.model.DiceMode

class ManageDiceModeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DicerDatabase.getInstance(application)
    val diceModes: MutableSharedFlow<List<DiceMode>> = MutableStateFlow(listOf())

    fun loadDiceModes() {
        viewModelScope.launch {
            Log.d("Scope", "emitting")
            diceModes.emit(repository.diceModeDao().all())
        }
    }

    fun addDiceMode(mode: DiceMode) {
        viewModelScope.launch {
            repository.diceModeDao().add(mode)
            loadDiceModes()
        }
    }

    fun removeDiceMode(mode: DiceMode) {
        viewModelScope.launch {
            repository.diceModeDao().delete(mode)
        }
    }
}
