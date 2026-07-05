package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.random.Random

data class UpgradeItem(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Double,
    val costMultiplier: Double,
    val effectValue: Double,
    val isCps: Boolean, // true for CpS, false for Click Power
    val count: Int,
    val icon: String // Emoji representation
) {
    val currentCost: Double
        get() = baseCost * costMultiplier.pow(count)
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: String
)

data class FloatingText(
    val id: Long,
    val text: String,
    val xOffset: Float,
    val yOffset: Float,
    val scale: Float = 1.0f,
    val isFever: Boolean = false
)

data class GoldenCookieState(
    val isVisible: Boolean = false,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val size: Float = 60f
)

class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.gameStateDao()

    // Primary state variables
    private val _currentCookies = MutableStateFlow(0.0)
    val currentCookies: StateFlow<Double> = _currentCookies.asStateFlow()

    private val _totalCookiesBaked = MutableStateFlow(0.0)
    val totalCookiesBaked: StateFlow<Double> = _totalCookiesBaked.asStateFlow()

    private val _totalClicks = MutableStateFlow(0L)
    val totalClicks: StateFlow<Long> = _totalClicks.asStateFlow()

    // Upgrades lists
    private val _upgrades = MutableStateFlow<List<UpgradeItem>>(emptyList())
    val upgrades: StateFlow<List<UpgradeItem>> = _upgrades.asStateFlow()

    // Achievements list
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Floating text list (click visual indicator)
    private val _floatingTexts = MutableStateFlow<List<FloatingText>>(emptyList())
    val floatingTexts: StateFlow<List<FloatingText>> = _floatingTexts.asStateFlow()

    // Golden Cookie (Fever Event) State
    private val _goldenCookie = MutableStateFlow(GoldenCookieState())
    val goldenCookie: StateFlow<GoldenCookieState> = _goldenCookie.asStateFlow()

    // Fever Mode details
    private val _feverActive = MutableStateFlow(false)
    val feverActive: StateFlow<Boolean> = _feverActive.asStateFlow()

    private val _feverMultiplier = MutableStateFlow(1)
    val feverMultiplier: StateFlow<Int> = _feverMultiplier.asStateFlow()

    private val _feverSecondsRemaining = MutableStateFlow(0)
    val feverSecondsRemaining: StateFlow<Int> = _feverSecondsRemaining.asStateFlow()

    // Live Cookie production chart data (last 30 seconds)
    private val _productionHistory = MutableStateFlow<List<Double>>(List(30) { 0.0 })
    val productionHistory: StateFlow<List<Double>> = _productionHistory.asStateFlow()

    // Offline Earnings Popup
    private val _offlineEarnings = MutableStateFlow<Double?>(null)
    val offlineEarnings: StateFlow<Double?> = _offlineEarnings.asStateFlow()

    private val _offlineSeconds = MutableStateFlow(0L)
    val offlineSeconds: StateFlow<Long> = _offlineSeconds.asStateFlow()

    private var nextFloatingTextId = 0L

    init {
        setupDefaultUpgradesAndAchievements()
        loadGame()

        // Core Game Loop: 10 ticks per second for smooth updates
        viewModelScope.launch {
            while (true) {
                delay(100)
                tickGame(0.1)
            }
        }

        // Live stats history logger (every 1 second)
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateHistory()
            }
        }

        // Periodic Autosave (every 5 seconds)
        viewModelScope.launch {
            while (true) {
                delay(5000)
                saveGame()
            }
        }

        // Golden Cookie Spawner loop
        viewModelScope.launch {
            while (true) {
                // Spawn golden cookie every 30 to 75 seconds
                val nextSpawnSeconds = Random.nextLong(30, 75)
                delay(nextSpawnSeconds * 1000)
                if (!_goldenCookie.value.isVisible && !_feverActive.value) {
                    spawnGoldenCookie()
                }
            }
        }
    }

    private fun setupDefaultUpgradesAndAchievements() {
        _upgrades.value = listOf(
            // Click upgrades
            UpgradeItem("click_spatula", "Plastic Spatula", "+1 Cookie per click.", 15.0, 1.15, 1.0, false, 0, "🥄"),
            UpgradeItem("click_pin", "Golden Pin", "+5 Cookies per click.", 120.0, 1.16, 5.0, false, 0, "🥖"),
            UpgradeItem("click_engine", "Choco Motor", "+25 Cookies per click.", 1100.0, 1.18, 25.0, false, 0, "⚙️"),
            UpgradeItem("click_oven", "Cosmic Oven", "+100 Cookies per click.", 12000.0, 1.20, 100.0, false, 0, "🌋"),

            // CpS upgrades
            UpgradeItem("cps_cursor", "Auto-Clicker", "Clicker cursor. +0.1 CpS.", 15.0, 1.15, 0.1, true, 0, "🖱️"),
            UpgradeItem("cps_grandma", "Grandma", "Experienced baker. +1.0 CpS.", 100.0, 1.15, 1.0, true, 0, "👵"),
            UpgradeItem("cps_bakery", "Bakery", "Full-scale store. +8.0 CpS.", 1100.0, 1.15, 8.0, true, 0, "🏪"),
            UpgradeItem("cps_factory", "Factory", "Mass baker. +47.0 CpS.", 12000.0, 1.15, 47.0, true, 0, "🏭"),
            UpgradeItem("cps_portal", "Space Portal", "Interdimensional. +260.0 CpS.", 130000.0, 1.15, 260.0, true, 0, "🌀"),
            UpgradeItem("cps_time", "Time Machine", "Bakes from past. +1400.0 CpS.", 1400000.0, 1.15, 1400.0, true, 0, "⏳")
        )

        _achievements.value = listOf(
            Achievement("ach_first", "First Bake", "Bake your first cookie!", false, "🍪"),
            Achievement("ach_100", "Novice Baker", "Bake 100 total cookies.", false, "🥞"),
            Achievement("ach_10k", "Cookie Baron", "Bake 10,000 total cookies.", false, "👑"),
            Achievement("ach_1m", "Megabake", "Bake 1,000,000 total cookies.", false, "🌌"),
            Achievement("ach_clicks_50", "Active Clicker", "Click the giant cookie 50 times.", false, "⚡"),
            Achievement("ach_clicks_500", "Tap Master", "Click the giant cookie 500 times.", false, "🎯"),
            Achievement("ach_golden", "Golden Touch", "Click a Golden Cookie!", false, "✨"),
            Achievement("ach_grandmas_5", "Grandma Army", "Have at least 5 Grandmas.", false, "💖"),
            Achievement("ach_factories_3", "Industrial Age", "Have at least 3 Cookie Factories.", false, "⚙️")
        )
    }

    // Load game from database
    private fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = dao.getGameState()
            withContext(Dispatchers.Main) {
                if (state != null) {
                    _currentCookies.value = state.currentCookies
                    _totalCookiesBaked.value = state.totalCookiesBaked
                    _totalClicks.value = state.totalClicks

                    // Reload upgrade counts
                    _upgrades.value = _upgrades.value.map { upgrade ->
                        when (upgrade.id) {
                            "click_spatula" -> upgrade.copy(count = state.plasticSpatulaCount)
                            "click_pin" -> upgrade.copy(count = state.goldenRollingPinCount)
                            "click_engine" -> upgrade.copy(count = state.chocolateEngineCount)
                            "click_oven" -> upgrade.copy(count = state.cosmicOvenCount)
                            "cps_cursor" -> upgrade.copy(count = state.cursorCount)
                            "cps_grandma" -> upgrade.copy(count = state.grandmaCount)
                            "cps_bakery" -> upgrade.copy(count = state.bakeryCount)
                            "cps_factory" -> upgrade.copy(count = state.factoryCount)
                            "cps_portal" -> upgrade.copy(count = state.portalCount)
                            "cps_time" -> upgrade.copy(count = state.timeMachineCount)
                            else -> upgrade
                        }
                    }

                    // Reload achievements
                    val unlockedSet = state.unlockedAchievements.split(",").filter { it.isNotEmpty() }.toSet()
                    _achievements.value = _achievements.value.map { ach ->
                        if (unlockedSet.contains(ach.id)) ach.copy(isUnlocked = true) else ach
                    }

                    // Calculate Offline Earnings
                    val timePassedMs = System.currentTimeMillis() - state.lastSavedTime
                    val timePassedSec = timePassedMs / 1000
                    if (timePassedSec > 10) { // Only calculate if offline more than 10 seconds
                        val baseCps = getCookiesPerSecond()
                        if (baseCps > 0) {
                            val earned = baseCps * timePassedSec
                            _currentCookies.value += earned
                            _totalCookiesBaked.value += earned
                            _offlineEarnings.value = earned
                            _offlineSeconds.value = timePassedSec
                        }
                    }
                }
            }
        }
    }

    // Save game to database
    fun saveGame() {
        val currentSpatula = _upgrades.value.firstOrNull { it.id == "click_spatula" }?.count ?: 0
        val currentPin = _upgrades.value.firstOrNull { it.id == "click_pin" }?.count ?: 0
        val currentEngine = _upgrades.value.firstOrNull { it.id == "click_engine" }?.count ?: 0
        val currentOven = _upgrades.value.firstOrNull { it.id == "click_oven" }?.count ?: 0

        val currentCursor = _upgrades.value.firstOrNull { it.id == "cps_cursor" }?.count ?: 0
        val currentGrandma = _upgrades.value.firstOrNull { it.id == "cps_grandma" }?.count ?: 0
        val currentBakery = _upgrades.value.firstOrNull { it.id == "cps_bakery" }?.count ?: 0
        val currentFactory = _upgrades.value.firstOrNull { it.id == "cps_factory" }?.count ?: 0
        val currentPortal = _upgrades.value.firstOrNull { it.id == "cps_portal" }?.count ?: 0
        val currentTimeMachine = _upgrades.value.firstOrNull { it.id == "cps_time" }?.count ?: 0

        val unlockedString = _achievements.value
            .filter { it.isUnlocked }
            .joinToString(",") { it.id }

        val gameState = GameState(
            id = 1,
            currentCookies = _currentCookies.value,
            totalCookiesBaked = _totalCookiesBaked.value,
            totalClicks = _totalClicks.value,
            lastSavedTime = System.currentTimeMillis(),
            plasticSpatulaCount = currentSpatula,
            goldenRollingPinCount = currentPin,
            chocolateEngineCount = currentEngine,
            cosmicOvenCount = currentOven,
            cursorCount = currentCursor,
            grandmaCount = currentGrandma,
            bakeryCount = currentBakery,
            factoryCount = currentFactory,
            portalCount = currentPortal,
            timeMachineCount = currentTimeMachine,
            unlockedAchievements = unlockedString
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertGameState(gameState)
        }
    }

    // Reset game completely
    fun resetGame() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearGameState()
            withContext(Dispatchers.Main) {
                _currentCookies.value = 0.0
                _totalCookiesBaked.value = 0.0
                _totalClicks.value = 0L
                _feverActive.value = false
                _feverSecondsRemaining.value = 0
                _feverMultiplier.value = 1
                setupDefaultUpgradesAndAchievements()
                saveGame()
            }
        }
    }

    // Dismiss offline earnings popup
    fun dismissOfflinePopup() {
        _offlineEarnings.value = null
        _offlineSeconds.value = 0L
    }

    // Calculate core click power
    fun getClickPower(): Double {
        var power = 1.0
        _upgrades.value.filter { !it.isCps }.forEach { upgrade ->
            power += upgrade.count * upgrade.effectValue
        }
        if (_feverActive.value) {
            power *= _feverMultiplier.value
        }
        return power
    }

    // Calculate cookies per second
    fun getCookiesPerSecond(): Double {
        var baseCps = 0.0
        _upgrades.value.filter { it.isCps }.forEach { upgrade ->
            baseCps += upgrade.count * upgrade.effectValue
        }
        if (_feverActive.value) {
            baseCps *= _feverMultiplier.value
        }
        return baseCps
    }

    // Click the big cookie!
    fun clickCookie() {
        val clickPower = getClickPower()
        _currentCookies.value += clickPower
        _totalCookiesBaked.value += clickPower
        _totalClicks.value += 1

        // Add a floating text click indicator
        val randX = Random.nextFloat() * 140f - 70f
        val randY = Random.nextFloat() * 40f - 120f
        val newText = FloatingText(
            id = nextFloatingTextId++,
            text = "+${formatValue(clickPower)}",
            xOffset = randX,
            yOffset = randY,
            scale = if (_feverActive.value) 1.5f else 1.0f,
            isFever = _feverActive.value
        )
        _floatingTexts.value = _floatingTexts.value + newText

        // Automatically clean up floating text after 1 second
        viewModelScope.launch {
            delay(1000)
            _floatingTexts.value = _floatingTexts.value.filter { it.id != newText.id }
        }

        checkAchievements()
    }

    // Handle buying an upgrade
    fun buyUpgrade(upgradeId: String): Boolean {
        val currentList = _upgrades.value
        val upgradeIndex = currentList.indexOfFirst { it.id == upgradeId }
        if (upgradeIndex != -1) {
            val upgrade = currentList[upgradeIndex]
            val cost = upgrade.currentCost
            if (_currentCookies.value >= cost) {
                _currentCookies.value -= cost
                val updatedUpgrade = upgrade.copy(count = upgrade.count + 1)
                _upgrades.value = currentList.toMutableList().apply {
                    set(upgradeIndex, updatedUpgrade)
                }
                
                checkAchievements()
                saveGame()
                return true
            }
        }
        return false
    }

    // Tick CpS and Fever mode
    private fun tickGame(secondsPassed: Double) {
        val currentCps = getCookiesPerSecond()
        if (currentCps > 0) {
            val gained = currentCps * secondsPassed
            _currentCookies.value += gained
            _totalCookiesBaked.value += gained
        }

        // Handle fever countdown
        if (_feverActive.value) {
            val rem = _feverSecondsRemaining.value
            if (rem > 0) {
                // Decay remaining timer
                viewModelScope.launch {
                    // Reduce by tick amount proportionally
                }
            }
        }

        checkAchievements()
    }

    // Explicit second decay for fever mode to be exact
    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_feverActive.value) {
                    val remaining = _feverSecondsRemaining.value - 1
                    if (remaining <= 0) {
                        _feverActive.value = false
                        _feverSecondsRemaining.value = 0
                        _feverMultiplier.value = 1
                    } else {
                        _feverSecondsRemaining.value = remaining
                    }
                }
            }
        }
    }

    // Golden Cookie Events
    private fun spawnGoldenCookie() {
        val size = Random.nextFloat() * 30f + 50f // 50 to 80 dp
        val x = Random.nextFloat() * 0.7f + 0.15f // Keep within safe screen center bounds
        val y = Random.nextFloat() * 0.5f + 0.2f
        _goldenCookie.value = GoldenCookieState(
            isVisible = true,
            xPercent = x,
            yPercent = y,
            size = size
        )

        // Dissolve after 10 seconds if not clicked
        viewModelScope.launch {
            delay(10000)
            if (_goldenCookie.value.isVisible) {
                _goldenCookie.value = _goldenCookie.value.copy(isVisible = false)
            }
        }
    }

    fun clickGoldenCookie() {
        if (!_goldenCookie.value.isVisible) return
        _goldenCookie.value = _goldenCookie.value.copy(isVisible = false)

        // Trigger Clicking Fever! (7x multipliers for 15 seconds)
        _feverActive.value = true
        _feverMultiplier.value = 7
        _feverSecondsRemaining.value = 15

        // Spawn a mega click floating text
        val newText = FloatingText(
            id = nextFloatingTextId++,
            text = "FEVER MODE ACTIVE! 7X CPS & CLICK POWER!",
            xOffset = 0f,
            yOffset = -150f,
            scale = 1.8f,
            isFever = true
        )
        _floatingTexts.value = _floatingTexts.value + newText

        // Unlock achievement
        unlockAchievement("ach_golden")

        viewModelScope.launch {
            delay(2500)
            _floatingTexts.value = _floatingTexts.value.filter { it.id != newText.id }
        }
    }

    // Add cookie count to history for line graph
    private fun updateHistory() {
        val currentHistory = _productionHistory.value.toMutableList()
        currentHistory.removeAt(0)
        currentHistory.add(_currentCookies.value)
        _productionHistory.value = currentHistory
    }

    // Check and unlock achievements
    private fun checkAchievements() {
        val totalBaked = _totalCookiesBaked.value
        val clicks = _totalClicks.value

        if (totalBaked >= 1.0) unlockAchievement("ach_first")
        if (totalBaked >= 100.0) unlockAchievement("ach_100")
        if (totalBaked >= 10000.0) unlockAchievement("ach_10k")
        if (totalBaked >= 1000000.0) unlockAchievement("ach_1m")

        if (clicks >= 50) unlockAchievement("ach_clicks_50")
        if (clicks >= 500) unlockAchievement("ach_clicks_500")

        val currentGrandmas = _upgrades.value.firstOrNull { it.id == "cps_grandma" }?.count ?: 0
        if (currentGrandmas >= 5) unlockAchievement("ach_grandmas_5")

        val currentFactories = _upgrades.value.firstOrNull { it.id == "cps_factory" }?.count ?: 0
        if (currentFactories >= 3) unlockAchievement("ach_factories_3")
    }

    private fun unlockAchievement(id: String) {
        val currentList = _achievements.value
        val achIndex = currentList.indexOfFirst { it.id == id }
        if (achIndex != -1) {
            val ach = currentList[achIndex]
            if (!ach.isUnlocked) {
                _achievements.value = currentList.toMutableList().apply {
                    set(achIndex, ach.copy(isUnlocked = true))
                }
                // Save immediately
                saveGame()
            }
        }
    }

    // Formatting utility helper
    fun formatValue(value: Double): String {
        return when {
            value >= 1_000_000_000 -> String.format("%.2f B", value / 1_000_000_000.0)
            value >= 1_000_000 -> String.format("%.2f M", value / 1_000_000.0)
            value >= 1_000 -> String.format("%.1f K", value / 1_000.0)
            else -> String.format("%.0f", value)
        }
    }
}
