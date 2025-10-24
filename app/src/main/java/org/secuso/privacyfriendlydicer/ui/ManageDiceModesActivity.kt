package org.secuso.privacyfriendlydicer.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.model.dialog.ValueSelectionDialog
import org.secuso.pfacore.ui.dialog.ShowValueSelectionDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlydicer.PFApplicationData
import org.secuso.privacyfriendlydicer.database.model.DiceMode
import org.secuso.privacyfriendlydicer.databinding.ActivityManageDiceModesBinding
import org.secuso.privacyfriendlydicer.databinding.DialogNewDiceModeBinding
import org.secuso.privacyfriendlydicer.dicer.Dicer
import org.secuso.privacyfriendlydicer.ui.adapter.DiceAdapter
import org.secuso.privacyfriendlydicer.ui.adapter.DiceModeAdapter
import org.secuso.privacyfriendlydicer.R

class ManageDiceModesActivity: BaseActivity() {
    override val onShake = null
    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.puzzle

    private val viewModel by lazy { ViewModelProvider(this)[ManageDiceModeViewModel::class.java] }
    val diceMode by lazy { PFApplicationData.instance(this).selectedDiceMode }
    val deleteDiceModeDialog by lazy { PFApplicationData.instance(this).deleteDiceModeDialog }

    private lateinit var dialogBinding: DialogNewDiceModeBinding
    private lateinit var adapter: DiceModeAdapter
    private val createDialog by lazy {
        ShowValueSelectionDialog(
            binding = dialogBinding,
            extraction = {
                DiceMode(
                    dices = (it.dices.adapter as DiceAdapter).dices,
                    name = it.diceModeName.text.toString(),
                    rounds = it.rounds.text.toString().toIntOrNull() ?: -1,
                    // TODO: Actual values not default ones
                    sortingOption = Dicer.SortOptions.NONE
                )
            },
            dialog = ValueSelectionDialog.build(this) {
                title = { getString(R.string.new_dice_mode_title) }
                acceptLabel = getString(R.string.new_dice_mode_create_label)
                onConfirmation = {
                    viewModel.addDiceMode(it)
                }
                isValid = {
                    val enabled = MutableLiveData(false)

                    val check = {
                        enabled.postValue(dialogBinding.diceModeName.text?.isNotBlank() == true
                            && (dialogBinding.dices.adapter as DiceAdapter).dices.isNotEmpty()
                            && dialogBinding.rounds.text?.isNotBlank() == true)
                    }

                    val watcher = object: TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            return
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            return
                        }

                        override fun afterTextChanged(s: Editable?) {
                            check()
                        }
                    }
                    dialogBinding.diceModeName.addTextChangedListener(watcher)
                    dialogBinding.rounds.addTextChangedListener(watcher)
                    dialogBinding.dice.addTextChangedListener(watcher)
                    dialogBinding.addDice.setOnClickListener {
                        if (!dialogBinding.dice.text.isNullOrBlank()) {
                            (dialogBinding.dices.adapter as DiceAdapter).addDice(dialogBinding.dice.text.toString().toInt())
                            check()
                        }
                    }

                    enabled
                }
            }
        )
    }

    interface DeleteDialogActions {
        fun onDelete(): Unit
        fun onAbort(): Unit
    }

    private fun deleteDialog(modeToDelete: DiceMode, action: DeleteDialogActions) = AbortElseDialog.build(this) {
        acceptLabel = ContextCompat.getString(this@ManageDiceModesActivity, R.string.manage_dice_mode_delete_dialog_delete_label)
        title = { getString(R.string.manage_dice_mode_delete_dialog_title, modeToDelete.name) }
        content = { ContextCompat.getString(this@ManageDiceModesActivity, R.string.manage_dice_mode_delete_dialog_desc) }
        onElse = action::onDelete
        onAbort = action::onAbort
        handleDismiss = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityManageDiceModesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DiceModeAdapter(listOf(), layoutInflater)
        adapter.onClick = {
            diceMode.value = it.id
            startActivity(Intent(this, DiceModeActivity::class.java))
        }

        val ithCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun isLongPressDragEnabled() = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val mode = adapter.diceModes[viewHolder.adapterPosition]
                val delete = {
                    viewModel.removeDiceMode(mode)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    diceMode.value = -1
                    reloadDrawer()
                }
                if (deleteDiceModeDialog.value) {
                    deleteDialog(mode, object : DeleteDialogActions {
                        override fun onDelete() = delete()
                        override fun onAbort() {
                            Log.d("Called", "called")
                            adapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                    }).show()
                } else {
                    delete()
                }
            }
        }
        val ith = ItemTouchHelper(ithCallback)
        ith.attachToRecyclerView(binding.diceModes)
        binding.diceModes.adapter = adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.diceModes.collect {
                    Log.d("Scope", "receiving")
                    adapter.diceModes = it
                    adapter.notifyDataSetChanged()
                }
            }
        }
        binding.fab.setOnClickListener {
            createDialog.binding.root.visibility = View.VISIBLE
            createDialog.show()
        }

        dialogBinding = DialogNewDiceModeBinding.inflate(layoutInflater)
        dialogBinding.root.visibility = View.GONE
        dialogBinding.dices.adapter = DiceAdapter(listOf(), layoutInflater)

        viewModel.loadDiceModes()
    }
}