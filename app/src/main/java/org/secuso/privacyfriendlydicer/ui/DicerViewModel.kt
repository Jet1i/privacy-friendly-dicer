package org.secuso.privacyfriendlydicer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.secuso.privacyfriendlydicer.dicer.Dicer

class DicerViewModel : ViewModel() {
    private val dicer = Dicer()
    private var faceNumber = 6
    private var diceNumber = 5

    private val dicerLiveData = MutableLiveData<IntArray?>()
    private val diceNumberLiveData = MutableLiveData<Int?>()
    private val faceNumberLiveData = MutableLiveData<Int?>()

    init {
        dicerLiveData.postValue(IntArray(0))
    }

    fun getDicerLiveData(): LiveData<IntArray?> {
        return dicerLiveData
    }

    fun getDiceNumberLiveData(): LiveData<Int?> {
        return diceNumberLiveData
    }

    fun getFaceNumberLiveData(): LiveData<Int?> {
        return faceNumberLiveData
    }

    fun getDiceNumber(): Int {
        return diceNumber
    }

    fun getFaceNumber(): Int {
        return faceNumber
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
        dicerLiveData.postValue(dicer.rollDice(diceNumber, faceNumber))
    }
}
