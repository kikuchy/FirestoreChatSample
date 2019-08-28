package com.diverseinc.firestorechat.ui.rooms

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diverseinc.firestorechat.data.Room
import com.diverseinc.firestorechat.databinding.AdapterRoomsBinding
import java.text.DateFormat
import java.util.*


class RoomListAdapter: RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {
    private var list: List<Room> = emptyList()

    private var onClickListener: ((Room) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterRoomsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], onClickListener)
    }

    fun update(rooms: List<Room>) {
        list = rooms
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: (Room) -> Unit) {
        onClickListener = listener
    }

    class ViewHolder(private val binding: AdapterRoomsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room, listener: ((Room) -> Unit)?) {
            binding.room = room
            binding.onClickListener = listener
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("items")
        fun RecyclerView.bindItems(rooms: List<Room>?) {
            if (rooms == null) return

            (adapter as RoomListAdapter).update(rooms)
        }

        @JvmStatic
        @BindingAdapter("android:text")
        fun TextView.setText(date: Date?) {
            text = date?.let {
                DateFormat.getInstance().format(date)
            } ?: ""
        }
    }
}
