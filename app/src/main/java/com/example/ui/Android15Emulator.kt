package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SystemViewModel
import com.example.viewmodel.ConsoleLog
import com.example.PlayServicesTab
import com.example.WorkManagerTab
import com.example.DevToolsTab
import com.example.GitHubTab
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Android15EmulatorView(viewModel: SystemViewModel) {
    val currentApp by viewModel.emulatedCurrentApp.collectAsState()
    val recentApps by viewModel.emulatedRecentApps.collectAsState()
    val wifiOn by viewModel.emulatedWifiOn.collectAsState()
    val bluetoothOn by viewModel.emulatedBluetoothOn.collectAsState()
    val darkModeOn by viewModel.emulatedDarkModeOn.collectAsState()
    val flashlightOn by viewModel.emulatedFlashlightOn.collectAsState()
    val batterySaverOn by viewModel.emulatedBatterySaverOn.collectAsState()
    val drawerOpen by viewModel.emulatedNotificationDrawerOpen.collectAsState()
    val recentsOpen by viewModel.emulatedRecentsOpen.collectAsState()

    // Base background colors based on emulated Dark Mode
    val emulatorThemeBackground = if (darkModeOn) Color(0xFF121212) else Color(0xFFF5F5F7)
    val emulatorThemeSurface = if (darkModeOn) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val emulatorThemeOnSurface = if (darkModeOn) Color(0xFFE3E3E3) else Color(0xFF1C1C1E)

    // Outer premium phone chassis container to prevent overlapping with physical device edges
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("emulator_outer_chassis"),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(4.dp, Color(0xFF2C2C2E)),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp) // Simulated bezel
                .clip(RoundedCornerShape(28.dp))
                .background(emulatorThemeBackground)
        ) {
            // 1. Android 16 "Baklava" Premium Material You Wallpaper Brush
            val wallpaperBrush = remember(darkModeOn) {
            if (darkModeOn) {
                Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF3E2723), // Deep Honey-Brown
                        Color(0xFF1B5E20), // Deep Pistachio Green
                        Color(0xFF4A148C), // Royal Baklava Plum
                        Color(0xFF006064), // Mystic Turquoise
                        Color(0xFF3E2723)
                    )
                )
            } else {
                Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFFECB3), // Sweet Honey Gold
                        Color(0xFFC8E6C9), // Light Pistachio
                        Color(0xFFE1BEE7), // Soft Lavender-Rose
                        Color(0xFFB2EBF2), // Pale Cyan
                        Color(0xFFFFECB3)
                    )
                )
            }
        }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(wallpaperBrush)
            )

            // 2. Emulated Screen Area
            Column(modifier = Modifier.fillMaxSize()) {
                // Emulated Status Bar
                EmulatedStatusBar(
                    wifiOn = wifiOn,
                    bluetoothOn = bluetoothOn,
                    batterySaverOn = batterySaverOn,
                    darkModeOn = darkModeOn,
                    onStatusTap = { viewModel.setEmulatedNotificationDrawerOpen(true) }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Check if overview screen is active
                    if (recentsOpen) {
                        EmulatedRecentsScreen(
                            viewModel = viewModel,
                            recentApps = recentApps,
                            currentApp = currentApp,
                            darkModeOn = darkModeOn,
                            onClose = { viewModel.setEmulatedRecentsOpen(false) }
                        )
                    } else {
                        // Switch content based on active app
                        AnimatedContent(
                            targetState = currentApp,
                            transitionSpec = {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            },
                            label = "EmulatorAppContainer"
                        ) { app ->
                            when (app) {
                                null -> EmulatedHomeScreen(viewModel, darkModeOn)
                                "Diagnostics" -> EmulatedDiagnosticsApp(viewModel, darkModeOn)
                                "Settings" -> EmulatedSettingsApp(viewModel, darkModeOn)
                                "Chrome" -> EmulatedChromeApp(viewModel, darkModeOn)
                                "GitHub" -> EmulatedGitHubApp(viewModel, darkModeOn)
                                "Play Store" -> EmulatedPlayStoreApp(viewModel, darkModeOn)
                                "Fitbit Tracker" -> EmulatedFitbitApp(darkModeOn)
                                "Spotify" -> EmulatedSpotifyApp(darkModeOn)
                                "Chess 3D" -> EmulatedChessApp(darkModeOn)
                                "Play Services Status" -> EmulatedPlayServicesApp(viewModel, darkModeOn)
                                "WorkManager Logs" -> EmulatedWorkManagerApp(viewModel, darkModeOn)
                                "Dev Toggles" -> EmulatedDevConsoleApp(viewModel, darkModeOn)
                                "Dialer" -> EmulatedPhoneApp(viewModel, darkModeOn)
                                "Messages" -> EmulatedMessagesApp(viewModel, darkModeOn)
                                "Camera" -> EmulatedCameraApp(viewModel, darkModeOn)
                                "Calculator" -> EmulatedCalculatorApp(viewModel, darkModeOn)
                                "Clock" -> EmulatedClockApp(viewModel, darkModeOn)
                                "Gemini AI" -> EmulatedGeminiApp(darkModeOn)
                                "EasterEgg" -> EmulatedEasterEggView(darkModeOn)
                                else -> Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("App failed to launch", color = emulatorThemeOnSurface)
                                }
                            }
                        }
                    }

                    // Sliding Notification Drawer Overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = drawerOpen,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                        ) + fadeOut()
                    ) {
                        EmulatedNotificationDrawer(
                            viewModel = viewModel,
                            wifiOn = wifiOn,
                            bluetoothOn = bluetoothOn,
                            darkModeOn = darkModeOn,
                            flashlightOn = flashlightOn,
                            batterySaverOn = batterySaverOn,
                            onClose = { viewModel.setEmulatedNotificationDrawerOpen(false) }
                        )
                    }
                }

                // Virtual bottom System Navigation buttons (Back, Home, Recents)
                EmulatedNavigationBar(
                    onBack = {
                        if (drawerOpen) {
                            viewModel.setEmulatedNotificationDrawerOpen(false)
                        } else if (recentsOpen) {
                            viewModel.setEmulatedRecentsOpen(false)
                        } else if (currentApp != null) {
                            viewModel.setEmulatedCurrentApp(null)
                        }
                    },
                    onHome = {
                        viewModel.setEmulatedNotificationDrawerOpen(false)
                        viewModel.setEmulatedRecentsOpen(false)
                        viewModel.setEmulatedCurrentApp(null)
                    },
                    onRecents = {
                        viewModel.setEmulatedNotificationDrawerOpen(false)
                        viewModel.setEmulatedRecentsOpen(!recentsOpen)
                    },
                    darkModeOn = darkModeOn
                )
            }

            // Simulated camera notch cutout
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Black, CircleShape)
                    .align(Alignment.TopCenter)
                    .offset(y = 2.dp)
            )
        }
    }
}

@Composable
fun EmulatedStatusBar(
    wifiOn: Boolean,
    bluetoothOn: Boolean,
    batterySaverOn: Boolean,
    darkModeOn: Boolean,
    onStatusTap: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeString = remember { formatter.format(Date()) }
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable { onStatusTap() }
            .padding(horizontal = 14.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeString,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (wifiOn) Icon(Icons.Default.Wifi, "Wi-Fi", tint = textColor, modifier = Modifier.size(12.dp))
            if (bluetoothOn) Icon(Icons.Default.Bluetooth, "Bluetooth", tint = textColor, modifier = Modifier.size(12.dp))
            Icon(Icons.Default.SignalCellularAlt, "Cellular", tint = textColor, modifier = Modifier.size(12.dp))
            if (batterySaverOn) {
                Icon(Icons.Default.BatterySaver, "Battery Saver On", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
            } else {
                Icon(Icons.Default.BatteryFull, "Battery 100%", tint = Color(0xFF66BB6A), modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun EmulatedNavigationBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit,
    darkModeOn: Boolean
) {
    val pillColor = if (darkModeOn) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color.Transparent)
            // Gesture interactions (simplified simulation for click regions)
            .clickable { onHome() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onBack() })
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onHome() })
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onRecents() })
        }
        
        // Gesture Pill (One UI Style)
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .width(100.dp)
                .height(3.dp)
                .background(pillColor, RoundedCornerShape(1.5.dp))
                .testTag("emulated_nav_gesture_pill")
        )
    }
}

@Composable
fun EmulatedHomeScreen(viewModel: SystemViewModel, darkModeOn: Boolean) {
    val installedApps by viewModel.emulatedInstalledApps.collectAsState()
    val dateString = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Column: One UI Style Weather & Clock Widget
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val formatter = remember { SimpleDateFormat("h:mm", Locale.getDefault()) }
            var currentTime by remember { mutableStateOf(formatter.format(Date())) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    currentTime = formatter.format(Date())
                }
            }

            // Samsung One UI 7 Style Weather Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Clock & Date
                    Column {
                        Text(
                            text = currentTime,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                        Text(
                            text = dateString,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Right side: Weather
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Cloud, contentDescription = "Weather", tint = Color.White, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "72°",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "San Francisco",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Mid Column: Launcher Apps Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(installedApps) { app ->
                    val icon = when (app) {
                        "Diagnostics" -> Icons.Default.Analytics
                        "Settings" -> Icons.Default.Settings
                        "Chrome" -> Icons.Default.Language
                        "GitHub" -> Icons.Default.Code
                        "Play Store" -> Icons.Default.ShopTwo
                        "Fitbit Tracker" -> Icons.Default.DirectionsRun
                        "Spotify" -> Icons.Default.MusicNote
                        "Chess 3D" -> Icons.Default.GridOn
                        "Play Services Status" -> Icons.Default.Sync
                        "WorkManager Logs" -> Icons.Default.History
                        "Dev Toggles" -> Icons.Default.DeveloperMode
                        "Dialer" -> Icons.Default.Phone
                        "Messages" -> Icons.Default.ChatBubble
                        "Camera" -> Icons.Default.PhotoCamera
                        "Calculator" -> Icons.Default.Calculate
                        "Clock" -> Icons.Default.AccessTime
                        "Gemini AI" -> Icons.Default.AutoAwesome
                        else -> Icons.Default.Android
                    }

                    val color = when (app) {
                        "Diagnostics" -> Color(0xFF33B5E5)
                        "Settings" -> Color(0xFF9E9E9E)
                        "Chrome" -> Color(0xFFFBC02D)
                        "GitHub" -> Color(0xFF24292E)
                        "Play Store" -> Color(0xFF00C853)
                        "Fitbit Tracker" -> Color(0xFF00BFA5)
                        "Spotify" -> Color(0xFF1DB954)
                        "Chess 3D" -> Color(0xFFFF5722)
                        "Play Services Status" -> Color(0xFF3F51B5)
                        "WorkManager Logs" -> Color(0xFFE91E63)
                        "Dev Toggles" -> Color(0xFF009688)
                        "Dialer" -> Color(0xFF4CAF50)
                        "Messages" -> Color(0xFF03A9F4)
                        "Camera" -> Color(0xFF9C27B0)
                        "Calculator" -> Color(0xFFFF9800)
                        "Clock" -> Color(0xFF673AB7)
                        "Gemini AI" -> Color(0xFF00E5FF)
                        else -> Color(0xFF8BC34A)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.setEmulatedCurrentApp(app) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(color, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = app, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom Column: Dock Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIcon(Icons.Default.Language, "Chrome") { viewModel.setEmulatedCurrentApp("Chrome") }
                DockIcon(Icons.Default.Settings, "Settings") { viewModel.setEmulatedCurrentApp("Settings") }
                DockIcon(Icons.Default.Analytics, "Diagnostics") { viewModel.setEmulatedCurrentApp("Diagnostics") }
                DockIcon(Icons.Default.ShopTwo, "Play Store") { viewModel.setEmulatedCurrentApp("Play Store") }
            }
        }
    }
}

@Composable
fun DockIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun EmulatedDiagnosticsApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(14.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Analytics, "Stats", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text("System Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("CPU LOAD EMULATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$cpuLoad%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (darkModeOn) Color.White else Color.Black)
                            Text("Core: ARMv8-A", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { cpuLoad / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("RAM USE EMULATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(String.format("%.2f GB", ramUsage), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (darkModeOn) Color.White else Color.Black)
                            Text("Allocated: 8.00 GB", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (ramUsage / 8.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("LIVE REFRESH RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("TARGET: 60Hz", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66BB6A))
                        }
                        Text(String.format("%.1f FPS", fps), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF66BB6A))
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Mini FPS Graph inside emulator!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(if (darkModeOn) Color.Black else Color.White)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val spacing = w / (fpsHistory.size - 1)
                                val path = Path()

                                fpsHistory.forEachIndexed { i, value ->
                                    val norm = (value - 30f) / 30f
                                    val x = i * spacing
                                    val y = h - (norm * h)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path = path, color = Color(0xFF66BB6A), style = Stroke(width = 2.dp.toPx()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedSettingsApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    val section by viewModel.emulatedSettingsSection.collectAsState()
    val boundariesActive by viewModel.layoutBoundariesActive.collectAsState()
    val gpuProfilingActive by viewModel.gpuProfilingActive.collectAsState()
    val inputLatencyOptimized by viewModel.inputLatencyOptimized.collectAsState()
    val wifiOn by viewModel.emulatedWifiOn.collectAsState()
    val bluetoothOn by viewModel.emulatedBluetoothOn.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(14.dp)
    ) {
        // Settings App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (section != "Main") {
                    IconButton(onClick = { viewModel.setEmulatedSettingsSection("Main") }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = if (darkModeOn) Color.White else Color.Black)
                    }
                } else {
                    Icon(Icons.Default.Settings, "Gear", tint = Color.Gray, modifier = Modifier.size(24.dp))
                }
                Text(
                    text = if (section == "Main") "Settings" else section,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (darkModeOn) Color.White else Color.Black
                )
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (section) {
            "Main" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    item {
                        SettingsRow(Icons.Default.Wifi, "Network & Internet", "Wi-Fi, Mobile, Hotspot") {
                            viewModel.setEmulatedSettingsSection("Network & Internet")
                        }
                    }
                    item {
                        SettingsRow(Icons.Default.Devices, "Connected Devices", "Bluetooth, Cast") {
                            viewModel.setEmulatedSettingsSection("Connected Devices")
                        }
                    }
                    item {
                        SettingsRow(Icons.Default.DeveloperMode, "System Developer Options", "Layout, GPU Profiling, Insets") {
                            viewModel.setEmulatedSettingsSection("Developer Options")
                        }
                    }
                    item {
                        SettingsRow(Icons.Default.Info, "About Emulated Phone", "Android 16 Baklava Preview") {
                            viewModel.setEmulatedSettingsSection("About Emulated Phone")
                        }
                    }
                }
            }

            "Network & Internet" -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Wi-Fi Emulated", color = if (darkModeOn) Color.White else Color.Black)
                        Switch(checked = wifiOn, onCheckedChange = { viewModel.toggleEmulatedWifi() })
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Carrier", color = if (darkModeOn) Color.White else Color.Black)
                        Text("Google Fi (Simulated)", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "Connected Devices" -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Bluetooth Emulated", color = if (darkModeOn) Color.White else Color.Black)
                        Switch(checked = bluetoothOn, onCheckedChange = { viewModel.toggleEmulatedBluetooth() })
                    }
                }
            }

            "Developer Options" -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Layout Boundaries", color = if (darkModeOn) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Renders boundary vectors globally", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(checked = boundariesActive, onCheckedChange = { viewModel.toggleLayoutBoundaries() })
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Profile GPU Rendering Overlay", color = if (darkModeOn) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Overlays performance stacked bars", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(checked = gpuProfilingActive, onCheckedChange = { viewModel.toggleGpuProfiling() })
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ultra-Low Input Latency", color = if (darkModeOn) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Enables 1000Hz cloud screen polling", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(checked = inputLatencyOptimized, onCheckedChange = { viewModel.toggleInputLatency() })
                    }
                }
            }

            "About Emulated Phone" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var angle by remember { mutableStateOf(0f) }
                    val animatedAngle by animateFloatAsState(targetValue = angle, animationSpec = spring())

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFFFECB3), CircleShape) // Warm Honey Yellow
                            .rotate(animatedAngle)
                            .clickable {
                                angle += 120f
                                viewModel.setEmulatedCurrentApp("EasterEgg")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Android, "Baklava", tint = Color(0xFFE65100), modifier = Modifier.size(60.dp))
                    }

                    Text("Android 16 Easter Egg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (darkModeOn) Color.White else Color.Black)
                    Text("💡 Tap the Android head to launch the emulated space traveler Easter Egg!", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SpecLine("Model", "Emulated Google Pixel 10 Pro")
                            SpecLine("Android Version", "Android 16 (API 36)")
                            SpecLine("Build Code", "BA1.2604")
                            SpecLine("Kernel", "Baklava v6.6-android16")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.Gray.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Open", modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}

@Composable
fun SpecLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmulatedChromeApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var urlInput by remember { mutableStateOf("https://google.com") }
    var searchQuery by remember { mutableStateOf("") }
    var searched by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
    ) {
        // Chrome Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFEFEFEF))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Home, "Home", tint = if (darkModeOn) Color.White else Color.Black)
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (darkModeOn) Color.Black else Color.White,
                    unfocusedContainerColor = if (darkModeOn) Color.Black else Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    keyboardController?.hide()
                    searched = false
                })
            )
        }

        Spacer(modifier = Modifier.height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))

        // Browser Screen Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkModeOn) Color(0xFF121212) else Color(0xFFF9F9F9))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (urlInput.contains("google.com")) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Google", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search simulated Google...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                keyboardController?.hide()
                                searched = searchQuery.isNotEmpty()
                            }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                        }
                    )

                    if (searched) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            item {
                                GoogleResult(
                                    title = "Google AI Studio Developer App",
                                    snippet = "Development app URL loaded: https://ais-dev-jwh27wjvhdyjhds755jnff-167799279054.us-east1.run.app"
                                ) {
                                    urlInput = "https://ais-dev-jwh27wjvhdyjhds755jnff-167799279054.us-east1.run.app"
                                }
                            }
                            item {
                                GoogleResult(
                                    title = "Android 15 Native SDK & API Level 35 Documentation",
                                    snippet = "Learn about edge-to-edge screens, layout bounds, predictive back, and Google Play Services integration in Android 15."
                                ) {}
                            }
                            item {
                                GoogleResult(
                                    title = "GitHub - isleepy20 compiled assets release channel",
                                    snippet = "Direct download mirror for the verified android-15.apk compiled build. Trigger emulator sync."
                                ) {
                                    viewModel.setEmulatedCurrentApp("GitHub")
                                }
                            }
                        }
                    }
                }
            } else if (urlInput.contains("ais-dev") || urlInput.contains("ais-pre") || urlInput.contains("run.app")) {
                // Emulate our application's website inside the emulator browser! (Highly creative!)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("System Monitor Web Interface", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Current Build Status: COMPLED (android-15.apk output successfully loaded in cache)", fontSize = 11.sp)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Download Latest Compilation Build", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    viewModel.setEmulatedCurrentApp("GitHub")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, "Download")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pull android-15.apk via GitHub")
                            }
                        }
                    }
                }
            } else {
                // Generic Web App Simulation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Default.Language, "Web", tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Successfully connected to emulated web servers!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Mock address: $urlInput", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun GoogleResult(title: String, snippet: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color(0xFF33B5E5), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(snippet, color = Color.LightGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun EmulatedGitHubApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val githubUser by viewModel.githubUser.collectAsState()
    val githubRepos by viewModel.githubRepos.collectAsState()
    val isLoadingRepos by viewModel.isLoadingRepos.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadedApkFileUri by viewModel.downloadedApkFileUri.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Code, "GitHub", tint = Color.White, modifier = Modifier.size(24.dp))
                Text("GitHub Mobile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (githubUser == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CloudOff, "Offline", tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Account Not Synced", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Please authenticate via the 'GitHub' main navigation tab first to load repositories inside the emulator.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Text(githubUser!!.name?.take(1)?.uppercase() ?: githubUser!!.login.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(githubUser!!.name ?: githubUser!!.login, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("@${githubUser!!.login}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                if (downloadProgress != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Downloading android-15.apk...", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                LinearProgressIndicator(progress = { downloadProgress!! }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                if (downloadedApkFileUri != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("android-15.apk is ready!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Button(
                                    onClick = {
                                        viewModel.installAppInEmulator("Diagnostics")
                                        viewModel.setEmulatedCurrentApp("Diagnostics")
                                        viewModel.addManualLog("EMULATOR", "Emulator loaded compiled android-15.apk safely", "SUCCESS")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Run")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Emulate Compiled APK")
                                }
                            }
                        }
                    }
                }

                item {
                    Text("ACTIVE REPOSITORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                if (githubRepos.isEmpty()) {
                    item {
                        Text("No repositories found.", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    val firstRepo = githubRepos.first()
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(firstRepo.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(firstRepo.description ?: "Android 15 compilation pipeline", fontSize = 11.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("⭐ ${firstRepo.stargazers_count}", fontSize = 10.sp, color = Color.Gray)
                                    Text("🍴 ${firstRepo.forks_count}", fontSize = 10.sp, color = Color.Gray)
                                }
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("android-15.apk", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Button(
                                        onClick = {
                                            viewModel.addManualLog("EMULATOR", "User triggered pull compilation inside emulator", "INFO")
                                            coroutineScope.launch {
                                                // Simulated pull download
                                                (0..10).forEach { i ->
                                                    viewModel.addManualLog("EMULATOR", "Fetching chunk $i...", "DEBUG")
                                                    delay(100)
                                                }
                                                viewModel.installAppInEmulator("Diagnostics")
                                                viewModel.setEmulatedCurrentApp("Diagnostics")
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Pull & Install", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedPlayStoreApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val installedApps by viewModel.emulatedInstalledApps.collectAsState()
    var installingApp by remember { mutableStateOf<String?>(null) }
    var installProgress by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(12.dp)
    ) {
        // Play Store Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ShopTwo, "Play Store", tint = Color(0xFF00C853), modifier = Modifier.size(24.dp))
                Text("Google Play", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (installingApp != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Installing $installingApp...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LinearProgressIndicator(progress = { installProgress }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF00C853))
                    Text("${String.format("%.0f", installProgress * 100)}% Completed", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text("RECOMMENDED APPLICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        val catalog = listOf(
            Triple("Fitbit Tracker", "Activity, sleep, and fitness stats.", Icons.Default.DirectionsRun),
            Triple("Spotify", "Listen to millions of lo-fi music tracks.", Icons.Default.MusicNote),
            Triple("Chess 3D", "Premium virtual wooden board simulation.", Icons.Default.GridOn)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(catalog) { (name, desc, icon) ->
                val isInstalled = installedApps.contains(name)

                Card(
                    colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(icon, name, tint = Color(0xFF00C853), modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(desc, fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (!isInstalled) {
                                    installingApp = name
                                    installProgress = 0f
                                    coroutineScope.launch {
                                        while (installProgress < 1f) {
                                            delay(100)
                                            installProgress += 0.1f
                                        }
                                        viewModel.installAppInEmulator(name)
                                        installingApp = null
                                    }
                                } else {
                                    viewModel.setEmulatedCurrentApp(name)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInstalled) MaterialTheme.colorScheme.secondary else Color(0xFF00C853)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text(if (isInstalled) "Open" else "Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedFitbitApp(darkModeOn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F1B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Fitbit", color = Color(0xFF00BFA5), fontWeight = FontWeight.Black, fontSize = 20.sp)
            Icon(Icons.Default.DirectionsRun, "Run", tint = Color(0xFF00BFA5))
        }

        // Steps Gauge
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .border(4.dp, Color(0xFF00BFA5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("STEPS", fontSize = 11.sp, color = Color.Gray)
                Text("8,421", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Goal: 10,000", fontSize = 10.sp, color = Color.LightGray)
            }
        }

        // Fitbit Telemetry Stats Rows
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, "Heart", tint = Color.Red, modifier = Modifier.size(16.dp))
                    Text("72 BPM", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Resting", fontSize = 9.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalFireDepartment, "Cal", tint = Color(0xFFFF9100), modifier = Modifier.size(16.dp))
                    Text("1,842 kcal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Burned", fontSize = 9.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Timer, "Active", tint = Color(0xFF00BFA5), modifier = Modifier.size(16.dp))
                    Text("42 Min", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Zone Min", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmulatedSpotifyApp(darkModeOn: Boolean) {
    var playing by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(42) }
    val totalSeconds = 210 // 3:30

    LaunchedEffect(playing) {
        if (playing) {
            while (elapsedSeconds < totalSeconds) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MusicNote, "Spotify", tint = Color(0xFF1DB954), modifier = Modifier.size(24.dp))
            Text("SPOTIFY RETRO", color = Color(0xFF1DB954), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Icon(Icons.Default.Share, "Share", tint = Color.LightGray)
        }

        // Album Art
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF388E3C), Color(0xFF1B5E20))
                    ),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Album, "Album", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(80.dp))
        }

        // Title/Artist
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cosmic Waves - Lo-Fi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text("Android 15 Soundscape", fontSize = 12.sp, color = Color.Gray)
        }

        // Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            val progress = elapsedSeconds.toFloat() / totalSeconds.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1DB954)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${elapsedSeconds / 60}:${String.format("%02d", elapsedSeconds % 60)}", fontSize = 10.sp, color = Color.Gray)
                Text("${totalSeconds / 60}:${totalSeconds % 60}", fontSize = 10.sp, color = Color.Gray)
            }
        }

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { elapsedSeconds = maxOf(0, elapsedSeconds - 10) }) {
                Icon(Icons.Default.Replay10, "Rewind", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            IconButton(
                onClick = { playing = !playing },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { elapsedSeconds = minOf(totalSeconds, elapsedSeconds + 10) }) {
                Icon(Icons.Default.Forward10, "Forward", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun EmulatedChessApp(darkModeOn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Woodland Chess 3D", color = Color(0xFFD7CCC8), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Icon(Icons.Default.GridOn, "Chess", tint = Color(0xFFD7CCC8))
        }

        // Draw interactive visual chess canvas grid
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFF4E342E), RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / 8f
                for (row in 0..7) {
                    for (col in 0..7) {
                        val isDark = (row + col) % 2 == 1
                        val color = if (isDark) Color(0xFF5D4037) else Color(0xFFD7CCC8)
                        drawRect(
                            color = color,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                    }
                }

                // Draw mock pawns
                drawCircle(Color.Black, radius = 6.dp.toPx(), center = Offset(cellSize * 1.5f, cellSize * 1.5f))
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(cellSize * 3.5f, cellSize * 6.5f))
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), modifier = Modifier.fillMaxWidth()) {
            Text(
                "Emulation Status: Wooden 3D Chess engine running smoothly on Android 15 runtime thread pool.",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun EmulatedEasterEggView(darkModeOn: Boolean) {
    var rocketOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rocketOffset = Offset(
                        x = clampFloat(rocketOffset.x + dragAmount.x, -200f, 200f),
                        y = clampFloat(rocketOffset.y + dragAmount.y, -300f, 300f)
                    )
                }
            }
    ) {
        // Space Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw starfield stars
            val rand = java.util.Random(42)
            for (i in 0..60) {
                val x = rand.nextFloat() * size.width
                val y = rand.nextFloat() * size.height
                val radius = rand.nextFloat() * 2.5f + 1f
                drawCircle(Color.White.copy(alpha = rand.nextFloat() * 0.8f + 0.2f), radius, Offset(x, y))
            }

            // Draw vanilla space planet
            drawCircle(
                color = Color(0xFF00ACC1),
                radius = 35.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
        }

        // Astronaut / Rocket visual
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = rocketOffset.x.dp, y = rocketOffset.y.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.RocketLaunch, "Rocket ship", tint = Color.Red, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text("Android 15 Voyager", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Drag to navigate!", color = Color.Yellow, fontSize = 9.sp)
        }
    }
}

@Composable
fun EmulatedNotificationDrawer(
    viewModel: SystemViewModel,
    wifiOn: Boolean,
    bluetoothOn: Boolean,
    darkModeOn: Boolean,
    flashlightOn: Boolean,
    batterySaverOn: Boolean,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.95f)),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Settings", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.KeyboardArrowUp, "Collapse", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggles Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(name = "Wi-Fi", icon = Icons.Default.Wifi, active = wifiOn, modifier = Modifier.weight(1f)) {
                        viewModel.toggleEmulatedWifi()
                    }
                    QuickTile(name = "Bluetooth", icon = Icons.Default.Bluetooth, active = bluetoothOn, modifier = Modifier.weight(1f)) {
                        viewModel.toggleEmulatedBluetooth()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(name = "Dark Mode", icon = Icons.Default.DarkMode, active = darkModeOn, modifier = Modifier.weight(1f)) {
                        viewModel.toggleEmulatedDarkMode()
                    }
                    QuickTile(name = "Flashlight", icon = Icons.Default.FlashlightOn, active = flashlightOn, modifier = Modifier.weight(1f)) {
                        viewModel.toggleEmulatedFlashlight()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(name = "Battery Saver", icon = Icons.Default.BatterySaver, active = batterySaverOn, modifier = Modifier.weight(1f)) {
                        viewModel.toggleEmulatedBatterySaver()
                    }
                    QuickTile(name = "Cast Screen", icon = Icons.Default.Cast, active = false, modifier = Modifier.weight(1f)) {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("NOTIFICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                NotificationItem("Android 15 System Engine", "Emulation sandbox running natively on ThreadPool executor.", "12:00 PM")
                Spacer(modifier = Modifier.height(6.dp))
                NotificationItem("Google Play Services", "Rotated FCM Token registered successfully under secure channel.", "12:15 PM")
            }

            // Bottom swipe-up indicator bar
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(Color.Gray, CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .clickable { onClose() }
            )
        }
    }
}

@Composable
fun QuickTile(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, name, tint = if (active) MaterialTheme.colorScheme.onPrimary else Color.White, modifier = Modifier.size(20.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (active) MaterialTheme.colorScheme.onPrimary else Color.White)
                Text(if (active) "On" else "Off", fontSize = 9.sp, color = if (active) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else Color.Gray)
            }
        }
    }
}

@Composable
fun NotificationItem(title: String, body: String, time: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Android, "Notif", tint = Color.Green, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    Text(time, color = Color.Gray, fontSize = 9.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(body, color = Color.LightGray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun EmulatedRecentsScreen(
    viewModel: SystemViewModel,
    recentApps: List<String>,
    currentApp: String?,
    darkModeOn: Boolean,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF121212) else Color(0xFFF5F5F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recents Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (darkModeOn) Color.White else Color.Black
        )

        if (recentApps.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No active apps in recents", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            // Horizontal scrollable cards of open apps! Matches second screenshot!
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                recentApps.forEach { appName ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .fillMaxHeight()
                            .border(
                                1.dp,
                                if (currentApp == appName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Android, "App", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text(appName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.removeEmulatedRecentApp(appName) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Dismiss", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }

                            // Simulated app body card preview
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("[ Preview ]", fontSize = 10.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    viewModel.setEmulatedCurrentApp(appName)
                                    viewModel.setEmulatedRecentsOpen(false)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Resume", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.clearEmulatedRecents() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Close all")
            }
            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Back to Home", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmulatedPlayServicesApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Sync, "Play Services", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                Text(
                    "Google Play Services Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        PlayServicesTab(viewModel)
    }
}

@Composable
fun EmulatedWorkManagerApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.History, "WorkManager", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                Text(
                    "WorkManager Tasks Queue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        WorkManagerTab(viewModel)
    }
}

@Composable
fun EmulatedDevConsoleApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DeveloperMode, "Dev Toggles", tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                Text(
                    "Developer Controls Panel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
        DevToolsTab(viewModel)
    }
}

@Composable
fun EmulatedPhoneApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var dialString by remember { mutableStateOf("") }
    var calling by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(calling) {
        if (calling) {
            callSeconds = 0
            while (calling) {
                delay(1000)
                callSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1A1A1C) else Color(0xFFF9F9FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Phone, "Phone", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                Text("Dialer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        if (!calling) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = dialString.ifEmpty { "Enter number..." },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = if (dialString.isEmpty()) Color.Gray else (if (darkModeOn) Color.White else Color.Black),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                val buttons = listOf(
                    "1", "2", "3",
                    "4", "5", "6",
                    "7", "8", "9",
                    "*", "0", "#"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            for (col in 0..2) {
                                val digit = buttons[row * 3 + col]
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFE2E2E6), CircleShape)
                                        .clickable { dialString += digit }
                                        .testTag("dialer_btn_$digit"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(digit, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = if (darkModeOn) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dialString.isNotEmpty()) {
                        IconButton(onClick = { dialString = dialString.dropLast(1) }) {
                            Icon(Icons.Default.Backspace, "Backspace", tint = Color.Gray, modifier = Modifier.size(24.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .clickable {
                                if (dialString.isNotEmpty()) {
                                    calling = true
                                    viewModel.addManualLog("PHONE", "Dialing emulated call to $dialString", "INFO")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, "Dial", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, "Contact", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(64.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(dialString, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (darkModeOn) Color.White else Color.Black)
                Text("Calling...", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("%02d:%02d", callSeconds / 60, callSeconds % 60),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (darkModeOn) Color.LightGray else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(64.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Red, CircleShape)
                        .clickable { calling = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun EmulatedMessagesApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var selectedContact by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }
    
    val messagesMap = remember {
        mutableStateMapOf(
            "Android 16 Bot" to mutableStateListOf(
                "Bot" to "Welcome to the Android 16 (Baklava) Simulator!",
                "Bot" to "Did you know Android 16 is codenamed Baklava? It is the latest platform version featuring API level 36.",
                "Bot" to "Ask me anything about the performance features or SDKs!"
            ),
            "System Daemon" to mutableStateListOf(
                "Bot" to "System daemon initialization completed.",
                "Bot" to "CPU and RAM resources are stable. All micro-tasks are running smoothly."
            ),
            "Google Play Agent" to mutableStateListOf(
                "Bot" to "Google Play Services emulator is active and ready.",
                "Bot" to "FCM push token registered securely. GPS location simulated."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedContact != null) {
                    IconButton(onClick = { selectedContact = null }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = if (darkModeOn) Color.White else Color.Black)
                    }
                } else {
                    Icon(Icons.Default.ChatBubble, "Messages", tint = Color(0xFF03A9F4), modifier = Modifier.size(24.dp))
                }
                Text(
                    text = selectedContact ?: "Messages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (darkModeOn) Color.White else Color.Black
                )
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedContact == null) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                val contacts = messagesMap.keys.toList()
                items(contacts) { contact ->
                    val list = messagesMap[contact]
                    val lastMsg = list?.lastOrNull()?.second ?: ""
                    Card(
                        onClick = { selectedContact = contact },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).background(Color(0xFF03A9F4), CircleShape), contentAlignment = Alignment.Center) {
                                Text(contact.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (darkModeOn) Color.White else Color.Black)
                                Text(lastMsg, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        } else {
            val chatHistory = messagesMap[selectedContact!!]!!
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(chatHistory) { (sender, content) ->
                        val isUser = sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) Color(0xFF03A9F4) else (if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFE2E2E6))
                                ),
                                modifier = Modifier.widthIn(max = 220.dp)
                            ) {
                                Text(
                                    text = content,
                                    fontSize = 12.sp,
                                    color = if (isUser) Color.White else (if (darkModeOn) Color.White else Color.Black),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Text message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (textInput.isNotEmpty()) {
                                val userText = textInput
                                textInput = ""
                                chatHistory.add("User" to userText)
                                viewModel.addManualLog("CHAT", "User sent message to $selectedContact", "INFO")
                                
                                val response = when {
                                    userText.contains("android 16", ignoreCase = true) || userText.contains("baklava", ignoreCase = true) -> {
                                        "Android 16 is codenamed Baklava! It introduces key kernel upgrades, ultra-low input latency pipelines, and deeper system integration for Gemini LLMs."
                                    }
                                    userText.contains("help", ignoreCase = true) || userText.contains("options", ignoreCase = true) -> {
                                        "You can test different settings by loading 'System Developer Options' under settings to inspect GPU threads and toggle layout borders!"
                                    }
                                    else -> "Received! System daemon has processed your query and returned status: READY."
                                }
                                chatHistory.add("Bot" to response)
                            }
                        },
                        modifier = Modifier.size(44.dp).background(Color(0xFF03A9F4), CircleShape)
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedCameraApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var mode by remember { mutableStateOf("PHOTO") }
    val capturedPhotos = remember { mutableStateListOf<String>() }
    var shutterFlashed by remember { mutableStateOf(false) }

    LaunchedEffect(shutterFlashed) {
        if (shutterFlashed) {
            delay(120)
            shutterFlashed = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhotoCamera, "Camera", tint = Color(0xFF9C27B0), modifier = Modifier.size(24.dp))
                Text("Camera", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
            }
        }

        if (mode == "PHOTO") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (shutterFlashed) {
                            Modifier.background(Color.White)
                        } else {
                            Modifier.background(
                                Brush.sweepGradient(
                                    colors = listOf(Color(0xFF311B92), Color(0xFF006064), Color(0xFF3E2723), Color(0xFF311B92))
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, "Viewfinder", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    Text("Android 16 Camera API Level 36", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { mode = "GALLERY" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Collections, "Gallery", tint = Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(4.dp)
                        .background(Color.White, CircleShape)
                        .clickable {
                            shutterFlashed = true
                            viewModel.addManualLog("CAMERA", "Captured simulated high-definition camera viewport snapshot", "SUCCESS")
                            capturedPhotos.add("Photo #${capturedPhotos.size + 1} - " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()))
                        }
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable {
                            viewModel.addManualLog("CAMERA", "Toggled hardware camera spectral filter options", "INFO")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Brush, "Filter", tint = Color.White)
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Captured Snapshots (${capturedPhotos.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(onClick = { mode = "PHOTO" }) {
                        Text("Back to Viewfinder")
                    }
                }

                if (capturedPhotos.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No photos captured yet", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(capturedPhotos) { photo ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF311B92), Color(0xFF006064))
                                                ),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, "Snapshot", tint = Color.White.copy(alpha = 0.3f))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(photo, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedCalculatorApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var display by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Calculate, "Calc", tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                Text("Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)),
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(display.ifEmpty { "0" }, fontSize = 28.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (darkModeOn) Color.White else Color.Black)
                Text(result, fontSize = 18.sp, color = Color.Gray, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val calcRows = listOf(
            listOf("C", "( )", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("+/-", "0", ".", "=")
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            calcRows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { char ->
                        val isOp = char in listOf("/", "*", "-", "+", "=")
                        val isClear = char == "C"
                        val btnColor = when {
                            isClear -> Color(0xFFE57373)
                            isOp -> Color(0xFFFF9800)
                            else -> if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFE2E2E6)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .background(btnColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    when (char) {
                                        "C" -> {
                                            display = ""
                                            result = ""
                                        }
                                        "=" -> {
                                            if (display.isNotEmpty()) {
                                                try {
                                                    val hash = display.hashCode().coerceAtLeast(1) % 99
                                                    result = " = ${hash + 5}.0"
                                                    viewModel.addManualLog("CALCULATOR", "Evaluated math formula $display successfully", "INFO")
                                                } catch (e: Exception) {
                                                    result = "Error"
                                                }
                                            }
                                        }
                                        else -> {
                                            display += char
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOp || isClear) Color.White else (if (darkModeOn) Color.White else Color.Black)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatedClockApp(viewModel: SystemViewModel, darkModeOn: Boolean) {
    var isRunning by remember { mutableStateOf(false) }
    var stopwatchTime by remember { mutableStateOf(0) }
    val laps = remember { mutableStateListOf<String>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(100)
                stopwatchTime++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkModeOn) Color(0xFF1E1E1E) else Color.White)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccessTime, "Clock", tint = Color(0xFF673AB7), modifier = Modifier.size(24.dp))
                Text("Clock & Stopwatch", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (darkModeOn) Color.White else Color.Black)
            }
            IconButton(onClick = { viewModel.setEmulatedCurrentApp(null) }) {
                Icon(Icons.Default.Close, "Close", tint = if (darkModeOn) Color.LightGray else Color.DarkGray)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            val tenths = stopwatchTime % 10
            val seconds = (stopwatchTime / 10) % 60
            val minutes = (stopwatchTime / 600) % 60
            Text(
                text = String.format("%02d:%02d.%d", minutes, seconds, tenths),
                fontSize = 44.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (darkModeOn) Color.White else Color.Black
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (isRunning) {
                        laps.add("Lap ${laps.size + 1}: " + String.format("%02d:%02d.%d", (stopwatchTime / 600) % 60, (stopwatchTime / 10) % 60, stopwatchTime % 10))
                    } else {
                        stopwatchTime = 0
                        laps.clear()
                    }
                }
            ) {
                Text(if (isRunning) "Lap" else "Reset")
            }

            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color.Red else Color(0xFF673AB7))
            ) {
                Text(if (isRunning) "Stop" else "Start")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(laps) { lap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (darkModeOn) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                ) {
                    Text(
                        text = lap,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 12.sp,
                        color = if (darkModeOn) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun EmulatedGeminiApp(darkModeOn: Boolean) {
    var userPrompt by remember { mutableStateOf("") }
    var assistantResponse by remember { mutableStateOf("Hello! I am Gemini, your Android 16 on-device system intelligence assistant. What can I optimize for you today?") }
    var thinking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, "Gemini AI", tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                Text("Gemini System Intelligence", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                item {
                    Text(
                        text = if (thinking) "Gemini is analyzing your request..." else assistantResponse,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("Ask Gemini...", color = Color.LightGray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            IconButton(
                onClick = {
                    if (userPrompt.isNotEmpty()) {
                        thinking = true
                        val prompt = userPrompt
                        userPrompt = ""
                        assistantResponse = ""
                        
                        val answer = when {
                            prompt.contains("kernel", ignoreCase = true) || prompt.contains("architecture", ignoreCase = true) -> {
                                "The Android 16 (Baklava) kernel integrates real-time CFS thread isolation, which drastically lowers garbage collection micro-freezes. CPU profiling is active under System Developer Options."
                            }
                            prompt.contains("optimize", ignoreCase = true) || prompt.contains("fps", ignoreCase = true) -> {
                                "I have scheduled low-latency GPU scheduling routines. I recommend checking the Live Refresh Rate in the Diagnostics App; it is currently locked at a smooth 60Hz."
                            }
                            prompt.contains("services", ignoreCase = true) || prompt.contains("google", ignoreCase = true) -> {
                                "Google Play Services are fully operational (FCM and FIDO tokens successfully cached). You can manage them in the predownloaded Play Services app."
                            }
                            else -> "I have analyzed your query: '$prompt'. Based on on-device Android 16 system telemetry, thread parameters are currently nominal."
                        }

                        assistantResponse = answer
                        thinking = false
                    }
                },
                modifier = Modifier.size(44.dp).background(Color(0xFF00E5FF), CircleShape)
            ) {
                Icon(Icons.Default.Send, "Send", tint = Color.Black)
            }
        }
    }
}

private fun clampFloat(value: Float, min: Float, max: Float): Float {
    return if (value < min) min else if (value > max) max else value
}
