package com.diverseinc.firestorechat.data

import java.util.*

inline class RoomId(val id: String)

inline class UserId(val id: String) {
    companion object {
        val INVALID = UserId("[INVALID USER]")
    }
}

data class Room(
    val id: RoomId,
    val hidden: Boolean,
    val messagingEnabled: Boolean,
    val lastViewedTimestamps: Map<UserId, Date>,
    val members: List<UserId>,
    val recentTranscript: TranscriptDigest?,
    val createdAt: Date,
    val updatedAt: Date
)
