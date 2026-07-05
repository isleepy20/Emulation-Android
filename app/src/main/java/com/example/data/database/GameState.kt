package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 1,
    val currentCookies: Double = 0.0,
    val totalCookiesBaked: Double = 0.0,
    val totalClicks: Long = 0L,
    val lastSavedTime: Long = System.currentTimeMillis(),
    
    // Click upgrades counts
    val plasticSpatulaCount: Int = 0,
    val goldenRollingPinCount: Int = 0,
    val chocolateEngineCount: Int = 0,
    val cosmicOvenCount: Int = 0,
    
    // CpS upgrades counts
    val cursorCount: Int = 0,
    val grandmaCount: Int = 0,
    val bakeryCount: Int = 0,
    val factoryCount: Int = 0,
    val portalCount: Int = 0,
    val timeMachineCount: Int = 0,
    
    // Unlocked achievements as comma separated string
    val unlockedAchievements: String = ""
)
