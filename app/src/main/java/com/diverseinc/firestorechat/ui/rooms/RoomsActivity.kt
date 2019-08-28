package com.diverseinc.firestorechat.ui.rooms

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.diverseinc.firestorechat.ui.addroom.AddRoomDialog
import com.diverseinc.firestorechat.R
import com.diverseinc.firestorechat.data.firebase.FirebaseRepository
import com.diverseinc.firestorechat.databinding.ActivityRoomsBinding
import com.diverseinc.firestorechat.ui.talk.TalkActivity
import kotlinx.coroutines.InternalCoroutinesApi

class RoomsActivity : AppCompatActivity() {
    lateinit var binding: ActivityRoomsBinding

    @InternalCoroutinesApi
    private val viewModel: RoomsViewModel by viewModels { RoomsViewModel.Factory(FirebaseRepository()) }

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rooms)
        binding.roomsList.apply {
            addItemDecoration(
                DividerItemDecoration(
                    binding.roomsList.context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
            adapter = RoomListAdapter().apply {
                setOnClickListener { TalkActivity(this@RoomsActivity, it.id) }
            }
        }
        lifecycle.addObserver(viewModel)
        binding.viewmodel = viewModel
        binding.owner = this
        binding.lifecycleOwner = this
    }

    fun onClickAdd() {
        AddRoomDialog().show(supportFragmentManager, "HOGEHOEG")
    }
}
