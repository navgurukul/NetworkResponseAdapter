package com.navgurukul.networkresponseadapter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cacheEntry: CacheEntry)

    @Query("SELECT * FROM cache_entries WHERE cache_key = :key")
    suspend fun get(key: String): CacheEntry?

    @Query("DELETE FROM cache_entries WHERE timestamp + (max_age_seconds * 1000) < :currentTime")
    suspend fun deleteExpired(currentTime: Long)

    @Query("DELETE FROM cache_entries")
    suspend fun deleteAll()
}
