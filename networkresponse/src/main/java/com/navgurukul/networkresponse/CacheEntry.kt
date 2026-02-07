package com.navgurukul.networkresponse


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing cached network responses
 */
@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val key: String,

    @ColumnInfo(name = "data")
    val data: String,

    @ColumnInfo(name = "headers")
    val headers: String?,

    @ColumnInfo(name = "code")
    val code: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "max_age_seconds")
    val maxAgeSeconds: Long
)