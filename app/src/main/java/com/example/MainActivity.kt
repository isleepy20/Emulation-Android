package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val gameViewModel: SystemViewModel = viewModel()
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding() // Guarantees absolutely no overlap with physical notches or navigations!
                        .testTag("app_root_container"),
                    color = Color(0xFF0F172A) // Sleek Slate-Dark background
                ) {
                    CookieClickerMainView(gameViewModel)
                }
            }
        }
    }
}

@Composable
fun CookieClickerMainView(viewModel: SystemViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    
    val currentCookies by viewModel.currentCookies.collectAsState()
    val totalCookiesBaked by viewModel.totalCookiesBaked.collectAsState()
    val totalClicks by viewModel.totalClicks.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val floatingTexts by viewModel.floatingTexts.collectAsState()
    val goldenCookie by viewModel.goldenCookie.collectAsState()
    
    val feverActive by viewModel.feverActive.collectAsState()
    val feverSecondsRemaining by viewModel.feverSecondsRemaining.collectAsState()
    val offlineEarnings by viewModel.offlineEarnings.collectAsState()
    val offlineSeconds by viewModel.offlineSeconds.collectAsState()
    val productionHistory by viewModel.productionHistory.collectAsState()
    
    val cps = viewModel.getCookiesPerSecond()
    val clickPower = viewModel.getClickPower()

    // Offline popup alert dialog
    if (offlineEarnings != null && offlineSeconds > 0) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOfflinePopup() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍪 Welcome Back!", fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                }
            },
            text = {
                Column {
                    Text(
                        "While you were away for ${viewModel.formatValue(offlineSeconds.toDouble())} seconds, your hard-working bakery produced:",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "${viewModel.formatValue(offlineEarnings!!)} Cookies",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4FC3F7),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissOfflinePopup() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4DB))
                ) {
                    Text("Awesome!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                tonalElevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = "Bake", modifier = Modifier.size(24.dp)) },
                    label = { Text("Bake", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00B4DB),
                        selectedTextColor = Color(0xFF00B4DB),
                        indicatorColor = Color(0xFF334155)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Store, contentDescription = "Upgrades", modifier = Modifier.size(24.dp)) },
                    label = { Text("Shop", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFB74D),
                        selectedTextColor = Color(0xFFFFB74D),
                        indicatorColor = Color(0xFF334155)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Stats", modifier = Modifier.size(24.dp)) },
                    label = { Text("Stats & Ach", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF81C784),
                        selectedTextColor = Color(0xFF81C784),
                        indicatorColor = Color(0xFF334155)
                    )
                )
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Premium Banner / Header showing Current Cookies
            HeaderWidget(
                currentCookies = currentCookies,
                cps = cps,
                feverActive = feverActive,
                feverSeconds = feverSecondsRemaining,
                viewModel = viewModel
            )

            // Dynamic Tab Views
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> BakeTab(
                        viewModel = viewModel,
                        floatingTexts = floatingTexts,
                        goldenCookie = goldenCookie,
                        clickPower = clickPower,
                        feverActive = feverActive
                    )
                    1 -> UpgradesTab(
                        viewModel = viewModel,
                        upgrades = upgrades,
                        currentCookies = currentCookies
                    )
                    2 -> StatsTab(
                        viewModel = viewModel,
                        totalCookies = totalCookiesBaked,
                        currentCookies = currentCookies,
                        totalClicks = totalClicks,
                        clickPower = clickPower,
                        cps = cps,
                        achievements = achievements,
                        productionHistory = productionHistory
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderWidget(
    currentCookies: Double,
    cps: Double,
    feverActive: Boolean,
    feverSeconds: Int,
    viewModel: SystemViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍪 COOKIE MOBILE",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )

                // Quick reset button for easy testing or restarting
                IconButton(
                    onClick = { viewModel.resetGame() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Game",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${viewModel.formatValue(currentCookies)} Cookies",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (feverActive) Color(0xFFFFD700) else Color(0xFF4FC3F7),
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = if (feverActive) Color(0xFFFF9800).copy(alpha = 0.5f) else Color(0xFF00B4DB).copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "per second: ${viewModel.formatValue(cps)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray
            )

            // Fever indicator
            if (feverActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE65100).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FlashOn, "Fever", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "FEVER MODE (7X PRODUCTION): ${feverSeconds}s",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun BakeTab(
    viewModel: SystemViewModel,
    floatingTexts: List<FloatingText>,
    goldenCookie: GoldenCookieState,
    clickPower: Double,
    feverActive: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { if (isPressed) isPressed = false }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background radial sunburst drawing to mimic a delightful vintage gameplay environment
                if (feverActive) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE65100).copy(alpha = 0.25f), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.8f
                    )
                } else {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00B4DB).copy(alpha = 0.15f), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.7f
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Golden Cookie floating random popup
        if (goldenCookie.isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (goldenCookie.xPercent * 300).dp, // safe screen bound calculation
                            y = (goldenCookie.yPercent * 500).dp
                        )
                        .scale(1.2f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.clickGoldenCookie()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Floating Golden Cookie visual
                    Box(
                        modifier = Modifier
                            .size(goldenCookie.size.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFF176), Color(0xFFFFB300), Color(0xFFF57C00))
                                )
                            )
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨🍪✨", fontSize = 24.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Giant Cookie Interactive Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Disable default gray ripple so we can enjoy custom bounce animation
                        onClick = {
                            isPressed = true
                            viewModel.clickCookie()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect underneath giant cookie
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .drawBehind {
                            drawCircle(
                                color = if (feverActive) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF00B4DB).copy(alpha = 0.15f),
                                radius = size.minDimension * 0.5f
                            )
                        }
                )

                // The giant cookie image asset
                Image(
                    painter = painterResource(id = R.drawable.img_cookie),
                    contentDescription = "Giant Cookie",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .border(4.dp, if (feverActive) Color(0xFFFFB300) else Color(0xFF334155), CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Float numbers box (Rendered inside the cookie region)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    floatingTexts.forEach { ft ->
                        Box(
                            modifier = Modifier
                                .offset(x = ft.xOffset.dp, y = ft.yOffset.dp)
                        ) {
                            Text(
                                text = ft.text,
                                color = if (ft.isFever) Color(0xFFFFD700) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = (22 * ft.scale).sp,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TAP THE COOKIE!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (feverActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+${viewModel.formatValue(clickPower)} per click",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun UpgradesTab(
    viewModel: SystemViewModel,
    upgrades: List<UpgradeItem>,
    currentCookies: Double
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "⚡ CLICK POWER UPGRADES",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFFFB74D),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(upgrades.filter { !it.isCps }) { item ->
            UpgradeCard(item = item, currentCookies = currentCookies, onBuy = { viewModel.buyUpgrade(item.id) }, viewModel = viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "👵 AUTO-CPS BAKERS",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF00B4DB),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(upgrades.filter { it.isCps }) { item ->
            UpgradeCard(item = item, currentCookies = currentCookies, onBuy = { viewModel.buyUpgrade(item.id) }, viewModel = viewModel)
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UpgradeCard(
    item: UpgradeItem,
    currentCookies: Double,
    onBuy: () -> Unit,
    viewModel: SystemViewModel
) {
    val canAfford = currentCookies >= item.currentCost

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (canAfford) {
                    if (item.isCps) Color(0xFF00B4DB).copy(alpha = 0.5f) else Color(0xFFFFB74D).copy(alpha = 0.5f)
                } else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Upgrade Emoji/Icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 24.sp)
            }

            // Description column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (item.count > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00B4DB).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "x${item.count}",
                                color = Color(0xFF4FC3F7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Price & Buy button column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${viewModel.formatValue(item.currentCost)} 🍪",
                    color = if (canAfford) Color(0xFFFFB74D) else Color.Gray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isCps) Color(0xFF00B4DB) else Color(0xFFFFB74D),
                        disabledContainerColor = Color(0xFF475569).copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        "BUY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (canAfford) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatsTab(
    viewModel: SystemViewModel,
    totalCookies: Double,
    currentCookies: Double,
    totalClicks: Long,
    clickPower: Double,
    cps: Double,
    achievements: List<Achievement>,
    productionHistory: List<Double>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Real-time Production Graph
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📈 REAL-TIME BAKERY STATS",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4FC3F7),
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // High-fidelity custom live line graph
                    LiveProductionChart(history = productionHistory)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Live cookie production over the last 30 seconds",
                        fontSize = 10.sp,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. Numerical stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊 STATS OVERVIEW",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    StatRow("Current cookies", viewModel.formatValue(currentCookies))
                    StatRow("Total cookies baked", viewModel.formatValue(totalCookies))
                    StatRow("Cookies per Click", viewModel.formatValue(clickPower))
                    StatRow("Cookies per Second", viewModel.formatValue(cps))
                    StatRow("Total giant cookie clicks", totalClicks.toString())
                }
            }
        }

        // 3. Achievements Title
        item {
            Text(
                text = "🏆 ACHIEVEMENTS UNLOCKED (${achievements.count { it.isUnlocked }}/${achievements.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF81C784),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // 4. Achievements List
        items(achievements.chunked(3)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (ach in rowItems) {
                    AchievementBadge(ach = ach, modifier = Modifier.weight(1f))
                }
                // Fill up remainder empty space
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LiveProductionChart(history: List<Double>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        val width = size.width
        val height = size.height
        val points = history
        if (points.isNotEmpty()) {
            val maxVal = points.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
            val minVal = points.minOrNull() ?: 0.0
            val range = (maxVal - minVal).coerceAtLeast(1.0)
            
            val path = Path()
            points.forEachIndexed { idx, value ->
                val x = idx * (width / (points.size - 1))
                val y = height - ((value - minVal) / range * (height - 24f)).toFloat() - 12f
                if (idx == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            // Neon cyan glowing stroke line
            drawPath(
                path = path,
                color = Color(0xFF00B4DB),
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Subtly shaded neon fill below
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00B4DB).copy(alpha = 0.25f), Color.Transparent)
                )
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun AchievementBadge(ach: Achievement, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (ach.isUnlocked) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(115.dp)
            .border(
                1.dp,
                if (ach.isUnlocked) Color(0xFF81C784).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (ach.isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ach.icon,
                    fontSize = 18.sp,
                    modifier = Modifier.drawBehind {
                        if (!ach.isUnlocked) {
                            // Subtle grayscale draw
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = ach.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (ach.isUnlocked) Color.White else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = ach.description,
                fontSize = 8.sp,
                color = if (ach.isUnlocked) Color.LightGray else Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
