package com.example.webviewpoc


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber

/**
 * POC to compare WebView implementations and capabilities
 *
 * Android 15+ uses:
 * - System WebView (Google Chrome-based)
 * - Jetpack WebKit for additional features
 *
 * For comparison, you could also test:
 * - Mozilla GeckoView (Firefox-based) - requires separate integration
 * - Chromium WebView (requires building Chromium)
 */
@Composable
fun WebViewSystemComparison(
    modifier: Modifier = Modifier
) {
    var systemInfo by remember { mutableStateOf("Loading...") }
    var webViewFeatures by remember { mutableStateOf<List<FeatureInfo>>(emptyList()) }
    var selectedView by remember { mutableStateOf(WebViewType.SYSTEM_WEBVIEW) }

    LaunchedEffect(Unit) {
        // This will be populated when WebView is created
    }

    Column(modifier = modifier.fillMaxSize()) {
        // System Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "WebView System Information",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    systemInfo,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Feature Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Jetpack WebKit Feature Support",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (webViewFeatures.isEmpty()) {
                    Text("Checking features...", style = MaterialTheme.typography.bodySmall)
                } else {
                    webViewFeatures.forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
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
                }
            }
        }

        // WebView Type Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("WebView Type", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                WebViewType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = selectedView == type,
                            onClick = { selectedView = type }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type.displayName)
                            Text(
                                type.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // WebView Display
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    configureOptimalWebView(this)

                    // Get system information
                    val packageManager = context.packageManager
                    val webViewPackage = getCurrentWebViewPackage(context)
                    val chromeVersion = getChromeVersion(this)

                    systemInfo = buildString {
                        appendLine("WebView Package: ${webViewPackage?.packageName ?: "Unknown"}")
                        appendLine("Version: ${webViewPackage?.versionName ?: "Unknown"}")
                        appendLine("Chrome Version: $chromeVersion")
                        appendLine("WebView Class: ${this@apply.javaClass.name}")
                        appendLine("Hardware Accelerated: ${isHardwareAccelerated}")
                        appendLine("Layer Type: ${when(layerType) {
                            android.view.View.LAYER_TYPE_HARDWARE -> "HARDWARE"
                            android.view.View.LAYER_TYPE_SOFTWARE -> "SOFTWARE"
                            else -> "NONE"
                        }}")
                    }

                    // Check Jetpack WebKit features
                    webViewFeatures = checkWebViewFeatures()

                    loadDataWithBaseURL(
                        "https://example.com",
                        getSystemComparisonHTML(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}

enum class WebViewType(val displayName: String, val description: String) {
    SYSTEM_WEBVIEW(
        "System WebView",
        "Default Android WebView (Chrome-based on Android 15+)"
    ),
    JETPACK_WEBKIT(
        "Jetpack WebKit Enhanced",
        "System WebView + AndroidX WebKit features"
    ),
    // Note: GeckoView would require separate dependency and implementation
    GECKOVIEW_INFO(
        "GeckoView (Info Only)",
        "Firefox-based, requires: org.mozilla.geckoview:geckoview"
    )
}

data class FeatureInfo(val name: String, val supported: Boolean)

@SuppressLint("SetJavaScriptEnabled")
private fun configureOptimalWebView(webView: WebView) {
    webView.settings.apply {
        // Essential settings
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true

        // WebGL and Graphics
        javaScriptCanOpenWindowsAutomatically = true
        allowFileAccess = true
        allowContentAccess = true
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Performance optimizations
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

        // Hardware acceleration (critical for WebGL)
        webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

        // View settings
        useWideViewPort = true
        loadWithOverviewMode = true
        builtInZoomControls = false
        setSupportZoom(false)

        // Media
        mediaPlaybackRequiresUserGesture = false

        // Android 15+ specific optimizations
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Enable any Android 15 specific features here
            // offscreenPreRaster = true // Example of potential new feature
        }
    }

    WebView.setWebContentsDebuggingEnabled(true)
}

private fun getCurrentWebViewPackage(context: android.content.Context): android.content.pm.PackageInfo? {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.webkit.WebView.getCurrentWebViewPackage()
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

    // Check all available Jetpack WebKit features
    val featuresToCheck = listOf(
        // Core features
        WebViewFeature.VISUAL_STATE_CALLBACK to "Visual State Callback",
        WebViewFeature.OFF_SCREEN_PRERASTER to "Offscreen Pre-Raster",
        WebViewFeature.SAFE_BROWSING_ENABLE to "Safe Browsing",
        WebViewFeature.DISABLED_ACTION_MODE_MENU_ITEMS to "Disabled Action Menu Items",
        WebViewFeature.START_SAFE_BROWSING to "Start Safe Browsing",
        WebViewFeature.SAFE_BROWSING_WHITELIST to "Safe Browsing Whitelist",

        // Web functionality
        WebViewFeature.WEB_RESOURCE_REQUEST_IS_REDIRECT to "Resource Request Redirect",
        WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION to "Resource Error Description",
        WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE to "Resource Error Code",
        WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY to "Safe Browse Back",
        WebViewFeature.SAFE_BROWSING_RESPONSE_PROCEED to "Safe Browse Proceed",
        WebViewFeature.SAFE_BROWSING_RESPONSE_SHOW_INTERSTITIAL to "Safe Browse Interstitial",

        // Service Worker
        WebViewFeature.SERVICE_WORKER_BASIC_USAGE to "Service Worker Basic",
        WebViewFeature.SERVICE_WORKER_CACHE_MODE to "Service Worker Cache",
        WebViewFeature.SERVICE_WORKER_CONTENT_ACCESS to "Service Worker Content Access",
        WebViewFeature.SERVICE_WORKER_FILE_ACCESS to "Service Worker File Access",
        WebViewFeature.SERVICE_WORKER_BLOCK_NETWORK_LOADS to "Service Worker Block Network",
        WebViewFeature.SERVICE_WORKER_SHOULD_INTERCEPT_REQUEST to "Service Worker Intercept",

        // Advanced features
        WebViewFeature.RECEIVE_WEB_RESOURCE_ERROR to "Receive Web Resource Error",
        WebViewFeature.RECEIVE_HTTP_ERROR to "Receive HTTP Error",
        WebViewFeature.SHOULD_OVERRIDE_WITH_REDIRECTS to "Should Override Redirects",
        WebViewFeature.SAFE_BROWSING_HIT to "Safe Browsing Hit",
        WebViewFeature.WEB_MESSAGE_PORT_POST_MESSAGE to "Web Message Port Post",
        WebViewFeature.WEB_MESSAGE_PORT_CLOSE to "Web Message Port Close",
        WebViewFeature.WEB_MESSAGE_PORT_SET_MESSAGE_CALLBACK to "Web Message Callback",
        WebViewFeature.CREATE_WEB_MESSAGE_CHANNEL to "Create Web Message Channel",
        WebViewFeature.POST_WEB_MESSAGE to "Post Web Message",
        WebViewFeature.WEB_MESSAGE_CALLBACK_ON_MESSAGE to "Web Message Callback",

        // Proxy
        WebViewFeature.PROXY_OVERRIDE to "Proxy Override",

        // Force dark
        WebViewFeature.FORCE_DARK to "Force Dark Mode",
        WebViewFeature.FORCE_DARK_STRATEGY to "Force Dark Strategy",

        // Get Variations Header
        WebViewFeature.GET_VARIATIONS_HEADER to "Get Variations Header",

        // Multi-process
        WebViewFeature.MULTI_PROCESS to "Multi-Process",

        // Tracing
        WebViewFeature.TRACING_CONTROLLER_BASIC_USAGE to "Tracing Controller",

        // Document start JS
        WebViewFeature.DOCUMENT_START_SCRIPT to "Document Start Script",

        // Attributes
        WebViewFeature.GET_WEB_VIEW_CLIENT to "Get WebView Client",
        WebViewFeature.GET_WEB_CHROME_CLIENT to "Get WebChrome Client"
    )

    featuresToCheck.forEach { (feature, name) ->
        try {
            val supported = WebViewFeature.isFeatureSupported(feature)
            features.add(FeatureInfo(name, supported))
        } catch (e: Exception) {
            features.add(FeatureInfo(name, false))
            Timber.e(e, "Error checking feature: $name")
        }
    }

    return features
}

private fun getSystemComparisonHTML(): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .card {
            background: rgba(255,255,255,0.1);
            border-radius: 10px;
            padding: 15px;
            margin: 10px 0;
            backdrop-filter: blur(10px);
        }
        .test-result {
            margin: 5px 0;
            padding: 5px;
            background: rgba(0,0,0,0.2);
            border-radius: 5px;
        }
        .success { color: #4ade80; }
        .error { color: #f87171; }
        canvas {
            width: 100%;
            height: 200px;
            border-radius: 10px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <h2>WebView Capability Tests</h2>
    
    <div class="card">
        <h3>WebGL Support</h3>
        <div id="webgl-info"></div>
        <canvas id="webgl-canvas"></canvas>
    </div>
    
    <div class="card">
        <h3>Browser Features</h3>
        <div id="browser-features"></div>
    </div>
    
    <div class="card">
        <h3>Performance Metrics</h3>
        <div id="performance"></div>
    </div>
    
    <script>
        // WebGL Detection and Info
        const canvas = document.getElementById('webgl-canvas');
        const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
        const webglInfo = document.getElementById('webgl-info');
        
        if (gl) {
            const info = {
                'Version': gl.getParameter(gl.VERSION),
                'Vendor': gl.getParameter(gl.VENDOR),
                'Renderer': gl.getParameter(gl.RENDERER),
                'GLSL Version': gl.getParameter(gl.SHADING_LANGUAGE_VERSION),
                'Max Texture Size': gl.getParameter(gl.MAX_TEXTURE_SIZE),
                'Max Viewport': gl.getParameter(gl.MAX_VIEWPORT_DIMS).join(' x '),
                'Max Vertex Attribs': gl.getParameter(gl.MAX_VERTEX_ATTRIBS),
                'Max Varying Vectors': gl.getParameter(gl.MAX_VARYING_VECTORS),
                'Max Fragment Uniforms': gl.getParameter(gl.MAX_FRAGMENT_UNIFORM_VECTORS),
                'Max Vertex Uniforms': gl.getParameter(gl.MAX_VERTEX_UNIFORM_VECTORS),
                'WebGL 2': gl instanceof WebGL2RenderingContext ? 'Yes' : 'No'
            };
            
            let html = '<div class="test-result success">WebGL Supported ✓</div>';
            for (const [key, value] of Object.entries(info)) {
                html += `<div class="test-result">${key}: ${value}</div>`;
            }
            webglInfo.innerHTML = html;
            
            // Simple WebGL animation
            let rotation = 0;
            function animate() {
                rotation += 0.02;
                const r = Math.sin(rotation) * 0.5 + 0.5;
                const g = Math.sin(rotation + 2) * 0.5 + 0.5;
                const b = Math.sin(rotation + 4) * 0.5 + 0.5;
                gl.clearColor(r, g, b, 1.0);
                gl.clear(gl.COLOR_BUFFER_BIT);
                requestAnimationFrame(animate);
            }
            animate();
        } else {
            webglInfo.innerHTML = '<div class="test-result error">WebGL Not Supported ✗</div>';
        }
        
        // Browser Features
        const features = {
            'WebGL': !!gl,
            'WebGL 2': gl instanceof WebGL2RenderingContext,
            'WebSockets': 'WebSocket' in window,
            'Web Workers': 'Worker' in window,
            'Service Workers': 'serviceWorker' in navigator,
            'IndexedDB': 'indexedDB' in window,
            'LocalStorage': 'localStorage' in window,
            'SessionStorage': 'sessionStorage' in window,
            'WebRTC': 'RTCPeerConnection' in window,
            'WebAudio': 'AudioContext' in window || 'webkitAudioContext' in window,
            'Geolocation': 'geolocation' in navigator,
            'File API': 'File' in window,
            'Fetch API': 'fetch' in window,
            'ES6 Modules': 'noModule' in document.createElement('script'),
            'CSS Grid': CSS.supports('display', 'grid'),
            'CSS Flexbox': CSS.supports('display', 'flex')
        };
        
        let featuresHTML = '';
        for (const [key, value] of Object.entries(features)) {
            const className = value ? 'success' : 'error';
            const symbol = value ? '✓' : '✗';
            featuresHTML += `<div class="test-result ${className}">${key}: ${symbol}</div>`;
        }
        document.getElementById('browser-features').innerHTML = featuresHTML;
        
        // Performance Metrics
        const perfInfo = {
            'User Agent': navigator.userAgent,
            'Platform': navigator.platform,
            'Hardware Concurrency': navigator.hardwareConcurrency || 'Unknown',
            'Device Memory': navigator.deviceMemory ? navigator.deviceMemory + ' GB' : 'Unknown',
            'Screen Resolution': screen.width + ' x ' + screen.height,
            'Device Pixel Ratio': window.devicePixelRatio,
            'Connection Type': navigator.connection ? navigator.connection.effectiveType : 'Unknown'
        };
        
        let perfHTML = '';
        for (const [key, value] of Object.entries(perfInfo)) {
            perfHTML += `<div class="test-result">${key}: ${value}</div>`;
        }
        document.getElementById('performance').innerHTML = perfHTML;
        
        // Log to console for debugging
        console.log('WebView System Test Results:', {
            webgl: gl !== null,
            features: features,
            performance: perfInfo
        });
    </script>
</body>
</html>
    """.trimIndent()
}