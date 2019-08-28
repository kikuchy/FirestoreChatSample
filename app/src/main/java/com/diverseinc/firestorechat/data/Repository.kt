package com.diverseinc.firestorechat.data

import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun userId(): UserId
    suspend fun rooms(): Flow<List<Room>>
    suspend fun addRoom(toUserId: String)
    suspend fun talks(roomId: RoomId): Flow<List<Transcript>>
    suspend fun postTranscript(roomId: RoomId, message: String)
}