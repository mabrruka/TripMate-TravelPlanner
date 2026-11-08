package com.example.tripmate.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tripId: Int,
    val title: String,
    val amount: Double,
    val category: String
)
