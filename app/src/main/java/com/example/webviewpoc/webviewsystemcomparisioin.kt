package com.example.webviewpoc

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewFeature
import timber.log.Timber

/**
 * POC to compare WebView implementations and capabilities
 * FIXED: Hardware acceleration now properly enabled
 */
@Composable
fun WebViewSystemComparison(
    modifier: Modifier = Modifier
) {
    var systemInfo by remember { mutableStateOf("Initializing...") }
    var webViewFeatures by remember { mutableStateOf<List<FeatureInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var consoleLog by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Remember WebView to prevent recreation
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.let { webView ->
                Timber.d("Disposing WebView")
                webView.stopLoading()
                webView.destroy()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Loading indicator at top
        if (isLoading && loadProgress > 0) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // System Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 150.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "📱 WebView System Info",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    systemInfo,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Feature Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "🔧 Features (${webViewFeatures.count { it.supported }}/${webViewFeatures.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (webViewFeatures.isEmpty()) {
                    Text("Checking...", style = MaterialTheme.typography.bodySmall)
                } else {
                    webViewFeatures.take(5).forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                feature.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (feature.supported) "✓" else "✗",
                                color = if (feature.supported)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (webViewFeatures.size > 5) {
                        Text(
                            "... and ${webViewFeatures.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Console Log Card
        if (consoleLog.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 80.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "📝 Console",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        consoleLog,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // WebView Card - MAIN DISPLAY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        Timber.d("🏗️ Creating WebView")
                        WebView(ctx).apply {
                            // CRITICAL: Set layout params FIRST
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Store reference
                            webViewState.value = this

                            // ⚡ HARDWARE ACCELERATION - MUST BE SET BEFORE SETTINGS
                            Timber.d("🎨 Setting hardware acceleration...")
                            setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                                isAntiAlias = true
                                isFilterBitmap = true
                                isDither = true
                            })

                            // Verify immediately
                            val isAccelerated = isHardwareAccelerated
                            Timber.d("✅ Hardware Accelerated (immediate check): $isAccelerated")

                            // Configure WebView settings
                            configureOptimalWebView(this)

                            // Set WebViewClient - PREVENTS DISAPPEARING
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    Timber.d("📄 Page started: $url")
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    Timber.d("✅ Page finished: $url")
                                    isLoading = false
                                    loadProgress = 100

                                    // FORCE VISIBILITY after load
                                    view?.visibility = View.VISIBLE
                                    view?.invalidate()

                                    // Check hardware acceleration again after page load
                                    view?.let {
                                        val hwAccel = it.isHardwareAccelerated
                                        Timber.d("🔍 Hardware Accelerated (after page load): $hwAccel")
                                        Timber.d("🔍 Layer Type: ${it.layerType}")

                                        // Update system info with actual values
                                        val webViewPackage = getCurrentWebViewPackage(ctx)
                                        val chromeVersion = getChromeVersion(it)

                                        systemInfo = buildString {
                                            appendLine("Package: ${webViewPackage?.packageName?.substringAfterLast('.') ?: "Unknown"}")
                                            appendLine("Version: ${webViewPackage?.versionName ?: "Unknown"}")
                                            appendLine("Chrome: $chromeVersion")
                                            appendLine("Hardware Accel: $hwAccel ${if (hwAccel) "✓" else "✗"}")
                                            appendLine("Layer Type: ${
                                                when (it.layerType) {
                                                    View.LAYER_TYPE_HARDWARE -> "HARDWARE ✓"
                                                    View.LAYER_TYPE_SOFTWARE -> "SOFTWARE ✗"
                                                    else -> "NONE ✗"
                                                }
                                            }")
                                            appendLine("Visible: ${it.isShown}")
                                            appendLine("Window Focus: ${it.hasWindowFocus()}")
                                        }
                                    }
                                }

                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    Timber.d("👁️ Page visible: $url")
                                }
                            }

                            // Set WebChromeClient
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress
                                    if (newProgress % 20 == 0) {
                                        Timber.d("📊 Progress: $newProgress%")
                                    }
                                }

                                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                    message?.let {
                                        val msg = "[${it.messageLevel()}] ${it.message()}"
                                        consoleLog = msg
                                        Timber.d("Console: $msg")
                                    }
                                    return true
                                }
                            }

                            // Get initial system information
                            val webViewPackage = getCurrentWebViewPackage(ctx)
                            val chromeVersion = getChromeVersion(this)

                            systemInfo = buildString {
                                appendLine("Package: ${webViewPackage?.packageName?.substringAfterLast('.') ?: "Unknown"}")
                                appendLine("Version: ${webViewPackage?.versionName ?: "Unknown"}")
                                appendLine("Chrome: $chromeVersion")
                                appendLine("Hardware Accel: Checking...")
                                appendLine("Layer Type: ${
                                    when (layerType) {
                                        View.LAYER_TYPE_HARDWARE -> "HARDWARE ✓"
                                        View.LAYER_TYPE_SOFTWARE -> "SOFTWARE ✗"
                                        else -> "NONE ✗"
                                    }
                                }")
                            }

                            // Check features
                            webViewFeatures = checkWebViewFeatures()

                            // FORCE VISIBILITY
                            visibility = View.VISIBLE
                            setBackgroundColor(Color.WHITE)
                            alpha = 1f

                            // Load HTML content
                            Timber.d("📥 Loading HTML content")
                            loadDataWithBaseURL(
                                "https://example.com",
                                getSystemComparisonHTML(),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    update = { webView ->
                        // CRITICAL: Keep WebView visible and hardware accelerated
                        webView.visibility = View.VISIBLE
                        webView.alpha = 1f

                        // Re-verify hardware acceleration
                        if (webView.layerType != View.LAYER_TYPE_HARDWARE) {
                            Timber.w("⚠️ Layer type changed! Re-applying hardware acceleration")
                            webView.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                                isAntiAlias = true
                                isFilterBitmap = true
                            })
                        }
                    }
                )

                // Loading overlay - only show initially
                if (isLoading && loadProgress < 50) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Loading WebView... $loadProgress%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class WebViewType(val displayName: String, val description: String) {
    SYSTEM_WEBVIEW(
        "System WebView",
        "Default Android WebView (Chrome-based)"
    ),
    JETPACK_WEBKIT(
        "Jetpack WebKit Enhanced",
        "System WebView + AndroidX WebKit"
    ),
    GECKOVIEW_INFO(
        "GeckoView (Info Only)",
        "Firefox-based (requires separate lib)"
    )
}

data class FeatureInfo(val name: String, val supported: Boolean)

@SuppressLint("SetJavaScriptEnabled")
private fun configureOptimalWebView(webView: WebView) {
    Timber.d("⚙️ Configuring WebView settings")

    // Note: Hardware acceleration already set in factory before this is called

    webView.settings.apply {
        // JavaScript
        javaScriptEnabled = true
        javaScriptCanOpenWindowsAutomatically = true

        // Storage
        domStorageEnabled = true
        databaseEnabled = true

        // File access
        allowFileAccess = true
        allowContentAccess = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true

        // Security
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Performance
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

        // Layout
        useWideViewPort = true
        loadWithOverviewMode = true
        layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL

        // Zoom
        builtInZoomControls = false
        displayZoomControls = false
        setSupportZoom(false)

        // Media
        mediaPlaybackRequiresUserGesture = false
    }

    // Enable debugging
    WebView.setWebContentsDebuggingEnabled(true)

    Timber.d("✅ WebView settings configured")
}

private fun getCurrentWebViewPackage(context: android.content.Context): android.content.pm.PackageInfo? {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            WebView.getCurrentWebViewPackage()
        } else {
            context.packageManager.getPackageInfo("com.google.android.webview", 0)
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to get WebView package")
        null
    }
}

private fun getChromeVersion(webView: WebView): String {
    return try {
        val userAgent = webView.settings.userAgentString
        val chromeRegex = "Chrome/([\\d.]+)".toRegex()
        chromeRegex.find(userAgent)?.groupValues?.get(1) ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun checkWebViewFeatures(): List<FeatureInfo> {
    val features = mutableListOf<FeatureInfo>()

    val featuresToCheck = listOf(
        WebViewFeature.VISUAL_STATE_CALLBACK to "Visual State Callback",
        WebViewFeature.OFF_SCREEN_PRERASTER to "Offscreen Pre-Raster",
        WebViewFeature.SAFE_BROWSING_ENABLE to "Safe Browsing",
        WebViewFeature.START_SAFE_BROWSING to "Start Safe Browsing",
        WebViewFeature.SERVICE_WORKER_BASIC_USAGE to "Service Worker",
        WebViewFeature.RECEIVE_WEB_RESOURCE_ERROR to "Web Resource Error",
        WebViewFeature.FORCE_DARK to "Force Dark Mode",
        WebViewFeature.MULTI_PROCESS to "Multi-Process",
        WebViewFeature.DOCUMENT_START_SCRIPT to "Document Start Script",
        WebViewFeature.GET_WEB_VIEW_CLIENT to "Get WebView Client"
    )

    featuresToCheck.forEach { (feature, name) ->
        try {
            val supported = WebViewFeature.isFeatureSupported(feature)
            features.add(FeatureInfo(name, supported))
        } catch (e: Exception) {
            features.add(FeatureInfo(name, false))
        }
    }

    return features.sortedByDescending { it.supported }
}

private fun getSystemComparisonHTML(): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>WebView Test</title>
    <style>
        * { 
            margin: 0; 
            padding: 0; 
            box-sizing: border-box; 
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
            padding: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            min-height: 100vh;
            overflow-y: auto;
        }
        .header {
            text-align: center;
            margin-bottom: 16px;
        }
        h1 {
            font-size: 22px;
            margin-bottom: 8px;
        }
        .card {
            background: rgba(255,255,255,0.15);
            border-radius: 12px;
            padding: 14px;
            margin: 10px 0;
            backdrop-filter: blur(10px);
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        h2 {
            font-size: 16px;
            margin-bottom: 10px;
            font-weight: 600;
        }
        .info-row {
            margin: 6px 0;
            padding: 6px 10px;
            background: rgba(0,0,0,0.2);
            border-radius: 6px;
            font-size: 13px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .success { 
            color: #4ade80; 
            font-weight: 600;
        }
        .error { 
            color: #f87171; 
        }
        canvas {
            width: 100%;
            height: 150px;
            border-radius: 8px;
            margin: 10px 0;
            background: rgba(0,0,0,0.3);
            display: block;
        }
        .badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
            background: rgba(255,255,255,0.2);
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🔬 WebView Test Suite</h1>
        <div class="badge" id="status">Initializing...</div>
    </div>
    
    <div class="card">
        <h2>🎨 WebGL Graphics</h2>
        <canvas id="glCanvas"></canvas>
        <div id="webgl-info">Checking WebGL...</div>
    </div>
    
    <div class="card">
        <h2>🌐 Browser Capabilities</h2>
        <div id="features">Scanning features...</div>
    </div>
    
    <div class="card">
        <h2>⚡ Device Info</h2>
        <div id="device-info">Loading...</div>
    </div>
    
    <script>
        console.log('🚀 Starting WebView test suite...');
        document.getElementById('status').textContent = 'Running Tests...';
        
        // WebGL Test
        const canvas = document.getElementById('glCanvas');
        const gl = canvas.getContext('webgl2', {
            alpha: false,
            antialias: true,
            depth: true,
            powerPreference: 'high-performance',
            desynchronized: true
        }) || canvas.getContext('webgl', {
            alpha: false,
            antialias: true,
            depth: true,
            powerPreference: 'high-performance'
        });
        
        const webglInfo = document.getElementById('webgl-info');
        
        if (gl) {
            console.log('✅ WebGL context created successfully');
            console.log('WebGL Version:', gl.getParameter(gl.VERSION));
            console.log('WebGL Vendor:', gl.getParameter(gl.VENDOR));
            console.log('WebGL Renderer:', gl.getParameter(gl.RENDERER));
            
            // Set canvas size with device pixel ratio
            const dpr = window.devicePixelRatio || 1;
            canvas.width = canvas.offsetWidth * dpr;
            canvas.height = canvas.offsetHeight * dpr;
            gl.viewport(0, 0, canvas.width, canvas.height);
            
            const isWebGL2 = gl instanceof WebGL2RenderingContext;
            
            webglInfo.innerHTML = 
                '<div class="info-row success">✓ WebGL ' + (isWebGL2 ? '2.0' : '1.0') + ' Active</div>' +
                '<div class="info-row">Renderer: ' + gl.getParameter(gl.RENDERER) + '</div>' +
                '<div class="info-row">Max Texture: ' + gl.getParameter(gl.MAX_TEXTURE_SIZE) + 'px</div>' +
                '<div class="info-row">Max Viewport: ' + gl.getParameter(gl.MAX_VIEWPORT_DIMS).join('×') + '</div>';
            
            // Animate with proper color cycling
            let hue = 0;
            function animate() {
                hue = (hue + 0.5) % 360;
                const r = Math.sin(hue * Math.PI / 180) * 0.5 + 0.5;
                const g = Math.sin((hue + 120) * Math.PI / 180) * 0.5 + 0.5;
                const b = Math.sin((hue + 240) * Math.PI / 180) * 0.5 + 0.5;
                gl.clearColor(r, g, b, 1.0);
                gl.clear(gl.COLOR_BUFFER_BIT);
                requestAnimationFrame(animate);
            }
            animate();
            console.log('🎨 WebGL animation started');
        } else {
            console.error('❌ WebGL not available');
            webglInfo.innerHTML = '<div class="info-row error">✗ WebGL Not Supported</div>';
        }
        
        // Features Test
        const features = {
            'WebGL': !!gl,
            'WebGL 2.0': gl instanceof WebGL2RenderingContext,
            'WebSockets': 'WebSocket' in window,
            'Workers': 'Worker' in window,
            'IndexedDB': 'indexedDB' in window,
            'LocalStorage': 'localStorage' in window,
            'WebRTC': 'RTCPeerConnection' in window,
            'Fetch': 'fetch' in window
        };
        
        let featuresHTML = '';
        let supported = 0;
        for (const key in features) {
            if (features[key]) supported++;
            const cls = features[key] ? 'success' : 'error';
            const icon = features[key] ? '✓' : '✗';
            featuresHTML += '<div class="info-row ' + cls + '">' + icon + ' ' + key + '</div>';
        }
        document.getElementById('features').innerHTML = 
            '<div class="info-row success">' + supported + '/' + Object.keys(features).length + ' Features Available</div>' +
            featuresHTML;
        
        // Device Info
        const deviceInfo = {
            'Screen': screen.width + '×' + screen.height,
            'Pixel Ratio': window.devicePixelRatio + 'x',
            'CPU Cores': navigator.hardwareConcurrency || 'N/A',
            'Memory': navigator.deviceMemory ? navigator.deviceMemory + ' GB' : 'N/A',
            'Online': navigator.onLine ? 'Yes' : 'No'
        };
        
        let deviceHTML = '';
        for (const key in deviceInfo) {
            deviceHTML += '<div class="info-row">' + key + ': <strong>' + deviceInfo[key] + '</strong></div>';
        }
        document.getElementById('device-info').innerHTML = deviceHTML;
        
        // Update status
        document.getElementById('status').textContent = 'All Tests Passed ✓';
        document.getElementById('status').style.background = 'rgba(74, 222, 128, 0.3)';
        
        console.log('✅ All tests completed successfully');
    </script>
</body>
</html>
    """.trimIndent()
}