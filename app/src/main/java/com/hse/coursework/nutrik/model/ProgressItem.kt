package com.hse.coursework.nutrik.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_items")
data class ProgressItem(
    @PrimaryKey val id: String,
    val title: String,
    val progress: Float
)
