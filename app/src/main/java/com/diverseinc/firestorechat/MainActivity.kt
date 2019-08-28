package com.diverseinc.firestorechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diverseinc.firestorechat.ui.addroom.AddRoomDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    lateinit var roomListAdapter: RoomListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { result ->
                    if (result.isSuccessful) {
                        initViewsWithRooms()
                    } else {
                        Log.e(TAG, "Firebaseのユーザー作るのに失敗したよ")
                    }
                }
        } else {
            initViewsWithRooms()
        }
    }

    private fun initViewsWithRooms() {
        val source = FirestoreDataSource(FirebaseAuth.getInstance().currentUser!!.uid)
        roomListAdapter = RoomListAdapter(source)
        roomListView.adapter = roomListAdapter
        source.reload()
        addRoomButton.setOnClickListener {
            AddRoomDialog().show(supportFragmentManager, "AddRoom")
        }
    }

    class RoomListItemHolder(view: View): RecyclerView.ViewHolder(view) {
        private val lastMessage: TextView = view.findViewById(R.id.roomLastMesssage)
        private val lastUpdated: TextView = view.findViewById(R.id.roomLastUpdatedDate)

        fun bind(item: Room) {
            lastMessage.text = item.recentTranscript?.text ?: ""
            lastUpdated.text = item.updatedAt.toString()
        }
    }

    class Transcript{
        var id: String? = null
        lateinit var from: String
        lateinit var text: String
        lateinit var to: String

    }

    class Room{
        var id: String? = null
        var lastViewedTimestamps: MutableList<Date> = mutableListOf()
        var members: MutableList<String> = mutableListOf()
        var recentTranscript: Transcript? = null
        var createdAt: Date = Date()
        var updatedAt: Date = Date()
    }


    class RoomListAdapter(private val dataSource: RoomDataSource) : RecyclerView.Adapter<RoomListItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomListItemHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.room_list_item, parent)
            return RoomListItemHolder(itemView)
        }

        override fun getItemCount(): Int = dataSource.itemCount

        override fun onBindViewHolder(holder: RoomListItemHolder, position: Int) {
            holder.bind(dataSource.items[position])
        }
    }
}
