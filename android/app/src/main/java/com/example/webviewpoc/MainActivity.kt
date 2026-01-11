package com.example.webviewpoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webviewpoc.ui.theme.WebViewPOCTheme

/**
 * Main Activity with Jetpack Compose
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebViewPOCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewPOCApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewPOCApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebView Performance POCs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("unoptimized") { UnoptimizedWebViewScreen(navController) }
            composable("optimized") { OptimizedWebViewScreen(navController) }
            composable("comparison") { ComparisonScreen(navController) }
            composable("webviewclient") { WebViewClientOptimizationsScreen(navController) }
            composable("caching") { CachingStrategiesScreen(navController) }
            composable("rendering") { RenderingPerformanceScreen(navController) }
            composable("jsbridge") { JavaScriptBridgeScreen(navController) }
            composable("settings") { SettingsExplorerScreen(navController) }
        }
    }
}

data class POCItem(
    val route: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String,
    val badgeColor: Color
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val pocItems = listOf(
        POCItem(
            route = "unoptimized",
            title = "Unoptimized WebView",
            description = "WebView with poor settings and no optimizations. Demonstrates common mistakes and performance issues.",
            icon = Icons.Default.Warning,
            badgeText = "❌ SLOW",
            badgeColor = Color(0xFFD32F2F)
        ),
        POCItem(
            route = "optimized",
            title = "Optimized WebView",
            description = "WebView with best practices and all optimizations enabled. Maximum performance configuration.",
            icon = Icons.Default.Done,
            badgeText = "✓ FAST",
            badgeColor = Color(0xFF388E3C)
        ),
        POCItem(
            route = "comparison",
            title = "Side-by-Side Comparison",
            description = "Compare unoptimized vs optimized WebView performance in real-time with metrics.",
            icon = Icons.Default.CompareArrows,
            badgeText = "⚖️ COMPARE",
            badgeColor = Color(0xFFF57C00)
        ),
        POCItem(
            route = "webviewclient",
            title = "WebViewClient Optimizations",
            description = "Demonstrates WebViewClient and ChromeClient optimizations for resource loading and rendering.",
            icon = Icons.Default.Settings,
            badgeText = "🔧 CLIENT",
            badgeColor = Color(0xFF1976D2)
        ),
        POCItem(
            route = "caching",
            title = "Caching Strategies",
            description = "Different caching modes and their impact on load times and data usage.",
            icon = Icons.Default.Storage,
            badgeText = "💾 CACHE",
            badgeColor = Color(0xFF7B1FA2)
        ),
        POCItem(
            route = "rendering",
            title = "Rendering Performance",
            description = "Hardware acceleration, layer types, and rendering optimizations.",
            icon = Icons.Default.Palette,
            badgeText = "🎨 RENDER",
            badgeColor = Color(0xFF0288D1)
        ),
        POCItem(
            route = "jsbridge",
            title = "JavaScript Bridge",
            description = "Optimized JavaScript-to-Native communication patterns and performance.",
            icon = Icons.Default.Code,
            badgeText = "🌉 BRIDGE",
            badgeColor = Color(0xFFFFA726)
        ),
        POCItem(
            route = "settings",
            title = "Settings Explorer",
            description = "Interactive explorer for all WebView settings with real-time preview.",
            icon = Icons.Default.Tune,
            badgeText = "⚙️ SETTINGS",
            badgeColor = Color(0xFF616161)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pocItems) { item ->
            POCCard(
                item = item,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

@Composable
fun POCCard(
    item: POCItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = item.badgeColor,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = item.badgeText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
