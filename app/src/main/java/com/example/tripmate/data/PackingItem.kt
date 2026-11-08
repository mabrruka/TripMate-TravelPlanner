package com.example.tripmate.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "packing_items",
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
data class PackingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tripId: Int,
    val name: String,
    val isPacked: Boolean = false
)
