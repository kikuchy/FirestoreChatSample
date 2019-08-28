package com.diverseinc.firestorechat.data

import java.util.*

data class TranscriptDigest(
    val text: String,
    val from: UserId,
    val to: UserId,
    val imageMap: List<Any>
)

data class Transcript(
    val id: String,
    val text: String,
    val from: UserId,
    val to: UserId,
    val createdAt: Date,
    val updatedAt: Date,
    val imageMap: List<Any>
)