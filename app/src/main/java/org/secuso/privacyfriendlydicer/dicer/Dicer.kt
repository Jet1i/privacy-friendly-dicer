package org.secuso.privacyfriendlydicer.dicer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.SecureRandom

/**
 * Created by yonjuni on 5/6/15.
 */
class Dicer(val onChange: (List<Dice>) -> Unit) {

    private var dices: MutableList<Dice> = mutableListOf()

    class Dice(value: Int) {
        var value: Int = value
            internal set
        var locked: Boolean = false
    }

    fun rollDice(poolSize: Int, faceNum: Int) {
        if (dices.size != poolSize) {
            dices = (0 until poolSize).map { Dice(random.nextInt(faceNum) + 1) }.toMutableList()
        } else {
            for (dice in dices) {
                if (!dice.locked) {
                    dice.value = random.nextInt(faceNum) + 1
                }
            }
        }
        onChange(dices)
    }

    fun rollDiceOnly(poolSize: Int, faceNum: Int): List<Dice> {
        if (dices.size != poolSize) {
            dices = (0 until poolSize).map { Dice(random.nextInt(faceNum) + 1) }.toMutableList()
        } else {
            for (dice in dices) {
                if (!dice.locked) {
                    dice.value = random.nextInt(faceNum) + 1
                }
            }
        }
        return dices
    }

    fun lock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].locked = true
        onChange(dices)
    }

    fun unlock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].locked = false
        onChange(dices)
    }

    fun toggleLock(index: Int) {
        if (dices.size < index || index < 0) {
            return
        }

        dices[index].apply {
            locked = !locked
        }
    }

    companion object {
        private val random = SecureRandom()
    }
}
