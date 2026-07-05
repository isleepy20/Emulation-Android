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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
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
                        .safeDrawingPadding()
                        .testTag("app_root_container"),
                    color = Color(0xFF0F172A) // Slate-Dark themed background
                ) {
                    CookieClickerMainView(gameViewModel)
                }
            }
        }
    }
}

@Composable
fun CookieClickerMainView(viewModel: SystemViewModel) {
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

    // Interactive button click scale
    var isPressed by remember { mutableStateOf(false) }
    val cookieScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cookie_scale"
    )

    // Infinite gentle rotation for the background aura and giant cookie
    val infiniteTransition = rememberInfiniteTransition(label = "rotation_transition")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Offline popup dialog
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
                        "While you were away for ${viewModel.formatValue(offlineSeconds.toDouble())} seconds, your bakery cooked up:",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "${viewModel.formatValue(offlineEarnings!!)} Cookies",
                        fontSize = 26.sp,
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
                    Text("Sweet!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // --- 1. HEADER BANNER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🍪",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Cookie Clicker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Bakery Edition",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = { viewModel.resetGame() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .size(40.dp)
                    .testTag("reset_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Progress",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 2. THE HERO INTERACTIVE BAKERY ZONE (Fixed) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (feverActive) {
                            listOf(Color(0xFF7F1D1D), Color(0xFF1E1B4B))
                        } else {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        }
                    )
                )
                .drawBehind {
                    // Optional decorative radial/circular background rings
                    drawCircle(
                        color = if (feverActive) Color(0x1AEF4444) else Color(0x06FFFFFF),
                        radius = size.minDimension / 1.5f,
                        center = center
                    )
                    drawCircle(
                        color = if (feverActive) Color(0x0DEF4444) else Color(0x0AFFFFFF),
                        radius = size.minDimension / 2.2f,
                        center = center
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Main Content inside interactive zone
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Score Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = viewModel.formatValue(currentCookies),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = if (feverActive) Color(0xFFFFD700) else Color(0xFF38BDF8),
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = if (feverActive) Color(0x80D97706) else Color(0x4038BDF8),
                                offset = Offset(2f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.testTag("cookie_score_text")
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "cookies",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (feverActive) Color(0xFFB45309) else Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CpS: ${viewModel.formatValue(cps)}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Central clickable rotating giant cookie image
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(cookieScale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.clickCookie()
                        }
                        .drawBehind {
                            if (feverActive) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0x66F59E0B), Color(0x00F59E0B)),
                                        center = center,
                                        radius = size.width / 1.1f
                                    )
                                )
                            }
                        }
                        .testTag("giant_cookie_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom user image asset img_cookie.jpg as giant cookie icon
                    Image(
                        painter = painterResource(id = R.drawable.img_cookie),
                        contentDescription = "Giant Cookie",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .clip(CircleShape)
                            .rotate(rotationAngle)
                            .border(
                                width = if (feverActive) 5.dp else 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = if (feverActive) {
                                        listOf(Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFFFBBF24))
                                    } else {
                                        listOf(Color(0xFF38BDF8), Color(0xFF0369A1), Color(0xFF38BDF8))
                                    }
                                ),
                                shape = CircleShape
                            )
                    )
                }

                // Bottom spacer
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Golden Cookie Floating Overlay Spawning
            if (goldenCookie.isVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(
                                BiasAlignment(
                                    horizontalBias = (goldenCookie.xPercent * 2f) - 1f,
                                    verticalBias = (goldenCookie.yPercent * 2f) - 1f
                                )
                            )
                            .size(goldenCookie.size.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFB45309))
                                )
                            )
                            .clickable {
                                viewModel.clickGoldenCookie()
                            }
                            .border(2.dp, Color.White, CircleShape)
                            .testTag("golden_cookie_floating"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨🍪✨",
                            fontSize = (goldenCookie.size / 3.2f).sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Floating Click +Value text indicators
            floatingTexts.forEach { fText ->
                Box(
                    modifier = Modifier
                        .offset(x = fText.xOffset.dp, y = fText.yOffset.dp)
                ) {
                    Text(
                        text = fText.text,
                        color = if (fText.isFever) Color(0xFFFFD700) else Color.White,
                        fontSize = (16f * fText.scale).sp,
                        fontWeight = FontWeight.Black,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(1f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }

            // Clicking Fever Screen Toast Overlay
            if (feverActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 FEVER MODE! 7X ACTIVE (${feverSecondsRemaining}s)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 3. SCROLLABLE CONTENTS (Upgrades, Achievements, Stats, Live Graph) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // SECTION: Upgrades
            item {
                Text(
                    text = "🛠️ Bakery Upgrades & Staff",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(upgrades) { upgrade ->
                val canAfford = currentCookies >= upgrade.currentCost
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canAfford) {
                            viewModel.buyUpgrade(upgrade.id)
                        }
                        .testTag("upgrade_item_${upgrade.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canAfford) Color(0xFF1E293B) else Color(0x801E293B)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (canAfford) Color(0x6638BDF8) else Color(0x1AFFFFFF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (canAfford) Color(0xFF334155) else Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = upgrade.icon, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = upgrade.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color.White else Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Lv. ${upgrade.count}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Text(
                                text = upgrade.description,
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = "Cost",
                                    tint = if (canAfford) Color(0xFFFFB74D) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = viewModel.formatValue(upgrade.currentCost),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color(0xFFFFB74D) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // SECTION: Achievements
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏆 Achievement Badges",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Render achievements as a neat flow row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    val unlockedCount = achievements.count { it.isUnlocked }
                    Text(
                        text = "Unlocked: $unlockedCount / ${achievements.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable row of achievements inside column
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        achievements.forEach { achievement ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .testTag("achievement_badge_${achievement.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (achievement.isUnlocked) {
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFFD700), Color(0xFFB45309))
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                                                )
                                            }
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = achievement.icon,
                                        fontSize = 20.sp,
                                        modifier = Modifier.scale(if (achievement.isUnlocked) 1.0f else 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = achievement.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (achievement.isUnlocked) Color.White else Color.Gray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // SECTION: Live Production Graph
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📈 Live Production History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Baking Rate (Interval Stats)",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Custom drawing graph
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        ) {
                            val width = size.width
                            val height = size.height

                            // Draw subtle gridlines
                            val gridLinesCount = 4
                            for (i in 1 until gridLinesCount) {
                                val y = height * i / gridLinesCount
                                drawLine(
                                    color = Color(0x11FFFFFF),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                            }

                            if (productionHistory.isNotEmpty()) {
                                val maxVal = productionHistory.maxOrNull()?.coerceAtLeast(10.0) ?: 10.0
                                val minVal = productionHistory.minOrNull() ?: 0.0
                                val range = (maxVal - minVal).coerceAtLeast(1.0)

                                val path = Path()
                                val points = productionHistory.mapIndexed { index, value ->
                                    val x = width * index / (productionHistory.size - 1)
                                    val ratio = (value - minVal) / range
                                    val y = height - (ratio.toFloat() * (height - 16f) + 8f)
                                    Offset(x, y)
                                }

                                // Connect line path
                                points.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        path.moveTo(point.x, point.y)
                                    } else {
                                        path.lineTo(point.x, point.y)
                                    }
                                }

                                // Draw line
                                drawPath(
                                    path = path,
                                    color = Color(0xFF38BDF8),
                                    style = Stroke(width = 4f)
                                )

                                // Fill area under path
                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0x3338BDF8), Color(0x0038BDF8))
                                    )
                                )

                                // Draw glowing dot on the last coordinate
                                points.lastOrNull()?.let { lastPoint ->
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6f,
                                        center = lastPoint
                                    )
                                    drawCircle(
                                        color = Color(0x8038BDF8),
                                        radius = 12f,
                                        center = lastPoint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION: Game Statistics
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📊 General Stats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow("Total Cookies Baked", viewModel.formatValue(totalCookiesBaked))
                        StatRow("Total Manual Clicks", viewModel.formatValue(totalClicks.toDouble()))
                        StatRow("Base Click Power", viewModel.formatValue(clickPower))
                        StatRow("Passive Auto-CpS", viewModel.formatValue(cps))
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
