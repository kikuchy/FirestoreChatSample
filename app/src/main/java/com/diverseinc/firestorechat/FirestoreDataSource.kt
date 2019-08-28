package com.diverseinc.firestorechat

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FirestoreDataSource(private val userId: String): RoomDataSource, RoomDataStorage {
    private val roomRef = FirebaseFirestore.getInstance().collection("room")
    private val cache: MutableList<MainActivity.Room> = mutableListOf()

    override val itemCount: Int
        get() = cache.size
    override val items: List<MainActivity.Room>
        get() = cache

    fun reload() {
        roomRef
            .whereArrayContains("members", userId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null) {
                    cache.clear()
                    cache.addAll(querySnapshot.documents.map { it.toObject(MainActivity.Room::class.java)!! })
                } else {
                    Log.e(MainActivity.TAG, "Roomの内容引けなかった", firebaseFirestoreException)
                }

            }
    }

    override fun add(toUserId: String) {
        val room = MainActivity.Room().apply {
            members = mutableListOf(userId, toUserId)
        }
        roomRef.add(room).addOnSuccessListener {  }
    }
}