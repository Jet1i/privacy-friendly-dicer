package org.secuso.privacyfriendlydicer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.dialog.InfoDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlydicer.PFApplicationData
import org.secuso.privacyfriendlydicer.R
import org.secuso.privacyfriendlydicer.databinding.ActivityMainBinding
import org.secuso.privacyfriendlydicer.ui.adapter.DiceAdapter

class DiceModeActivity : BaseActivity() {
    private val dicerViewModel by lazy { ViewModelProvider(this).get<DicerViewModel>(DicerViewModel::class.java) }
    private val adapter: DiceAdapter by lazy {
        DiceAdapter(
            dicerViewModel.currentDices,
            layoutInflater
        )
    }

    val invalidDiceModeDialog = InfoDialog.build(this) {
        title = { ContextCompat.getString(context, R.string.dialog_select_game_mode_first_title) }
        content = { ContextCompat.getString(context, R.string.dialog_select_game_mode_first_desc) }
        onClose = {
            startActivity(Intent(this@DiceModeActivity, ManageDiceModesActivity::class.java))
            finish()
        }
    }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.baseline_play_circle_outline_24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val diceMode = PFApplicationData.instance(this).selectedDiceMode.value
        if (!dicerViewModel.isValidDiceMode(diceMode)) {
            invalidDiceModeDialog.show()
        }
        dicerViewModel.loadDiceMode(diceMode)
        binding.seekBar.visibility = View.GONE
        binding.seekBarFace.visibility = View.GONE
        binding.chooseDiceHeading.visibility = View.GONE
        binding.chooseFacesHeading.visibility = View.GONE
        binding.chooseDiceNumber.visibility = View.GONE
        binding.chooseFaceNumber.visibility = View.GONE
        binding.dices.adapter = adapter
        adapter.onClick = {
            dicerViewModel.toggleLock(it)
            adapter.notifyItemChanged(it)
        }

        //Button
        binding.rollButton.setOnClickListener { rollDice() }

        binding.sumTextView.text = getString(R.string.main_dice_sum,
            adapter.dices.sumOf { it.value }.toString())
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                dicerViewModel.dices.collect {
                    adapter.dices = it
                    binding.sumTextView.text = getString(R.string.main_dice_sum,
                        adapter.dices.sumOf { it.value }.toString())
                    vibrate(50)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                dicerViewModel.remainingRolls.collect {
                    binding.rollButton.apply {
                        Log.d("Test", "received: $it")
                        if (it == 0) {
                            text = ContextCompat.getString(context, R.string.restart_button)
                            setOnClickListener {
                                dicerViewModel.loadDiceMode(diceMode)
                                text = ContextCompat.getString(context, R.string.roll_button)
                                setOnClickListener { rollDice() }
                            }
                        }
                    }
                }
            }
        }
    }

    override val onShake: () -> Unit = { rollDice() }
    fun rollDice() {
        dicerViewModel.rollDice()
    }
}
