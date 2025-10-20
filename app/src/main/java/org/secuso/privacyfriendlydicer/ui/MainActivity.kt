package org.secuso.privacyfriendlydicer.ui

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlydicer.R
import org.secuso.privacyfriendlydicer.databinding.ActivityMainBinding
import org.secuso.privacyfriendlydicer.ui.adapter.DiceAdapter
import java.util.Locale

class MainActivity : BaseActivity() {
    private val dicerViewModel by lazy { ViewModelProvider(this).get<DicerViewModel>(DicerViewModel::class.java) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val adapter: DiceAdapter by lazy {
        DiceAdapter(
            dicerViewModel.currentDices,
            layoutInflater
        )
    }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_dice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.dices.adapter = adapter
        adapter.onClick = {
            dicerViewModel.toggleLock(it)
            adapter.notifyItemChanged(it)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dicerViewModel.setDiceNumber(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seekBarFace.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dicerViewModel.setFaceNumber(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //Button
        binding.rollButton.setOnClickListener { rollDice() }

        displaySum()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                dicerViewModel.dices.collect {
                    adapter.dices = it
                    displaySum()
                    vibrate(50)
                }
            }
        }

        dicerViewModel.diceNumberLiveData.observe(this) {
            binding.chooseDiceNumber.text = String.format(Locale.ENGLISH, "%d", it)
        }

        dicerViewModel.faceNumberLiveData.observe(this) {
            binding.chooseFaceNumber.text = String.format(Locale.ENGLISH, "%d", it)
        }
    }

    override val onShake: () -> Unit = { rollDice() }
    fun rollDice() {
        dicerViewModel.rollDice()
    }

    private fun displaySum() {
        binding.sumTextView.text = getString(R.string.main_dice_sum,
            adapter.dices.sumOf { it.value }.toString())
    }
}
