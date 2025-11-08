package org.secuso.privacyfriendlydicer.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlydicer.R
import org.secuso.privacyfriendlydicer.databinding.ActivityMainBinding
import org.secuso.privacyfriendlydicer.sound.SoundPreferences
import org.secuso.privacyfriendlydicer.sound.ThreeSecondSoundPlayer
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
    private val soundPlayer by lazy { ThreeSecondSoundPlayer(this) }
    private val builtInSounds by lazy {
        listOf(
            BuiltInSound(R.string.sound_option_rollthedice, rawResourceUri(R.raw.rollthedice)),
            BuiltInSound(R.string.sound_option_alien, rawResourceUri(R.raw.alien)),
            BuiltInSound(R.string.sound_option_no, rawResourceUri(R.raw.no)),
            BuiltInSound(R.string.sound_option_ok, rawResourceUri(R.raw.ok)),
            BuiltInSound(R.string.sound_option_yahoo, rawResourceUri(R.raw.yahoo)),
        )
    }
    private val soundPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { handleSoundSelection(it) }
        }
    }
    private val defaultSoundUri: Uri by lazy { builtInSounds.first().uri }

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
        binding.chooseSoundButton.setOnClickListener { showSoundChooserDialog() }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_select_sound -> {
            showSoundChooserDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override val onShake: () -> Unit = { rollDice() }
    fun rollDice() {
        playCustomSoundIfNeeded()
        dicerViewModel.rollDice()
    }

    private fun displaySum() {
        binding.sumTextView.text = getString(R.string.main_dice_sum,
            adapter.dices.sumOf { it.value }.toString())
    }

    private fun playCustomSoundIfNeeded() {
        if (!SoundPreferences.isSoundEnabled(this)) return
        val soundUri = SoundPreferences.getCustomSoundUri(this) ?: defaultSoundUri
        soundPlayer.play(soundUri)
    }

    private fun launchSoundPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        soundPickerLauncher.launch(intent)
    }

    private fun showSoundChooserDialog() {
        val options = builtInSounds.map { getString(it.labelRes) } + getString(R.string.sound_option_pick_file)
        val currentUriString = (SoundPreferences.getCustomSoundUri(this) ?: defaultSoundUri).toString()
        val selectedIndex = builtInSounds.indexOfFirst { it.uri.toString() == currentUriString }
        AlertDialog.Builder(this)
            .setTitle(R.string.sound_picker_title)
            .setSingleChoiceItems(options.toTypedArray(), selectedIndex) { dialog, which ->
                if (which < builtInSounds.size) {
                    val selected = builtInSounds[which]
                    SoundPreferences.setCustomSoundUri(this, selected.uri, contentResolver)
                    Toast.makeText(this, R.string.sound_picker_saved, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    launchSoundPicker()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun handleSoundSelection(uri: Uri) {
        SoundPreferences.setCustomSoundUri(this, uri, contentResolver)
        Toast.makeText(this, R.string.sound_picker_saved, Toast.LENGTH_SHORT).show()
    }

    private fun rawResourceUri(@RawRes resId: Int): Uri {
        val res = resources
        val pkg = res.getResourcePackageName(resId)
        val type = res.getResourceTypeName(resId)
        val entry = res.getResourceEntryName(resId)
        return Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://$pkg/$type/$entry")
    }

    override fun onDestroy() {
        soundPlayer.stop()
        super.onDestroy()
    }

    private data class BuiltInSound(@StringRes val labelRes: Int, val uri: Uri)
}
