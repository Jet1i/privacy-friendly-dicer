package org.secuso.privacyfriendlydicer.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlydicer.database.DicerDatabase
import org.secuso.privacyfriendlydicer.dicer.Dicer

class DicerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DicerDatabase.getInstance(application)
    val dices: MutableSharedFlow<List<Dicer.Dice>> = MutableSharedFlow()
    val remainingRolls: MutableSharedFlow<Int> = MutableSharedFlow()
    private val dicer = Dicer {
        viewModelScope.launch {
            _dices = it
            dices.emit(it)
            rolls += 1
            if (maxRolls > 0) {
                remainingRolls.emit(maxRolls - rolls)
            }
        }
    }
    var faceNumber: Int = 6
        private set
    var diceNumber = 5
        private set
    private var rolls = 0
    private var maxRolls = -1


    private var _dices: List<Dicer.Dice> = {
        dicer.reloadDice(diceNumber, faceNumber)
        dicer.rollDiceOnly()
    }()
    val currentDices
        get() = _dices

    val diceNumberLiveData = MutableLiveData<Int?>()
    val faceNumberLiveData = MutableLiveData<Int?>()


    fun getDiceNumberLiveData(): LiveData<Int?> {
        return diceNumberLiveData
    }

    fun getFaceNumberLiveData(): LiveData<Int?> {
        return faceNumberLiveData
    }

    fun setDiceNumber(diceNumber: Int) {
        this.diceNumber = diceNumber
        dicer.reloadDice(diceNumber, faceNumber)
        diceNumberLiveData.postValue(diceNumber)
    }

    fun setFaceNumber(faceNumber: Int) {
        this.faceNumber = faceNumber
        dicer.reloadDice(diceNumber, faceNumber)
        faceNumberLiveData.postValue(faceNumber)
    }

    fun rollDice() {
        if (maxRolls !in 0..rolls) {
            viewModelScope.launch {
                dicer.rollDice()
            }
        }
    }

    fun toggleLock(position: Int) {
        dicer.toggleLock(position)
    }

    fun isValidDiceMode(diceMode: Int) = repository.diceModeDao().isValidDiceMode(diceMode)
    fun loadDiceMode(diceMode: Int) {
        val mode = repository.diceModeDao().getDiceMode(diceMode)
        dicer.loadDice(mode.dices)
        maxRolls = mode.rounds
        rolls = 0
        rollDice()
    }
}
