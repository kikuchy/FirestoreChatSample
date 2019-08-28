package com.diverseinc.firestorechat

interface RoomDataSource {
    val itemCount: Int
    val items: List<MainActivity.Room>
}