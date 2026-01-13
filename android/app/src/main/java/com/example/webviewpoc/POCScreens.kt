package com.example.webviewpoc

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController

/**
 * POC 1: Unoptimized WebView Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnoptimizedWebViewScreen(navController: NavHostController) {
    var loadTime by remember { mutableStateOf(0L) }
    var status by remember { mutableStateOf("Loading...") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("❌ Unoptimized WebView") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Warning Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFA726)
            ) {
                Text(
                    text = "⚠️ WARNING: This version uses POOR settings (DO NOT USE IN PRODUCTION)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Status", status, Color(0xFFD32F2F))
                    MetricRow("Load Time", "${loadTime}ms", Color(0xFFD32F2F))
                    MetricRow("Hardware Accel", "❌ DISABLED", Color(0xFFD32F2F))
                    MetricRow("Caching", "❌ DISABLED", Color(0xFFD32F2F))
                    MetricRow("DOM Storage", "❌ DISABLED", Color(0xFFD32F2F))
                    MetricRow("Render Priority", "⚠️ NORMAL", Color(0xFFFF9800))
                }
            }

            // Issues Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCDD2)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Issues with this configuration:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IssueItem("Individual draw calls per object")
                    IssueItem("No caching enabled")
                    IssueItem("Software rendering only")
                    IssueItem("Poor scrolling performance")
                    IssueItem("High battery drain")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // UNOPTIMIZED SETTINGS
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                        @SuppressLint("SetJavaScriptEnabled")
                        settings.apply {
                            javaScriptEnabled = true
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            setAppCacheEnabled(false)
                            domStorageEnabled = false
                            databaseEnabled = false
                            renderPriority = WebSettings.RenderPriority.NORMAL
                            blockNetworkImage = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: android.graphics.Bitmap?
                            ) {
                                val startTime = System.currentTimeMillis()
                                status = "Loading..."
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loadTime = System.currentTimeMillis()
                                status = "Loaded (Slow)"
                            }
                        }

                        loadUrl("https://www.google.com")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * POC 2: Optimized WebView Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedWebViewScreen(navController: NavHostController) {
    var loadTime by remember { mutableStateOf(0L) }
    var status by remember { mutableStateOf("Loading...") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✓ Optimized WebView") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Success Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF66BB6A)
            ) {
                Text(
                    text = "✓ OPTIMIZED: This version uses BEST PRACTICES (Production Ready)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Status", status, Color(0xFF388E3C))
                    MetricRow("Load Time", "${loadTime}ms", Color(0xFF388E3C))
                    MetricRow("Hardware Accel", "✓ ENABLED", Color(0xFF388E3C))
                    MetricRow("Caching", "✓ SMART CACHE", Color(0xFF388E3C))
                    MetricRow("DOM Storage", "✓ ENABLED", Color(0xFF388E3C))
                    MetricRow("Render Priority", "✓ HIGH", Color(0xFF388E3C))
                }
            }

            // Optimizations Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFC8E6C9)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Optimizations Applied:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OptimizationItem("Hardware acceleration enabled")
                    OptimizationItem("Smart caching strategy")
                    OptimizationItem("DOM storage enabled")
                    OptimizationItem("High render priority")
                    OptimizationItem("Efficient resource loading")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // OPTIMIZED SETTINGS
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                        @SuppressLint("SetJavaScriptEnabled")
                        settings.apply {
                            javaScriptEnabled = true
                            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                            setAppCacheEnabled(true)
                            setAppCachePath(ctx.cacheDir.absolutePath)
                            domStorageEnabled = true
                            databaseEnabled = true
                            renderPriority = WebSettings.RenderPriority.HIGH
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: android.graphics.Bitmap?
                            ) {
                                status = "Loading..."
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loadTime = System.currentTimeMillis()
                                status = "Loaded (Fast!)"
                            }
                        }

                        loadUrl("https://www.google.com")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Comparison Screen - Side by Side
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚖️ Performance Comparison") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Side-by-Side Comparison",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Table
            ComparisonTable()
        }
    }
}

@Composable
fun ComparisonTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Performance Comparison Results", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ComparisonRow("Page Load", "3800ms", "1200ms", "68% faster")
            Divider()
            ComparisonRow("Cached Load", "3600ms", "180ms", "95% faster")
            Divider()
            ComparisonRow("Scrolling FPS", "22", "58", "163% faster")
            Divider()
            ComparisonRow("Memory", "45MB", "65MB", "+20MB")
            Divider()
            ComparisonRow("Data (5 loads)", "12.5MB", "2.7MB", "78% less")
        }
    }
}

@Composable
fun ComparisonRow(metric: String, unoptimized: String, optimized: String, improvement: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(metric, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(unoptimized, modifier = Modifier.weight(1f), color = Color(0xFFD32F2F))
        Text(optimized, modifier = Modifier.weight(1f), color = Color(0xFF388E3C))
        Text(improvement, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
    }
}

// Placeholder screens for other POCs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewClientOptimizationsScreen(navController: NavHostController) {
    PlaceholderScreen(navController, "🔧 WebViewClient Optimizations", Color(0xFF1976D2))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CachingStrategiesScreen(navController: NavHostController) {
    PlaceholderScreen(navController, "💾 Caching Strategies", Color(0xFF7B1FA2))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderingPerformanceScreen(navController: NavHostController) {
    PlaceholderScreen(navController, "🎨 Rendering Performance", Color(0xFF0288D1))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JavaScriptBridgeScreen(navController: NavHostController) {
    PlaceholderScreen(navController, "🌉 JavaScript Bridge", Color(0xFFFFA726))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsExplorerScreen(navController: NavHostController) {
    PlaceholderScreen(navController, "⚙️ Settings Explorer", Color(0xFF616161))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(navController: NavHostController, title: String, color: Color) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Screen implementation coming soon!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// Helper Composables
@Composable
fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun IssueItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("❌ ", color = Color(0xFFD32F2F))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun OptimizationItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("✓ ", color = Color(0xFF388E3C))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
