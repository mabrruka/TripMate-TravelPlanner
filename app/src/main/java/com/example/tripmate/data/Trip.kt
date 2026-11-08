package com.example.tripmate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val notes: String = ""
)
