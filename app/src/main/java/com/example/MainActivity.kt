package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.JobLog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ConsoleLog
import com.example.viewmodel.SystemViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission logged through ViewModel inside Compose setContent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto request notifications permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme(darkTheme = true) { // We force a modern, premium technical dark mode
                val systemViewModel: SystemViewModel = viewModel()
                
                // Track if permission is granted for UI diagnostics
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    systemViewModel.addManualLog(
                        "PERMISSIONS", 
                        "Notification Permission State Check: " + if (isGranted) "GRANTED" else "NOT GRANTED", 
                        if (isGranted) "INFO" else "WARNING"
                    )
                }

                // Global Layout boundaries configuration
                val layoutBoundaries by systemViewModel.layoutBoundariesActive.collectAsState()
                val gpuProfiling by systemViewModel.gpuProfilingActive.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .testTag("app_root_container")
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            SystemBottomNavigation(systemViewModel)
                        }
                    ) { innerPadding ->
                        val selectedTab by systemViewModel.selectedTab.collectAsState()
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .then(
                                    if (layoutBoundaries) Modifier.border(1.5.dp, Color(0xFFE91E63)) else Modifier
                                )
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(40)) togetherWith fadeOut(animationSpec = tween(40))
                                },
                                label = "TabNavigation"
                            ) { tab ->
                                when (tab) {
                                    0 -> SystemStatusTab(systemViewModel)
                                    1 -> PlayServicesTab(systemViewModel)
                                    2 -> WorkManagerTab(systemViewModel)
                                    3 -> DevToolsTab(systemViewModel)
                                    4 -> GitHubTab(systemViewModel)
                                }
                            }
                        }
                    }

                    // Optional GPU Profiling simulated overlay
                    if (gpuProfiling) {
                        GpuProfilingOverlay(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

// Custom Debug Outline Modifier to simulate "Show Layout Boundaries" developer tool
@Composable
fun Modifier.debugOutline(active: Boolean, color: Color = Color(0xFFE91E63)): Modifier {
    return if (active) {
        this.border(1.dp, color)
    } else {
        this
    }
}

@Composable
fun SystemBottomNavigation(viewModel: SystemViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeTasks by viewModel.activeTasksCount.collectAsState()

    NavigationBar(
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { viewModel.setSelectedTab(0) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "System") },
            label = { Text("System") },
            modifier = Modifier.testTag("nav_tab_system")
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { viewModel.setSelectedTab(1) },
            icon = { Icon(Icons.Default.Dns, contentDescription = "Play Services") },
            label = { Text("Play Services") },
            modifier = Modifier.testTag("nav_tab_play_services")
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { viewModel.setSelectedTab(2) },
            icon = { 
                BadgedBox(
                    badge = {
                        if (activeTasks > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(activeTasks.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Default.ListAlt, contentDescription = "WorkManager")
                }
            },
            label = { Text("WorkManager") },
            modifier = Modifier.testTag("nav_tab_workmanager")
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { viewModel.setSelectedTab(3) },
            icon = { Icon(Icons.Default.Terminal, contentDescription = "DevTools") },
            label = { Text("Dev Tools") },
            modifier = Modifier.testTag("nav_tab_devtools")
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { viewModel.setSelectedTab(4) },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = "GitHub") },
            label = { Text("GitHub") },
            modifier = Modifier.testTag("nav_tab_github")
        )
    }
}

// -------------------------------------------------------------
// TAB 0: SYSTEM STATUS & NATIVE EMULATION
// -------------------------------------------------------------
@Composable
fun SystemStatusTab(viewModel: SystemViewModel) {
    val fps by viewModel.fps.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val ramUsage by viewModel.ramUsage.collectAsState()
    val debugActive by viewModel.layoutBoundariesActive.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Technical Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_system_banner),
                        contentDescription = "System banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f
                    )
                    
                    // Technical Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ANDROID 15 NATIVE PLATFORM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFB6FFB6).copy(alpha = 0.85f), CircleShape)
                                )
                                Text(
                                    text = "EMULATOR ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB6FFB6)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "System Monitor",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = "Android 15 • Build AD1.2405 • API ${viewModel.sdkVersion} (${viewModel.codename})",
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Physical Phone Installation & Stream Delay Optimizer Guide
            var linkCopied by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager }
            val debugActive by viewModel.layoutBoundariesActive.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Physical Phone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "RUN ON YOUR PHYSICAL PHONE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Escape the web-streaming latency entirely! Since you have connected your GitHub account, you can compile and download the native `.apk` directly onto your Android device for native 60 FPS performance.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Step 1: Download Native APK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compile & Download Native APK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "1. Click the Settings (Gear Icon) in the Google AI Studio top-right bar.\n" +
                                        "2. Select 'Generate APK / AAB' from the menu.\n" +
                                        "3. Google AI Studio will compile your code and download the official '.apk' file to your computer/phone.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Step 2: Open on Device
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Install on Android Phone", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "Transfer the downloaded APK file to your phone (via USB, Google Drive, email, or by downloading it directly from your GitHub repository releases/actions), open the file, and authorize 'Install from Unknown Sources' to run the app.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Step 3: Run Mobile Web Emulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instant Option: Web-Stream on Mobile Chrome", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "Open your app's live stream URL in your phone's browser for full touch screen control without installing any files.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clip = android.content.ClipData.newPlainText("Shared App URL", "https://ais-pre-jwh27wjvhdyjhds755jnff-167799279054.us-east1.run.app")
                                clipboardManager.setPrimaryClip(clip)
                                linkCopied = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f).testTag("copy_phone_link_btn")
                        ) {
                            Icon(
                                imageVector = if (linkCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (linkCopied) "Link Copied!" else "Copy Phone Link", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                uriHandler.openUri("https://ais-pre-jwh27wjvhdyjhds755jnff-167799279054.us-east1.run.app")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("open_phone_stream_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open Link", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Stream", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            // Live Diagnostics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FPS Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .debugOutline(debugActive),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LIVE FPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("%.1f", fps),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (fps >= 55) Color(0xFFB6FFB6) else Color(0xFFFFCC00)
                        )
                        LinearProgressIndicator(
                            progress = { (fps / 60.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = if (fps >= 55) Color(0xFFB6FFB6) else Color(0xFFFFCC00),
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    }
                }

                // CPU Load Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .debugOutline(debugActive),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU LOAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$cpuLoad%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { (cpuLoad / 100f) },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    }
                }

                // RAM Usage Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .debugOutline(debugActive),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RAM USE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("%.2f GB", ramUsage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        LinearProgressIndicator(
                            progress = { (ramUsage / 8.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }

        item {
            // Live Frame Rendering Path Graph (Custom Canvas)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        "FRAME TIME TELEMETRY (LIVE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val spacing = width / (fpsHistory.size - 1)
                            
                            val path = Path()
                            fpsHistory.forEachIndexed { index, value ->
                                // Map 30fps to 60fps onto canvas height
                                val normalizedVal = clampFloat((value - 30f) / 30f, 0f, 1f)
                                val x = index * spacing
                                val y = height - (normalizedVal * height)
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            
                            drawPath(
                                path = path,
                                color = Color(0xFFB6FFB6),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("History Interval (last 20 frames)", fontSize = 9.sp, color = Color.Gray)
                        Text("Target: 60Hz", fontSize = 9.sp, color = Color(0xFFB6FFB6))
                    }
                }
            }
        }

        item {
            // Native Platform Spec Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "HARDWARE ENGINE SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    SpecRow("Hardware Model", viewModel.model)
                    SpecRow("Manufacturer", viewModel.manufacturer)
                    SpecRow("System Kernel", "Android 15 Native SDK runtime wrapper")
                    SpecRow("Edge-to-Edge Enabled", "TRUE (enableEdgeToEdge() loaded)")
                    SpecRow("Predictive Back Handled", "TRUE (OnBackPressedDispatcher linked)")

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.addManualLog("SYSTEM", "Simulating native Predictive Back Gesture. Closing secondary sheets.", "INFO")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("predictive_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Predictive Back Gesture")
                    }
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.LightGray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}

// -------------------------------------------------------------
// TAB 1: GOOGLE PLAY SERVICES INTEGRATIONS
// -------------------------------------------------------------
@Composable
fun PlayServicesTab(viewModel: SystemViewModel) {
    val playStatus by viewModel.playServicesStatus.collectAsState()
    val gpsConnected by viewModel.gpsLocationConnected.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    val debugActive by viewModel.layoutBoundariesActive.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Card displaying Play Services current simulation state
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(
                    containerColor = when (playStatus) {
                        "AVAILABLE" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Google Play Services Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (playStatus) {
                                    "AVAILABLE" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            Text(
                                text = "CURRENT: $playStatus",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = when (playStatus) {
                                    "AVAILABLE" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                        Icon(
                            imageVector = if (playStatus == "AVAILABLE") Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = "Cloud Status",
                            tint = when (playStatus) {
                                "AVAILABLE" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (playStatus) {
                            "AVAILABLE" -> "Google Api Availability check succeeded. Native mapping, sign-in, and location listeners are bound to Google play services engine."
                            "OUT_OF_DATE" -> "WARNING: Google Play Services requires updating. Application services falling back to native Android framework hooks."
                            else -> "ERROR: Google Play Services unavailable. Maps and FCM background sync channels suspended."
                        },
                        fontSize = 11.sp,
                        color = when (playStatus) {
                            "AVAILABLE" -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        }
                    )
                }
            }
        }

        item {
            // Google Play Services Simulated Status Controller
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "DEBUG OVERRIDE: PLAY SERVICES CONFIG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updatePlayServicesStatus("AVAILABLE") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (playStatus == "AVAILABLE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("play_avail_btn")
                        ) {
                            Text("Available", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.updatePlayServicesStatus("OUT_OF_DATE") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (playStatus == "OUT_OF_DATE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("play_out_btn")
                        ) {
                            Text("Out-of-Date", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.updatePlayServicesStatus("SUSPENDED") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (playStatus == "SUSPENDED") Color.Red else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("play_susp_btn")
                        ) {
                            Text("Suspended", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        item {
            // Google Location Service Lock
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.MyLocation, contentDescription = "GPS Location", tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Google Fused Location Provider", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (gpsConnected) "Status: HIGH_ACCURACY Lock Active" else "Status: DISCONNECTED",
                                    fontSize = 11.sp,
                                    color = if (gpsConnected) Color(0xFFB6FFB6) else Color.Gray
                                )
                            }
                        }
                        Switch(
                            checked = gpsConnected,
                            onCheckedChange = { viewModel.toggleGpsLocation() },
                            modifier = Modifier.testTag("location_provider_switch")
                        )
                    }

                    if (gpsConnected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mock Position Latitude/Longitude", fontSize = 9.sp, color = Color.Gray)
                                Text("37.4220° N, 122.0841° W", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Googleplex, Mountain View, CA", fontSize = 11.sp, color = Color.LightGray)
                            }
                            Icon(Icons.Default.GpsFixed, contentDescription = "Fixed Lock", tint = Color(0xFFB6FFB6))
                        }
                    }
                }
            }
        }

        item {
            // Google Maps SDK Simulated Map Vector Graphic
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "GOOGLE MAPS SDK OVERLAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(color = Color.Black, shape = RoundedCornerShape(4.dp)) {
                            Text("V4.2.1", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Custom Simulated Vector Map View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF202025), RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw simulated map grid lines
                            val gridSpacing = 40.dp.toPx()
                            for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                                drawLine(
                                    color = Color(0xFF303035),
                                    start = Offset(x.toFloat(), 0f),
                                    end = Offset(x.toFloat(), size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                                drawLine(
                                    color = Color(0xFF303035),
                                    start = Offset(0f, y.toFloat()),
                                    end = Offset(size.width, y.toFloat()),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // Draw simulated highways
                            drawLine(
                                color = Color(0xFF50505A),
                                start = Offset(0f, size.height * 0.3f),
                                end = Offset(size.width, size.height * 0.7f),
                                strokeWidth = 8.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xFF50505A),
                                start = Offset(size.width * 0.5f, 0f),
                                end = Offset(size.width * 0.5f, size.height),
                                strokeWidth = 6.dp.toPx()
                            )
                            
                            // Draw GPS marker
                            if (gpsConnected) {
                                drawCircle(
                                    color = Color(0xFF33B5E5).copy(alpha = 0.35f),
                                    radius = 20.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                                )
                                drawCircle(
                                    color = Color(0xFF33B5E5),
                                    radius = 3.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                                )
                            }
                        }
                        
                        Text(
                            text = if (gpsConnected) "Maps API Render: Googleplex Locked" else "Maps API Render: Offline",
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            // FCM Setup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "FCM Notification", tint = MaterialTheme.colorScheme.secondary)
                            Text("Google Cloud Messaging (FCM)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Secure Token Payload:", fontSize = 11.sp, color = Color.Gray)
                    
                    Text(
                        text = fcmToken,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = { viewModel.regenerateFcmToken() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("rotate_token_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rotate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rotate Secure Push Token")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: WORKMANAGER BACKGROUND TASKS
// -------------------------------------------------------------
@Composable
fun WorkManagerTab(viewModel: SystemViewModel) {
    val jobLogs by viewModel.jobLogs.collectAsState()
    val activeTasks by viewModel.activeTasksCount.collectAsState()
    val debugActive by viewModel.layoutBoundariesActive.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Upper Scheduler Actions panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .debugOutline(debugActive),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "WORKMANAGER TASK CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Simulates native Android 15 periodic and immediate background worker threads. Triggers persistent Room logging and real system notifications.",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                if (activeTasks > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("$activeTasks Active WorkManager job(s) executing...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerBackgroundTask("Periodic Telemetry Sync") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("trigger_telemetry_btn")
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = "Sync", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Telemetry", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { viewModel.triggerBackgroundTask("FCM Notification Fetcher") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("trigger_fcm_btn")
                    ) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = "FCM Fetch", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fetch FCM Queue", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Persistent database headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ROOM SQLITE SYNC LOGS (${jobLogs.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )

            TextButton(
                onClick = { viewModel.clearAllJobLogs() },
                enabled = jobLogs.isNotEmpty(),
                modifier = Modifier.testTag("clear_logs_btn")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Truncate Table", fontSize = 11.sp)
            }
        }

        // List of persistent SQLite Room logs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (jobLogs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = "Empty",
                        modifier = Modifier.size(40.dp),
                        tint = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No sync records found in Room database.", fontSize = 13.sp, color = Color.Gray)
                    Text("Trigger an action above to insert SQLite rows.", fontSize = 11.sp, color = Color.DarkGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(jobLogs, key = { it.id }) { log ->
                        JobLogCard(log, onDelete = { viewModel.clearAllJobLogs() }, debugActive)
                    }
                }
            }
        }
    }
}

@Composable
fun JobLogCard(log: JobLog, onDelete: () -> Unit, debugActive: Boolean) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val timeString = formatter.format(Date(log.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugOutline(debugActive),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = when (log.status) {
                            "SUCCESS" -> Color(0xFFB6FFB6).copy(alpha = 0.15f)
                            "RUNNING" -> Color(0xFF33B5E5).copy(alpha = 0.15f)
                            else -> Color.Red.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = log.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (log.status) {
                                "SUCCESS" -> Color(0xFFB6FFB6)
                                "RUNNING" -> Color(0xFF33B5E5)
                                else -> Color.Red
                            },
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(log.jobName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(timeString, fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(log.message, fontSize = 11.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Payload: ${log.payloadSize}", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    if (log.executionTimeMs > 0) {
                        Text("Duration: ${log.executionTimeMs}ms", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }
                }
                Text("Row ID: #${log.id}", fontSize = 10.sp, color = Color.DarkGray, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: DEVELOPER TOOLS & DIAGNOSTICS LOGS
// -------------------------------------------------------------
@Composable
fun DevToolsTab(viewModel: SystemViewModel) {
    val customLogs by viewModel.customLogs.collectAsState()
    val boundariesActive by viewModel.layoutBoundariesActive.collectAsState()
    val gpuProfilingActive by viewModel.gpuProfilingActive.collectAsState()
    val streamQualityUHD by viewModel.streamQualityUHD.collectAsState()
    val inputLatencyOptimized by viewModel.inputLatencyOptimized.collectAsState()
    val debugActive by viewModel.layoutBoundariesActive.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Native System Profiler Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .debugOutline(debugActive),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "DEVELOPER PERFORMANCE TUNING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Show Layout Boundaries", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Renders neon pink bounding vectors", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = boundariesActive,
                        onCheckedChange = { viewModel.toggleLayoutBoundaries() },
                        modifier = Modifier.testTag("layout_boundaries_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Simulate Profile GPU Rendering", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Overlays frame-time performance graph", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = gpuProfilingActive,
                        onCheckedChange = { viewModel.toggleGpuProfiling() },
                        modifier = Modifier.testTag("gpu_profiling_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Optimized Cloud Stream (1080p UHD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (streamQualityUHD) "Full HD Resolution @ 60 FPS Locked" else "Standard Definition (Reduced Quality)",
                            fontSize = 11.sp,
                            color = if (streamQualityUHD) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    Switch(
                        checked = streamQualityUHD,
                        onCheckedChange = { viewModel.toggleStreamQuality() },
                        modifier = Modifier.testTag("stream_quality_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Input Polling & Low Latency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (inputLatencyOptimized) "Ultra-Low Delay (Sub-5ms Mode Active)" else "Standard Delay (High Latency Input)",
                            fontSize = 11.sp,
                            color = if (inputLatencyOptimized) Color(0xFFB6FFB6) else Color.Gray
                        )
                    }
                    Switch(
                        checked = inputLatencyOptimized,
                        onCheckedChange = { viewModel.toggleInputLatency() },
                        modifier = Modifier.testTag("input_latency_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Diagnostic Console Log filters
        Text("NATIVE CONSOLE LOGGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf("ALL", "SYSTEM", "PLAY_SERVICES", "WORK_MANAGER", "DEV_TOOLS", "PERMISSIONS")
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 11.sp) },
                    modifier = Modifier.testTag("filter_chip_$filter")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Logging output terminal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            val filteredLogs = remember(customLogs, selectedFilter) {
                if (selectedFilter == "ALL") {
                    customLogs
                } else {
                    customLogs.filter { it.tag == selectedFilter }
                }
            }

            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs matching filter.", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false
                ) {
                    items(filteredLogs) { log ->
                        ConsoleLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsoleLogItem(log: ConsoleLog) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val timeStr = timeFormatter.format(Date(log.timestamp))

    val color = when (log.level) {
        "DEBUG" -> Color(0xFFB6FFB6)
        "WARNING" -> Color(0xFFFFCC00)
        "ERROR" -> Color(0xFFFF3333)
        else -> Color.White
    }

    Text(
        text = "[$timeStr] [${log.tag}] ${log.message}",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = color,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// -------------------------------------------------------------
// GPU PROFILING SIMULATOR
// -------------------------------------------------------------
@Composable
fun GpuProfilingOverlay(modifier: Modifier = Modifier) {
    // Generates modern multicolored bars at bottom, representing Draw, Prepare, Process, Execute stages
    val heights = remember { mutableStateListOf<List<Float>>() }
    
    // Periodically update heights for dynamic visual feedback
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (heights.size > 40) {
                heights.removeAt(0)
            }
            heights.add(
                listOf(
                    kotlin.random.Random.nextFloat() * 10f + 2f,  // Draw (Blue)
                    kotlin.random.Random.nextFloat() * 15f + 4f,  // Prepare (Purple)
                    kotlin.random.Random.nextFloat() * 8f + 2f,   // Process (Red)
                    kotlin.random.Random.nextFloat() * 12f + 3f   // Execute (Orange)
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.DarkGray))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Simulated GPU Rendering Profile (ms/frame)", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem("Draw", Color(0xFF0099CC))
                LegendItem("Prepare", Color(0xFF9933CC))
                LegendItem("Process", Color(0xFFFF4444))
                LegendItem("Execute", Color(0xFFFFBB33))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 6.dp.toPx()
                val barGap = 4.dp.toPx()
                val thresholdY = size.height - (16.6f * (size.height / 45f)) // 16ms boundary line
                
                // Draw 16.6ms threshold (60Hz boundary)
                drawLine(
                    color = Color.Green,
                    start = Offset(0f, thresholdY),
                    end = Offset(size.width, thresholdY),
                    strokeWidth = 1.dp.toPx()
                )

                heights.forEachIndexed { colIndex, barHeights ->
                    val x = size.width - ((heights.size - colIndex) * (barWidth + barGap))
                    if (x >= 0) {
                        var currentY = size.height
                        
                        // Draw stacked bars
                        barHeights.forEachIndexed { index, value ->
                            val segmentHeight = value * (size.height / 45f)
                            val color = when (index) {
                                0 -> Color(0xFF0099CC)
                                1 -> Color(0xFF9933CC)
                                2 -> Color(0xFFFF4444)
                                else -> Color(0xFFFFBB33)
                            }
                            drawRect(
                                color = color,
                                topLeft = Offset(x, currentY - segmentHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, segmentHeight)
                            )
                            currentY -= segmentHeight
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color))
        Text(label, fontSize = 8.sp, color = Color.LightGray)
    }
}

private fun clampFloat(value: Float, min: Float, max: Float): Float {
    return if (value < min) min else if (value > max) max else value
}

@Composable
fun GitHubTab(viewModel: SystemViewModel) {
    val context = LocalContext.current
    val debugActive by viewModel.layoutBoundariesActive.collectAsState()

    val githubToken by viewModel.githubToken.collectAsState()
    val githubUsername by viewModel.githubUsername.collectAsState()
    val githubUser by viewModel.githubUser.collectAsState()
    val githubRepos by viewModel.githubRepos.collectAsState()
    val isLoadingRepos by viewModel.isLoadingRepos.collectAsState()
    val reposError by viewModel.reposError.collectAsState()

    val selectedRepo by viewModel.selectedRepo.collectAsState()
    val releasesList by viewModel.releasesList.collectAsState()
    val isLoadingReleases by viewModel.isLoadingReleases.collectAsState()
    val releasesError by viewModel.releasesError.collectAsState()

    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadingAssetName by viewModel.downloadingAssetName.collectAsState()
    val downloadedApkFileUri by viewModel.downloadedApkFileUri.collectAsState()
    val downloadError by viewModel.downloadError.collectAsState()
    val downloadSuccessMessage by viewModel.downloadSuccessMessage.collectAsState()

    var tokenInput by remember { mutableStateOf(githubToken) }
    var usernameInput by remember { mutableStateOf(githubUsername) }

    // Synchronize inputs if changed elsewhere
    LaunchedEffect(githubToken) { tokenInput = githubToken }
    LaunchedEffect(githubUsername) { usernameInput = githubUsername }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .debugOutline(debugActive),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "GitHub Native",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "GitHub Integration",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Connect accounts, fetch repositories, pull compiled APKs, and install them directly onto your local device.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Active Download / Emulation Setup Notification Card
        if (downloadProgress != null || downloadedApkFileUri != null || downloadError != null || downloadSuccessMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugOutline(debugActive),
                    colors = CardDefaults.cardColors(
                        containerColor = if (downloadError != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, if (downloadError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (downloadError != null) Icons.Default.ErrorOutline else Icons.Default.DownloadDone,
                                contentDescription = "Download status",
                                tint = if (downloadError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (downloadProgress != null) "DOWNLOADING APK..." else if (downloadError != null) "DOWNLOAD FAILED" else "APK READY FOR EMULATION",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (downloadError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (downloadProgress != null) {
                            val pct = downloadProgress!!
                            Text(
                                text = "Pulling ${downloadingAssetName ?: "APK"} from GitHub releases...",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "Progress: ${String.format("%.1f", pct * 100)}%",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        if (downloadError != null) {
                            Text(text = "Error: ${downloadError}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }

                        if (downloadSuccessMessage != null) {
                            Text(text = downloadSuccessMessage!!, fontSize = 12.sp, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (downloadedApkFileUri != null) {
                                Button(
                                    onClick = {
                                        viewModel.installApk(context, downloadedApkFileUri!!)
                                    },
                                    modifier = Modifier.weight(1f).testTag("install_pulled_apk_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Emulation (Install)")
                                }
                            }

                            Button(
                                onClick = { viewModel.clearDownloadState() },
                                modifier = if (downloadedApkFileUri == null) Modifier.fillMaxWidth() else Modifier.weight(0.5f).testTag("clear_apk_state_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Credentials & Authentication Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .debugOutline(debugActive),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "GITHUB AUTHENTICATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (githubUser != null) {
                        // User Profile UI
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Letter Avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (githubUser!!.name ?: githubUser!!.login).take(1).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = githubUser!!.name ?: githubUser!!.login,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "@${githubUser!!.login}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (!githubUser!!.bio.isNullOrEmpty()) {
                                    Text(
                                        text = githubUser!!.bio!!,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${githubUser!!.public_repos} Public Repositories Loaded",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            TextButton(
                                onClick = {
                                    viewModel.saveGitHubCredentials("", "")
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Disconnect", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Disconnect", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Inputs UI
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("GitHub Username") },
                            placeholder = { Text("e.g. isleepy20") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User") },
                            modifier = Modifier.fillMaxWidth().testTag("github_username_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            label = { Text("Personal Access Token (PAT) - Optional") },
                            placeholder = { Text("ghp_...") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = "Token") },
                            modifier = Modifier.fillMaxWidth().testTag("github_token_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Text(
                            text = "💡 Direct public queries are limited by GitHub rate limits. Providing a PAT resolves limits and accesses private repositories.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Button(
                            onClick = {
                                viewModel.saveGitHubCredentials(tokenInput.trim(), usernameInput.trim())
                            },
                            modifier = Modifier.fillMaxWidth().testTag("connect_github_btn"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = usernameInput.isNotEmpty() || tokenInput.isNotEmpty()
                        ) {
                            if (isLoadingRepos) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.Login, contentDescription = "Connect")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Load GitHub Repositories")
                            }
                        }
                    }

                    if (reposError != null) {
                        Text(
                            text = "Sync Error: $reposError",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Repository list / selected repo detail
        if (githubUser != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedRepo == null) "SELECT A REPOSITORY" else "REPOSITORY INFO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (selectedRepo != null) {
                        TextButton(onClick = { viewModel.selectRepo(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back to list", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (selectedRepo == null) {
                // List Repos
                if (githubRepos.isEmpty() && !isLoadingRepos) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "No repositories found for this account.",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(githubRepos) { repo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectRepo(repo) }
                                .debugOutline(debugActive),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = repo.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = repo.full_name,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Stars",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = repo.stargazers_count.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                if (!repo.description.isNullOrEmpty()) {
                                    Text(
                                        text = repo.description!!,
                                        fontSize = 12.sp,
                                        color = Color.LightGray,
                                        maxLines = 2
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!repo.language.isNullOrEmpty()) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(repo.language!!, fontSize = 10.sp) }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Text(
                                        text = "View Releases →",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Show Repository Details & Releases
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedRepo!!.name.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = selectedRepo!!.description ?: "No description provided",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Language: ${selectedRepo!!.language ?: "Unknown"}", fontSize = 11.sp, color = Color.Gray)
                                Text("Stars: ${selectedRepo!!.stargazers_count}", fontSize = 11.sp, color = Color.Gray)
                                Text("Forks: ${selectedRepo!!.forks_count}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "COMPILED RELEASES & APK ASSETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (isLoadingReleases) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (releasesError != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "Failed to load releases: $releasesError",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (releasesList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "No releases found.",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "To deploy APKs through this module, compile your project in GitHub and create a Release containing the output `.apk` file.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(releasesList) { release ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .debugOutline(debugActive),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = release.name ?: release.tag_name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = release.tag_name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                if (!release.body.isNullOrEmpty()) {
                                    Text(
                                        text = release.body!!,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 3
                                    )
                                }

                                val apkAssets = remember(release) {
                                    release.assets.filter { it.name.endsWith(".apk") }
                                }

                                if (apkAssets.isEmpty()) {
                                    Text(
                                        text = "⚠️ No compiled .apk assets found in this release.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Text("Download Assets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    
                                    apkAssets.forEach { asset ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = asset.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Size: ${String.format("%.2f", asset.size / (1024.0 * 1024.0))} MB • Downloads: ${asset.download_count}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.downloadApk(asset, selectedRepo!!.name)
                                                },
                                                modifier = Modifier.size(36.dp).testTag("download_apk_${asset.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download APK",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
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
