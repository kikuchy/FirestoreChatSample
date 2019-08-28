package com.diverseinc.firestorechat.ui.talk

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.diverseinc.firestorechat.R
import com.diverseinc.firestorechat.data.firebase.FirebaseRepository
import com.diverseinc.firestorechat.data.RoomId
import com.diverseinc.firestorechat.databinding.ActivityTalkBinding
import kotlinx.coroutines.InternalCoroutinesApi

class TalkActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ROOM_ID = "ROOM_ID"

        operator fun invoke(context: Context, roomId: RoomId) {
            context.startActivity(Intent(context, TalkActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId.id)
            })
        }
    }

    private val roomId: RoomId by lazy {
        RoomId(intent.getStringExtra(EXTRA_ROOM_ID))
    }

    @InternalCoroutinesApi
    private val viewModel: TalkViewModel by viewModels { TalkViewModel.Factory(FirebaseRepository(), roomId) }

    private lateinit var binding: ActivityTalkBinding

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_talk)
        binding.lifecycleOwner = this
        binding.transcriptList.adapter = TalkListAdapter()
        binding.viewModel = viewModel
        binding.owner = this
    }

    @InternalCoroutinesApi
    fun sendMessage() {
        val message = binding.draftMessage.text?.toString()
        if (message != null) {
            viewModel.sendMessage(message)
            binding.draftMessage.text?.clear()
        }
    }
}
