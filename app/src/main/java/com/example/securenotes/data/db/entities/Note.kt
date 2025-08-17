package com.example.securenotes.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isPrivate: Boolean = false

)
