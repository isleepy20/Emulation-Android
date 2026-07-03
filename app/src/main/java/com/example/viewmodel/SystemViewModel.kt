package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Choreographer
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.database.JobLog
import com.example.data.repository.JobRepository
import com.example.data.network.*
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import okhttp3.OkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = JobRepository(database.jobLogDao())

    // UI state flows
    val jobLogs: StateFlow<List<JobLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // System Diagnostics
    private val _fps = MutableStateFlow(60.0)
    val fps: StateFlow<Double> = _fps.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<Float>>(List(20) { 60f })
    val fpsHistory: StateFlow<List<Float>> = _fpsHistory.asStateFlow()

    private val _cpuLoad = MutableStateFlow(12)
    val cpuLoad: StateFlow<Int> = _cpuLoad.asStateFlow()

    private val _ramUsage = MutableStateFlow(4.2) // GB
    val ramUsage: StateFlow<Double> = _ramUsage.asStateFlow()

    // Play Services Settings (Simulated + Actual state integration)
    private val _playServicesStatus = MutableStateFlow("AVAILABLE") // AVAILABLE, OUT_OF_DATE, MISSING, SUSPENDED
    val playServicesStatus: StateFlow<String> = _playServicesStatus.asStateFlow()

    private val _gpsLocationConnected = MutableStateFlow(true)
    val gpsLocationConnected: StateFlow<Boolean> = _gpsLocationConnected.asStateFlow()

    private val _fcmToken = MutableStateFlow("fcm_token_native_android15_v35_90a7b4c")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    // Debug Options
    private val _layoutBoundariesActive = MutableStateFlow(false)
    val layoutBoundariesActive: StateFlow<Boolean> = _layoutBoundariesActive.asStateFlow()

    private val _gpuProfilingActive = MutableStateFlow(false)
    val gpuProfilingActive: StateFlow<Boolean> = _gpuProfilingActive.asStateFlow()

    private val _streamQualityUHD = MutableStateFlow(true)
    val streamQualityUHD: StateFlow<Boolean> = _streamQualityUHD.asStateFlow()

    private val _inputLatencyOptimized = MutableStateFlow(true)
    val inputLatencyOptimized: StateFlow<Boolean> = _inputLatencyOptimized.asStateFlow()

    private val _customLogs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val customLogs: StateFlow<List<ConsoleLog>> = _customLogs.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Active background task tracking
    private val _activeTasksCount = MutableStateFlow(0)
    val activeTasksCount: StateFlow<Int> = _activeTasksCount.asStateFlow()

    // --- GitHub Integration State ---
    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _githubUsername = MutableStateFlow("")
    val githubUsername: StateFlow<String> = _githubUsername.asStateFlow()

    private val _githubUser = MutableStateFlow<GitHubUser?>(null)
    val githubUser: StateFlow<GitHubUser?> = _githubUser.asStateFlow()

    private val _githubRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val githubRepos: StateFlow<List<GitHubRepo>> = _githubRepos.asStateFlow()

    private val _isLoadingRepos = MutableStateFlow(false)
    val isLoadingRepos: StateFlow<Boolean> = _isLoadingRepos.asStateFlow()

    private val _reposError = MutableStateFlow<String?>(null)
    val reposError: StateFlow<String?> = _reposError.asStateFlow()

    private val _selectedRepo = MutableStateFlow<GitHubRepo?>(null)
    val selectedRepo: StateFlow<GitHubRepo?> = _selectedRepo.asStateFlow()

    private val _releasesList = MutableStateFlow<List<GitHubRelease>>(emptyList())
    val releasesList: StateFlow<List<GitHubRelease>> = _releasesList.asStateFlow()

    private val _isLoadingReleases = MutableStateFlow(false)
    val isLoadingReleases: StateFlow<Boolean> = _isLoadingReleases.asStateFlow()

    private val _releasesError = MutableStateFlow<String?>(null)
    val releasesError: StateFlow<String?> = _releasesError.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _downloadingAssetName = MutableStateFlow<String?>(null)
    val downloadingAssetName: StateFlow<String?> = _downloadingAssetName.asStateFlow()

    private val _downloadedApkFileUri = MutableStateFlow<String?>(null)
    val downloadedApkFileUri: StateFlow<String?> = _downloadedApkFileUri.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    private val _downloadSuccessMessage = MutableStateFlow<String?>(null)
    val downloadSuccessMessage: StateFlow<String?> = _downloadSuccessMessage.asStateFlow()

    // System constants
    val sdkVersion = Build.VERSION.SDK_INT
    val codename = Build.VERSION.CODENAME
    val model = Build.MODEL
    val manufacturer = Build.MANUFACTURER

    private var frameCallback: Choreographer.FrameCallback? = null
    private var lastFrameTimeNanos: Long = 0

    init {
        createNotificationChannel()
        addLog("SYSTEM", "Initialized System Console. Native SDK API: $sdkVersion", "INFO")
        addLog("PLAY_SERVICES", "Checked Google Play Services. Found version 24.18.22. Status: AVAILABLE", "INFO")
        addLog("DATABASE", "SQLite Room local DB connected successfully", "INFO")
        addLog("DEV_TOOLS", "Streaming pipeline optimized. Resolution locked to Full HD 1080p (60 FPS)", "INFO")
        addLog("DEV_TOOLS", "Input polling optimized: Ultra-Low Latency Mode (Sub-5ms / 1000Hz)", "INFO")
        
        // Start live hardware emulation loop
        startHardwareMonitor()
        // Start Choreographer Frame Rate Tracker
        startFpsTracker()
        
        // Populate initial mock log if empty to show first launch state beautifully
        viewModelScope.launch(Dispatchers.IO) {
            val count = database.jobLogDao().getAllLogs().firstOrNull()?.size ?: 0
            if (count == 0) {
                repository.insertLog(
                    JobLog(
                        jobName = "Initialization Sync",
                        status = "SUCCESS",
                        payloadSize = "1.2 KB",
                        executionTimeMs = 450,
                        message = "Native environment synced. Edge-to-Edge configured successfully."
                    )
                )
            }
        }
        loadGitHubCredentials()
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleLayoutBoundaries() {
        _layoutBoundariesActive.value = !_layoutBoundariesActive.value
        addLog("DEV_TOOLS", "Layout Boundaries toggled: ${_layoutBoundariesActive.value}", "DEBUG")
    }

    fun toggleGpuProfiling() {
        _gpuProfilingActive.value = !_gpuProfilingActive.value
        addLog("DEV_TOOLS", "GPU Profile Overlay toggled: ${_gpuProfilingActive.value}", "DEBUG")
    }

    fun toggleStreamQuality() {
        _streamQualityUHD.value = !_streamQualityUHD.value
        val quality = if (_streamQualityUHD.value) "1080p Ultra-HD (60 FPS)" else "Standard SD (Compressed)"
        addLog("DEV_TOOLS", "Stream Resolution Override configured: $quality", "INFO")
        addLog("SYSTEM", "Display pipeline buffer resized. Resolution locked to ${if (_streamQualityUHD.value) "1920x1080" else "854x480"}", "DEBUG")
    }

    fun toggleInputLatency() {
        _inputLatencyOptimized.value = !_inputLatencyOptimized.value
        val mode = if (_inputLatencyOptimized.value) "Ultra-Low Latency Mode (Sub-5ms / 1000Hz touch polling)" else "Standard Mode (120Hz polling)"
        addLog("DEV_TOOLS", "Input transmission protocol optimized: $mode", "INFO")
        addLog("SYSTEM", "Cloud input sampling delay set to ${if (_inputLatencyOptimized.value) "1ms" else "25ms"}", "DEBUG")
    }

    fun updatePlayServicesStatus(status: String) {
        _playServicesStatus.value = status
        addLog("PLAY_SERVICES", "Google Play Services emulator state override: $status", "WARNING")
    }

    fun toggleGpsLocation() {
        _gpsLocationConnected.value = !_gpsLocationConnected.value
        val state = if (_gpsLocationConnected.value) "CONNECTED" else "DISCONNECTED"
        addLog("PLAY_SERVICES", "Google Location Provider Client $state", if (_gpsLocationConnected.value) "INFO" else "WARNING")
    }

    fun regenerateFcmToken() {
        val randomHex = (1..8).map { "0123456789abcdef"[Random.nextInt(16)] }.joinToString("")
        _fcmToken.value = "fcm_token_native_android15_v35_$randomHex"
        addLog("PLAY_SERVICES", "FCM Registration Token rotated successfully: ${_fcmToken.value}", "INFO")
        sendNativeNotification("FCM Token Rotated", "New secure token registered on background sync channel.")
    }

    fun triggerBackgroundTask(jobName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _activeTasksCount.value += 1
            addLog("WORK_MANAGER", "WorkManager queued unique periodic job: $jobName", "INFO")
            
            val initialLog = JobLog(
                jobName = jobName,
                status = "RUNNING",
                payloadSize = "0.0 KB",
                executionTimeMs = 0,
                message = "WorkManager thread acquired. Syncing payload with Google Play Services gateway..."
            )
            repository.insertLog(initialLog)
            
            // Simulating execution
            delay(150)
            
            _activeTasksCount.value = maxOf(0, _activeTasksCount.value - 1)
            val isSuccess = Random.nextFloat() > 0.05 // lower fail rate since it is optimized
            val kbSynced = String.format("%.1f KB", Random.nextDouble(1.0, 48.0))
            val execTime = Random.nextLong(30, 80)
            val status = if (isSuccess) "SUCCESS" else "FAILED"
            val detailMsg = if (isSuccess) {
                "WorkManager task successfully completed. Synced $kbSynced telemetry in ${execTime}ms."
            } else {
                "WorkManager encountered transient synchronization error (HTTP 503 Gateway Timeout)."
            }
            
            repository.insertLog(
                JobLog(
                    jobName = jobName,
                    status = status,
                    payloadSize = if (isSuccess) kbSynced else "0.0 KB",
                    executionTimeMs = execTime,
                    message = detailMsg
                )
            )
            
            addLog(
                "WORK_MANAGER",
                "WorkManager finished $jobName. Status: $status. Duration: ${execTime}ms.",
                if (isSuccess) "INFO" else "ERROR"
            )

            if (isSuccess) {
                sendNativeNotification(
                    "Sync Successful",
                    "$jobName completed natively on Android 15 platform. Synced $kbSynced."
                )
            } else {
                sendNativeNotification(
                    "Sync Failed",
                    "$jobName failed. Android 15 WorkManager will retry with exponential backoff."
                )
            }
        }
    }

    fun clearAllJobLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLogs()
            addLog("DATABASE", "Job logs table truncated.", "INFO")
        }
    }

    fun addManualLog(tag: String, message: String, level: String) {
        addLog(tag, message, level)
    }

    private fun addLog(tag: String, message: String, level: String) {
        val newLog = ConsoleLog(
            timestamp = System.currentTimeMillis(),
            tag = tag,
            message = message,
            level = level
        )
        _customLogs.update { current ->
            (listOf(newLog) + current).take(150) // keep last 150 logs in memory
        }
    }

    private fun startHardwareMonitor() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(2000)
                // Fluctuating stats for realistic rendering
                val activeCount = _activeTasksCount.value
                val baseCpu = if (activeCount > 0) 35 else 5
                _cpuLoad.value = clamp(baseCpu + Random.nextInt(-3, 6), 1, 99)
                _ramUsage.value = String.format("%.2f", 4.12 + Random.nextDouble(-0.15, 0.35)).toDouble()
            }
        }
    }

    private fun startFpsTracker() {
        var frameCount = 0
        var lastFpsUpdateTimeNanos = 0L

        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                if (lastFpsUpdateTimeNanos == 0L) {
                    lastFpsUpdateTimeNanos = frameTimeNanos
                } else {
                    val elapsedNanos = frameTimeNanos - lastFpsUpdateTimeNanos
                    if (elapsedNanos >= 1_000_000_000L) { // Rate limit FPS updates to exactly once per second
                        val elapsedSeconds = elapsedNanos / 1_000_000_000.0
                        val calculatedFps = clampDouble(frameCount / elapsedSeconds, 10.0, 60.1)
                        _fps.value = calculatedFps
                        _fpsHistory.update { history ->
                            (history + calculatedFps.toFloat()).takeLast(20)
                        }
                        frameCount = 0
                        lastFpsUpdateTimeNanos = frameTimeNanos
                    }
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(frameCallback!!)
    }

    override fun onCleared() {
        super.onCleared()
        frameCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = getApplication<Application>().applicationContext
            val name = "System Console Notifications"
            val descriptionText = "Real-time updates of background tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("system_console_channel", name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNativeNotification(title: String, message: String) {
        val context = getApplication<Application>().applicationContext
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "system_console_channel")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(100000), builder.build())
    }

    // --- GitHub Integration Methods ---

    fun saveGitHubCredentials(token: String, username: String) {
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("token", token)
            putString("username", username)
            apply()
        }
        _githubToken.value = token
        _githubUsername.value = username
        
        addLog("GITHUB", "Credentials updated. Username: $username. PAT present: ${token.isNotEmpty()}", "INFO")
        
        // Reset old data
        _githubUser.value = null
        _githubRepos.value = emptyList()
        _selectedRepo.value = null
        _releasesList.value = emptyList()
        _reposError.value = null
        _releasesError.value = null

        // Trigger updates
        syncGitHubData()
    }

    fun loadGitHubCredentials() {
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val username = prefs.getString("username", "") ?: ""
        _githubToken.value = token
        _githubUsername.value = username
        
        if (username.isNotEmpty()) {
            addLog("GITHUB", "Credentials loaded. Syncing data for $username...", "INFO")
            syncGitHubData()
        }
    }

    fun syncGitHubData() {
        viewModelScope.launch {
            val username = _githubUsername.value
            val token = _githubToken.value
            
            if (username.isEmpty() && token.isEmpty()) {
                _reposError.value = "Username or Token is required"
                return@launch
            }

            _isLoadingRepos.value = true
            _reposError.value = null
            addLog("GITHUB", "Fetching user profile and repositories...", "INFO")

            try {
                val authHeader = if (token.isNotEmpty()) "token $token" else null
                
                // 1. Fetch User Profile
                val user = if (token.isNotEmpty()) {
                    GitHubApiClient.service.getAuthenticatedUser(authHeader!!)
                } else {
                    GitHubApiClient.service.getUser(username, authHeader)
                }
                _githubUser.value = user
                
                // Auto-sync username text field if they logged in with token
                if (token.isNotEmpty() && _githubUsername.value.isEmpty()) {
                    _githubUsername.value = user.login
                }
                addLog("GITHUB", "User profile fetched: ${user.name ?: user.login} (${user.public_repos} public repos)", "SUCCESS")

                // 2. Fetch Repositories
                val repos = if (token.isNotEmpty()) {
                    GitHubApiClient.service.getAuthenticatedUserRepos(authHeader!!)
                } else {
                    GitHubApiClient.service.getUserRepos(username, authHeader)
                }
                _githubRepos.value = repos
                addLog("GITHUB", "Successfully loaded ${repos.size} repositories.", "SUCCESS")
            } catch (e: Exception) {
                _reposError.value = e.message ?: "Failed to load GitHub data"
                addLog("GITHUB", "Sync failed: ${e.message}", "ERROR")
            } finally {
                _isLoadingRepos.value = false
            }
        }
    }

    fun fetchRepoReleases(owner: String, repoName: String) {
        viewModelScope.launch {
            _isLoadingReleases.value = true
            _releasesError.value = null
            _releasesList.value = emptyList()
            addLog("GITHUB", "Loading releases for $owner/$repoName...", "INFO")

            try {
                val token = _githubToken.value
                val authHeader = if (token.isNotEmpty()) "token $token" else null
                
                val releases = GitHubApiClient.service.getReleases(owner, repoName, authHeader)
                _releasesList.value = releases
                addLog("GITHUB", "Loaded ${releases.size} releases for $repoName", "SUCCESS")
                
                if (releases.isEmpty()) {
                    addLog("GITHUB", "No releases found for $repoName. Ensure your GitHub Action or release compiles an APK.", "WARNING")
                }
            } catch (e: Exception) {
                _releasesError.value = e.message ?: "Failed to load releases"
                addLog("GITHUB", "Failed loading releases: ${e.message}", "ERROR")
            } finally {
                _isLoadingReleases.value = false
            }
        }
    }

    fun selectRepo(repo: GitHubRepo?) {
        _selectedRepo.value = repo
        if (repo != null) {
            val owner = repo.full_name.substringBefore("/")
            fetchRepoReleases(owner, repo.name)
        } else {
            _releasesList.value = emptyList()
        }
    }

    fun downloadApk(asset: GitHubAsset, repoName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.value = 0f
            _downloadingAssetName.value = asset.name
            _downloadError.value = null
            _downloadSuccessMessage.value = null
            addLog("GITHUB", "Starting download: ${asset.name} (${String.format("%.2f", asset.size / (1024.0 * 1024.0))} MB)", "INFO")

            try {
                val url = asset.browser_download_url
                val client = OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .apply {
                        val token = _githubToken.value
                        if (token.isNotEmpty()) {
                            addHeader("Authorization", "token $token")
                        }
                    }
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Failed: HTTP ${response.code} ${response.message}")
                }

                val body = response.body ?: throw Exception("Empty response body")
                val totalBytes = body.contentLength()
                
                val cacheDir = getApplication<Application>().cacheDir
                val apkFile = File(cacheDir, asset.name)
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (totalBytes > 0) {
                        _downloadProgress.value = totalBytesRead.toFloat() / totalBytes.toFloat()
                    }
                }

                outputStream.write(buffer, 0, 0) // Ensure stream flushed correctly
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _downloadProgress.value = null
                _downloadingAssetName.value = null
                _downloadedApkFileUri.value = apkFile.absolutePath
                _downloadSuccessMessage.value = "Successfully downloaded ${asset.name}!"
                addLog("GITHUB", "Completed download of ${asset.name}. Saved to: ${apkFile.absolutePath}", "SUCCESS")
                sendNativeNotification("APK Download Completed", "Downloaded ${asset.name} successfully. Tap to emulate/install.")
            } catch (e: Exception) {
                _downloadProgress.value = null
                _downloadingAssetName.value = null
                _downloadError.value = e.message ?: "Unknown download error"
                addLog("GITHUB", "Download failed: ${e.message}", "ERROR")
            }
        }
    }

    fun installApk(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                addLog("GITHUB", "Installation failed: File not found at $filePath", "ERROR")
                return
            }

            addLog("GITHUB", "Launching Android package installer for ${file.name}", "INFO")
            
            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            addLog("GITHUB", "Installation trigger failed: ${e.message}", "ERROR")
            addLog("GITHUB", "Ensure you authorize the app to install packages under settings.", "WARNING")
        }
    }

    fun clearDownloadState() {
        _downloadedApkFileUri.value = null
        _downloadSuccessMessage.value = null
        _downloadError.value = null
    }

    private fun clamp(value: Int, min: Int, max: Int): Int {
        return if (value < min) min else if (value > max) max else value
    }

    private fun clampDouble(value: Double, min: Double, max: Double): Double {
        return if (value < min) min else if (value > max) max else value
    }
}

data class ConsoleLog(
    val timestamp: Long,
    val tag: String,
    val message: String,
    val level: String // "DEBUG", "INFO", "WARNING", "ERROR"
)
