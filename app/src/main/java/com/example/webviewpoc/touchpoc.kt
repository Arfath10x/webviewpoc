package com.example.webviewpoc


import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber

@Composable
fun WebViewTouchPOC(
    modifier: Modifier = Modifier
) {
    var touchEventLog by remember { mutableStateOf("") }
    var keyEventLog by remember { mutableStateOf("") }
    var zoomLevel by remember { mutableStateOf(100f) }

    Column(modifier = modifier.fillMaxSize()) {
        // Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Touch Events: $touchEventLog", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Key Events: $keyEventLog", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Zoom: ${zoomLevel.toInt()}%", style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { /* Will be set via webview */ }) {
                        Text("Zoom In")
                    }
                    Button(onClick = { /* Will be set via webview */ }) {
                        Text("Zoom Out")
                    }
                    Button(onClick = { /* Will be set via webview */ }) {
                        Text("Reset")
                    }
                }
            }
        }

        // WebView with custom touch/key handling
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    configureWebViewSettings(this)

                    // Custom touch event handling
                    setOnTouchListener { view, event ->
                        val action = when (event.action) {
                            MotionEvent.ACTION_DOWN -> "DOWN"
                            MotionEvent.ACTION_MOVE -> "MOVE"
                            MotionEvent.ACTION_UP -> "UP"
                            MotionEvent.ACTION_POINTER_DOWN -> "MULTI_DOWN"
                            MotionEvent.ACTION_POINTER_UP -> "MULTI_UP"
                            else -> "OTHER"
                        }

                        touchEventLog = "$action at (${event.x.toInt()}, ${event.y.toInt()}) - Pointers: ${event.pointerCount}"
                        Timber.d("Touch: $touchEventLog")

                        // Allow WebView to handle the event
                        false
                    }

                    // Custom key event handling
                    setOnKeyListener { _, keyCode, event ->
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            val keyName = KeyEvent.keyCodeToString(keyCode)
                            keyEventLog = "Key: $keyName"
                            Timber.d("Key pressed: $keyName")

                            // Handle specific keys (e.g., volume keys for zoom)
                            when (keyCode) {
                                KeyEvent.KEYCODE_VOLUME_UP -> {
                                    zoomIn()
                                    zoomLevel = (zoomLevel + 10f).coerceAtMost(300f)
                                    true
                                }
                                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                    zoomOut()
                                    zoomLevel = (zoomLevel - 10f).coerceAtLeast(50f)
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }

                    // Enable zoom controls
                    settings.apply {
                        builtInZoomControls = true
                        displayZoomControls = false // Hide the on-screen zoom controls
                        setSupportZoom(true)

                        // For pinch-to-zoom
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }

                    // Load test HTML with WebGL canvas
                    loadDataWithBaseURL(
                        "https://example.com",
                        getWebGLTestHTML(),
                        "text/html",
                        "UTF-8",
                        null
                    )

                    // JavaScript interface for zoom control
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun onZoomChange(level: Float) {
                            zoomLevel = level
                        }
                    }, "AndroidBridge")
                }
            },
            update = { webView ->
                // Update can be used for dynamic changes
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun configureWebViewSettings(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true

        // Hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // WebGL specific
        allowFileAccess = true
        allowContentAccess = true
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mediaPlaybackRequiresUserGesture = false

        // Performance
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
    }

    WebView.setWebContentsDebuggingEnabled(true)
}

private fun getWebGLTestHTML(): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes">
    <style>
        body { margin: 0; padding: 0; overflow: hidden; touch-action: none; }
        canvas { display: block; width: 100%; height: 100vh; }
        #info {
            position: absolute;
            top: 10px;
            left: 10px;
            background: rgba(0,0,0,0.7);
            color: white;
            padding: 10px;
            font-family: monospace;
            font-size: 12px;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <div id="info">
        Touch: <span id="touch">-</span><br>
        Zoom: <span id="zoom">100%</span><br>
        FPS: <span id="fps">0</span><br>
        Keys: <span id="keys">-</span>
    </div>
    <canvas id="glCanvas"></canvas>
    
    <script>
        const canvas = document.getElementById('glCanvas');
        const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
        
        if (!gl) {
            alert('WebGL not supported!');
        }
        
        // Resize canvas
        function resizeCanvas() {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
            if (gl) {
                gl.viewport(0, 0, canvas.width, canvas.height);
            }
        }
        window.addEventListener('resize', resizeCanvas);
        resizeCanvas();
        
        // Touch event tracking
        let touchCount = 0;
        let lastTouchDistance = 0;
        
        canvas.addEventListener('touchstart', (e) => {
            touchCount = e.touches.length;
            document.getElementById('touch').textContent = 
                touchCount + ' finger(s) at (' + 
                Math.round(e.touches[0].clientX) + ',' + 
                Math.round(e.touches[0].clientY) + ')';
                
            if (touchCount === 2) {
                const touch1 = e.touches[0];
                const touch2 = e.touches[1];
                lastTouchDistance = Math.hypot(
                    touch2.clientX - touch1.clientX,
                    touch2.clientY - touch1.clientY
                );
            }
        });
        
        canvas.addEventListener('touchmove', (e) => {
            e.preventDefault();
            
            if (e.touches.length === 2) {
                const touch1 = e.touches[0];
                const touch2 = e.touches[1];
                const distance = Math.hypot(
                    touch2.clientX - touch1.clientX,
                    touch2.clientY - touch1.clientY
                );
                
                const scale = distance / lastTouchDistance;
                const newZoom = Math.round(currentZoom * scale);
                document.getElementById('zoom').textContent = newZoom + '%';
                
                // Notify Android
                if (typeof AndroidBridge !== 'undefined') {
                    AndroidBridge.onZoomChange(newZoom);
                }
                
                lastTouchDistance = distance;
                currentZoom = newZoom;
            } else if (e.touches.length === 1) {
                document.getElementById('touch').textContent = 
                    'Moving at (' + 
                    Math.round(e.touches[0].clientX) + ',' + 
                    Math.round(e.touches[0].clientY) + ')';
            }
        });
        
        canvas.addEventListener('touchend', (e) => {
            if (e.touches.length === 0) {
                document.getElementById('touch').textContent = 'Released';
            }
        });
        
        // Keyboard events
        let currentZoom = 100;
        document.addEventListener('keydown', (e) => {
            document.getElementById('keys').textContent = e.key + ' (' + e.keyCode + ')';
            
            // Zoom with +/- keys
            if (e.key === '+' || e.key === '=') {
                currentZoom = Math.min(300, currentZoom + 10);
                document.getElementById('zoom').textContent = currentZoom + '%';
            } else if (e.key === '-' || e.key === '_') {
                currentZoom = Math.max(50, currentZoom - 10);
                document.getElementById('zoom').textContent = currentZoom + '%';
            }
        });
        
        // Mouse wheel zoom
        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? -10 : 10;
            currentZoom = Math.max(50, Math.min(300, currentZoom + delta));
            document.getElementById('zoom').textContent = currentZoom + '%';
        });
        
        // Simple WebGL rendering for testing
        let rotation = 0;
        let lastTime = 0;
        let frameCount = 0;
        let fps = 0;
        
        function render(timestamp) {
            // FPS calculation
            frameCount++;
            if (timestamp - lastTime >= 1000) {
                fps = frameCount;
                frameCount = 0;
                lastTime = timestamp;
                document.getElementById('fps').textContent = fps;
            }
            
            // Clear with rotating color
            rotation += 0.01;
            const r = Math.sin(rotation) * 0.5 + 0.5;
            const g = Math.sin(rotation + 2) * 0.5 + 0.5;
            const b = Math.sin(rotation + 4) * 0.5 + 0.5;
            
            gl.clearColor(r, g, b, 1.0);
            gl.clear(gl.COLOR_BUFFER_BIT);
            
            requestAnimationFrame(render);
        }
        
        requestAnimationFrame(render);
        
        console.log('WebGL version:', gl.getParameter(gl.VERSION));
        console.log('WebGL vendor:', gl.getParameter(gl.VENDOR));
        console.log('WebGL renderer:', gl.getParameter(gl.RENDERER));
    </script>
</body>
</html>
    """.trimIndent()
}