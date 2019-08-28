package com.diverseinc.firestorechat.data.firebase

import com.diverseinc.firestorechat.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseRepository() : Repository {
    private val db = FirebaseFirestore.getInstance()
    private val roomRef = db.collection("room")

    override suspend fun userId(): UserId {
        val auth = FirebaseAuth.getInstance()
        return if (auth.currentUser == null) {
            suspendCoroutine { continuation ->
                auth.signInAnonymously()
                    .addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            continuation.resume(UserId(auth.currentUser!!.uid))
                        } else {
                            continuation.resumeWithException(IllegalStateException())
                        }
                    }
            }
        } else {
            UserId(auth.currentUser!!.uid)
        }
    }


    @ExperimentalCoroutinesApi
    override suspend fun rooms(): Flow<List<Room>> {
        return channelFlow {
            val registration = roomRef
                .whereArrayContains("members", userId().id)
                .whereEqualTo("isHidden", false)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) {
                        close(firebaseFirestoreException)
                    } else {
                        offer(querySnapshot.documents.map {
                            val transcripMap: Map<String, Any?>? =
                                it["recentTranscript"] as? Map<String, Any?>
                            Room(
                                id = RoomId(it.id),
                                hidden = it.getBoolean("isHidden") ?: true,
                                members = (it["members"] as? List<String>)?.map {
                                    UserId(
                                        it
                                    )
                                }
                                    ?: emptyList(),
                                recentTranscript = transcripMap?.let {
                                    TranscriptDigest(
                                        text = transcripMap["text"] as String,
                                        from = UserId(transcripMap["from"] as String),
                                        to = UserId(transcripMap["to"] as String),
                                        imageMap = transcripMap as? List<Any> ?: emptyList()
                                    )
                                },
                                createdAt = it.getDate("createdAt") ?: Date(),
                                updatedAt = it.getDate("updatedAt") ?: Date(),
                                messagingEnabled = it.getBoolean("isMessagingEnabled") ?: false,
                                lastViewedTimestamps = (it["lastViewedTimestamps"] as? Map<String, Date>)?.mapKeys {
                                    UserId(
                                        it.key
                                    )
                                } ?: emptyMap()
                            )
                        })
                    }
                }
            awaitClose {
                registration.remove()
            }
        }
    }

    override suspend fun addRoom(toUserId: String) {
        val userId = userId()
        suspendCoroutine<Unit> { continuation ->
            roomRef
                .add(
                    mapOf(
                        "members" to arrayListOf(toUserId, userId),
                        "recentTranscript" to emptyMap<String, Any>(),
                        "lastViewedTimestamps" to mapOf<String, Date>(),
                        "createdAt" to Date(),
                        "updatedAt" to Date(),
                        "isHidden" to false,
                        "isMessagingEnabled" to true
                    )
                )
                .addOnCompleteListener { result ->
                    val e = result.exception
                    if (e != null) {
                        continuation.resumeWithException(e)
                    } else {
                        continuation.resume(Unit)
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun talks(roomId: RoomId): Flow<List<Transcript>> {
        val userId = userId()
        suspendCoroutine<Unit> { continuation ->
            val theRoom = roomRef.document(roomId.id)
            db.runTransaction { transaction ->
                val old: Map<String, Date> =
                    transaction.get(theRoom)["lastViewedTimestamps"] as? Map<String, Date>
                        ?: emptyMap()
                transaction.update(theRoom, "lastViewedTimestamps", old + (userId.id to Date()))
            }
                .addOnCompleteListener { result ->
                    val e = result.exception
                    if (e != null) {
                        continuation.resumeWithException(e)
                    } else {
                        continuation.resume(Unit)
                    }
                }
        }
        return channelFlow {
            val registration = roomRef
                .document(roomId.id)
                .collection("transcripts")
                .orderBy("createdAt")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) {
                        offer(querySnapshot.documents.map {
                            Transcript(
                                id = it.id,
                                text = it.getString("text") ?: "",
                                createdAt = it.getDate("createdAt") ?: Date(),
                                updatedAt = it.getDate("updatedAt") ?: Date(),
                                from = it.getString("from")?.let {
                                    UserId(
                                        it
                                    )
                                } ?: UserId.INVALID,
                                to = it.getString("to")?.let {
                                    UserId(
                                        it
                                    )
                                } ?: UserId.INVALID,
                                imageMap = emptyList()
                            )
                        })
                    } else {
                        close(firebaseFirestoreException)
                    }
                }
            awaitClose {
                registration.remove()
            }
        }
    }

    override suspend fun postTranscript(roomId: RoomId, message: String) {
        val theRoom = roomRef.document(roomId.id)
        val members = suspendCoroutine<List<UserId>> { continuation ->
            theRoom
                .get()
                .addOnCompleteListener {
                    val result = it.result
                    if (result != null) {
                        val members = result["members"] as List<String>
                        continuation.resume(members.map {
                            UserId(
                                it
                            )
                        })
                    } else {
                        continuation.resumeWithException(it.exception!!)
                    }
                }
        }
        val fromUserId = userId()
        val toUserId = members.first { it != fromUserId }
        val transcriptMap = mapOf(
            "createdAt" to Date(),
            "from" to fromUserId.id,
            "imageMap" to emptyList<Any>(),
            "text" to message,
            "to" to toUserId.id,
            "updatedAt" to Date()
        )

        coroutineScope {
            val addRecent = async {
                suspendCoroutine<Unit> { continuation ->
                    db.runBatch { batch ->
                        batch.update(theRoom, "recentTranscript", transcriptMap)
                        batch.update(theRoom, "updatedAt", Date())
                    }
                        .addOnCompleteListener { result ->
                            val e = result.exception
                            if (e != null) {
                                continuation.resumeWithException(e)
                            } else {
                                continuation.resume(Unit)
                            }
                        }
                }
            }
            val addCollection = async {
                suspendCoroutine<Unit> { continuation ->
                    theRoom
                        .collection("transcripts")
                        .add(transcriptMap)
                        .addOnCompleteListener { result ->
                            val e = result.exception
                            if (e != null) {
                                continuation.resumeWithException(e)
                            } else {
                                continuation.resume(Unit)
                            }
                        }
                }
            }
            addRecent.await()
            addCollection.await()
        }
    }
}