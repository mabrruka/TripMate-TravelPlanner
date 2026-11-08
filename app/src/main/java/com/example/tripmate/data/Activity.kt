package com.example.tripmate.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class TripActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tripId: Int,
    val title: String,
    val dayNumber: Int,
    val time: String
)
