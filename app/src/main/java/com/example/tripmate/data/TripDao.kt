package com.example.tripmate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    // TRIPS

    @Query("SELECT * FROM trips ORDER BY startDate ASC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripById(tripId: Int): Flow<Trip?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Delete
    suspend fun deleteTrip(trip: Trip)

    // ACTIVITIES

    @Query("SELECT * FROM activities WHERE tripId = :tripId ORDER BY dayNumber ASC, time ASC")
    fun getActivities(tripId: Int): Flow<List<TripActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: TripActivity)

    @Delete
    suspend fun deleteActivity(activity: TripActivity)

    // EXPENSES

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY id DESC")
    fun getExpenses(tripId: Int): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // PACKING ITEMS

    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY id ASC")
    fun getPackingItems(tripId: Int): Flow<List<PackingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItem)

    @Update
    suspend fun updatePackingItem(item: PackingItem)

    @Delete
    suspend fun deletePackingItem(item: PackingItem)
}
