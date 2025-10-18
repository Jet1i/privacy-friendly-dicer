package org.secuso.privacyfriendlydicer.ui

import android.util.Log
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
import org.secuso.privacyfriendlydicer.dicer.Dicer

class DicerViewModel : ViewModel() {
    val dices: MutableSharedFlow<List<Dicer.Dice>> = MutableSharedFlow()
    private val dicer = Dicer {
        viewModelScope.launch {
            _dices = it
            dices.emit(it)
        }
    }
    var faceNumber = 6
        private set
    var diceNumber = 5
        private set

    private var _dices: List<Dicer.Dice> = dicer.rollDiceOnly(diceNumber, faceNumber)
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
        diceNumberLiveData.postValue(diceNumber)
    }

    fun setFaceNumber(faceNumber: Int) {
        this.faceNumber = faceNumber
        faceNumberLiveData.postValue(faceNumber)
    }

    fun rollDice() {
        viewModelScope.launch {
            dicer.rollDice(diceNumber, faceNumber)
        }
    }

    fun toggleLock(position: Int) {
        dicer.toggleLock(position)
    }
}
