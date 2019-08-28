package com.diverseinc.firestorechat.ui.addroom

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.diverseinc.firestorechat.data.firebase.FirebaseRepository
import com.diverseinc.firestorechat.ui.rooms.RoomsViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.InternalCoroutinesApi

class AddRoomDialog : DialogFragment() {

    @InternalCoroutinesApi
    private val viewModel: RoomsViewModel by activityViewModels {
        RoomsViewModel.Factory(
            FirebaseRepository()
        )
    }

    @InternalCoroutinesApi
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val textEdit = TextInputEditText(context).apply {
            hint = "User ID"
        }
        return AlertDialog
            .Builder(requireContext())
            .setTitle("Input user ID to chat with you")
            .setView(textEdit)
            .setNegativeButton("Cancel") { _, _ -> this.dismiss() }
            .setPositiveButton("Add") { _, _ ->
                viewModel.addRoom(textEdit.text?.toString() ?: "")
            }
            .create()
    }

    companion object {
        @JvmStatic
        fun invoke() = AddRoomDialog()
    }
}
