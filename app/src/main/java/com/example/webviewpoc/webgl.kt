package com.example.webviewpoc


import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber

/**
 * POC for WebGL optimization and hardware acceleration
 *
 * Key optimizations for Android 15+:
 * 1. Hardware acceleration (LAYER_TYPE_HARDWARE)
 * 2. WebGL 2.0 support
 * 3. GPU process optimization
 * 4. Memory management
 * 5. Render priority
 */
@Composable
fun WebGLOptimizationPOC(
    modifier: Modifier = Modifier
) {
    var fps by remember { mutableStateOf(0) }
    var triangleCount by remember { mutableStateOf(1000) }
    var consoleLog by remember { mutableStateOf("") }
    var hardwareInfo by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "WebGL Performance Monitor",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("FPS: $fps", style = MaterialTheme.typography.bodyLarge)
                Text("Triangles: $triangleCount", style = MaterialTheme.typography.bodySmall)

                Slider(
                    value = triangleCount.toFloat(),
                    onValueChange = { triangleCount = it.toInt() },
                    valueRange = 100f..10000f,
                    steps = 99
                )

                Text("Hardware Info:", style = MaterialTheme.typography.bodySmall)
                Text(hardwareInfo, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Console Output
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Console Output", style = MaterialTheme.typography.titleSmall)
                Text(
                    consoleLog,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        // WebView with optimized WebGL
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    configureOptimalWebGLSettings(this)

                    // Custom WebChrome for console messages
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            val msg = "${message.messageLevel()}: ${message.message()}"
                            consoleLog = msg
                            Timber.d("WebGL Console: $msg")

                            // Parse FPS from console if available
                            if (message.message().startsWith("FPS:")) {
                                message.message().substringAfter("FPS: ").toIntOrNull()?.let {
                                    fps = it
                                }
                            }

                            return true
                        }
                    }

                    // Get hardware info
                    hardwareInfo = getHardwareAccelerationInfo(this)

                    loadDataWithBaseURL(
                        "https://example.com",
                        getOptimizedWebGLHTML(triangleCount),
                        "text/html",
                        "UTF-8",
                        null
                    )

                    // JavaScript interface for bidirectional communication
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun updateTriangleCount(count: Int) {
                            triangleCount = count
                        }

                        @android.webkit.JavascriptInterface
                        fun updateFPS(fpsValue: Int) {
                            fps = fpsValue
                        }

                        @android.webkit.JavascriptInterface
                        fun logMessage(message: String) {
                            Timber.d("WebGL: $message")
                        }
                    }, "AndroidBridge")
                }
            },
            update = { webView ->
                // Update triangle count in JavaScript
                webView.evaluateJavascript(
                    "if(typeof updateTriangleCount !== 'undefined') updateTriangleCount($triangleCount);",
                    null
                )
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun configureOptimalWebGLSettings(webView: WebView) {
    // Force hardware acceleration - CRITICAL for WebGL
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
        // Enable anti-aliasing for smoother rendering
        isAntiAlias = true
        isFilterBitmap = true
    })

    webView.settings.apply {
        // Essential JavaScript
        javaScriptEnabled = true
        javaScriptCanOpenWindowsAutomatically = true

        // Storage for WebGL resources
        domStorageEnabled = true
        databaseEnabled = true

        // File access for loading 3D models/textures
        allowFileAccess = true
        allowContentAccess = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true

        // Mixed content (if loading from HTTPS/HTTP)
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Media playback
        mediaPlaybackRequiresUserGesture = false

        // Cache strategy - LOAD_DEFAULT for best balance
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        // HIGH render priority for smooth WebGL
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

        // View settings
        useWideViewPort = true
        loadWithOverviewMode = true

        // Disable zoom for 3D viewport
        builtInZoomControls = false
        displayZoomControls = false
        setSupportZoom(false)

        // Save form data disabled for performance
        saveFormData = false
        savePassword = false

        // Android 15+ specific optimizations
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Offscreen pre-raster for better scrolling performance
            offscreenPreRaster = true
        }

        // Enable safe browsing but in background to not block rendering
        safeBrowsingEnabled = false // Disable for POC to avoid overhead
    }

    // Enable remote debugging in debug builds
    WebView.setWebContentsDebuggingEnabled(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)

    Timber.d("WebView configured for optimal WebGL performance")
}

private fun getHardwareAccelerationInfo(webView: WebView): String {
    return buildString {
        appendLine("Hardware Accelerated: ${webView.isHardwareAccelerated}")
        appendLine("Layer Type: ${when(webView.layerType) {
            View.LAYER_TYPE_HARDWARE -> "HARDWARE (Optimal)"
            View.LAYER_TYPE_SOFTWARE -> "SOFTWARE (Slow)"
            View.LAYER_TYPE_NONE -> "NONE"
            else -> "UNKNOWN"
        }}")
        appendLine("Has Window Focus: ${webView.hasWindowFocus()}")
        appendLine("Is Shown: ${webView.isShown}")
        appendLine("Android Version: ${android.os.Build.VERSION.SDK_INT}")
    }
}

private fun getOptimizedWebGLHTML(initialTriangles: Int): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <style>
        * { margin: 0; padding: 0; }
        body { 
            overflow: hidden; 
            background: #000;
            touch-action: none;
        }
        canvas { 
            display: block; 
            width: 100vw;
            height: 100vh;
        }
        #stats {
            position: absolute;
            top: 10px;
            left: 10px;
            color: white;
            font-family: monospace;
            font-size: 12px;
            background: rgba(0,0,0,0.7);
            padding: 10px;
            border-radius: 5px;
            z-index: 1000;
        }
    </style>
</head>
<body>
    <div id="stats"></div>
    <canvas id="glCanvas"></canvas>
    
    <script>
        // ========================================
        // WebGL 2.0 Optimized 3D Rendering POC
        // ========================================
        
        const canvas = document.getElementById('glCanvas');
        const gl = canvas.getContext('webgl2', {
            alpha: false,
            antialias: true,
            depth: true,
            stencil: false,
            preserveDrawingBuffer: false,
            powerPreference: 'high-performance', // Use high-performance GPU
            failIfMajorPerformanceCaveat: false,
            desynchronized: true // Reduce input latency
        }) || canvas.getContext('webgl', {
            alpha: false,
            antialias: true,
            depth: true,
            powerPreference: 'high-performance'
        });
        
        if (!gl) {
            document.body.innerHTML = '<h1 style="color:red">WebGL not supported!</h1>';
            throw new Error('WebGL not supported');
        }
        
        console.log('WebGL Context:', gl instanceof WebGL2RenderingContext ? 'WebGL 2.0' : 'WebGL 1.0');
        console.log('Vendor:', gl.getParameter(gl.VENDOR));
        console.log('Renderer:', gl.getParameter(gl.RENDERER));
        
        // Vertex Shader (optimized)
        const vertexShaderSource = \`#version 300 es
        precision highp float;
        
        in vec3 aPosition;
        in vec3 aColor;
        
        uniform mat4 uModelView;
        uniform mat4 uProjection;
        
        out vec3 vColor;
        
        void main() {
            gl_Position = uProjection * uModelView * vec4(aPosition, 1.0);
            vColor = aColor;
        }
        \`;
        
        // Fragment Shader (optimized)
        const fragmentShaderSource = \`#version 300 es
        precision highp float;
        
        in vec3 vColor;
        out vec4 fragColor;
        
        void main() {
            fragColor = vec4(vColor, 1.0);
        }
        \`;
        
        // Compile shader
        function createShader(type, source) {
            const shader = gl.createShader(type);
            gl.shaderSource(shader, source);
            gl.compileShader(shader);
            
            if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
                console.error('Shader compile error:', gl.getShaderInfoLog(shader));
                gl.deleteShader(shader);
                return null;
            }
            
            return shader;
        }
        
        // Create program
        const vertexShader = createShader(gl.VERTEX_SHADER, vertexShaderSource);
        const fragmentShader = createShader(gl.FRAGMENT_SHADER, fragmentShaderSource);
        
        const program = gl.createProgram();
        gl.attachShader(program, vertexShader);
        gl.attachShader(program, fragmentShader);
        gl.linkProgram(program);
        
        if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
            console.error('Program link error:', gl.getProgramInfoLog(program));
        }
        
        gl.useProgram(program);
        
        // Get uniform locations
        const uModelView = gl.getUniformLocation(program, 'uModelView');
        const uProjection = gl.getUniformLocation(program, 'uProjection');
        
        // Generate dynamic geometry
        let triangleCount = ${initialTriangles};
        let vertices = [];
        let colors = [];
        
        function generateGeometry(count) {
            vertices = [];
            colors = [];
            
            for (let i = 0; i < count; i++) {
                const angle = (i / count) * Math.PI * 2;
                const radius = 0.5 + (i % 10) * 0.05;
                const height = (i / count) * 2 - 1;
                
                // Triangle vertices
                const x1 = Math.cos(angle) * radius;
                const y1 = height;
                const z1 = Math.sin(angle) * radius;
                
                const x2 = Math.cos(angle + 0.1) * radius;
                const y2 = height + 0.1;
                const z2 = Math.sin(angle + 0.1) * radius;
                
                const x3 = 0;
                const y3 = height + 0.05;
                const z3 = 0;
                
                vertices.push(x1, y1, z1, x2, y2, z2, x3, y3, z3);
                
                // Rainbow colors
                const r = Math.sin(angle) * 0.5 + 0.5;
                const g = Math.sin(angle + 2) * 0.5 + 0.5;
                const b = Math.sin(angle + 4) * 0.5 + 0.5;
                
                colors.push(r, g, b, r, g, b, r, g, b);
            }
        }
        
        generateGeometry(triangleCount);
        
        // Create buffers
        const positionBuffer = gl.createBuffer();
        const colorBuffer = gl.createBuffer();
        
        function updateBuffers() {
            gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
            
            gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        }
        
        updateBuffers();
        
        // Setup attributes
        const aPosition = gl.getAttribLocation(program, 'aPosition');
        const aColor = gl.getAttribLocation(program, 'aColor');
        
        // Matrix utilities
        function perspective(fov, aspect, near, far) {
            const f = 1.0 / Math.tan(fov / 2);
            const nf = 1 / (near - far);
            return new Float32Array([
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (far + near) * nf, -1,
                0, 0, 2 * far * near * nf, 0
            ]);
        }
        
        function lookAt(eye, center, up) {
            const z = normalize(subtract(eye, center));
            const x = normalize(cross(up, z));
            const y = cross(z, x);
            
            return new Float32Array([
                x[0], y[0], z[0], 0,
                x[1], y[1], z[1], 0,
                x[2], y[2], z[2], 0,
                -dot(x, eye), -dot(y, eye), -dot(z, eye), 1
            ]);
        }
        
        function subtract(a, b) { return [a[0]-b[0], a[1]-b[1], a[2]-b[2]]; }
        function cross(a, b) { return [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]]; }
        function dot(a, b) { return a[0]*b[0] + a[1]*b[1] + a[2]*b[2]; }
        function normalize(v) { const len = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]); return [v[0]/len, v[1]/len, v[2]/len]; }
        
        // Resize canvas
        function resizeCanvas() {
            canvas.width = window.innerWidth * window.devicePixelRatio;
            canvas.height = window.innerHeight * window.devicePixelRatio;
            gl.viewport(0, 0, canvas.width, canvas.height);
        }
        window.addEventListener('resize', resizeCanvas);
        resizeCanvas();
        
        // Animation
        let rotation = 0;
        let lastTime = performance.now();
        let frameCount = 0;
        let fps = 0;
        let lastFpsUpdate = lastTime;
        
        // Enable depth test
        gl.enable(gl.DEPTH_TEST);
        gl.depthFunc(gl.LEQUAL);
        
        // Enable backface culling for performance
        gl.enable(gl.CULL_FACE);
        gl.cullFace(gl.BACK);
        
        function render(currentTime) {
            frameCount++;
            
            // Update FPS every second
            if (currentTime - lastFpsUpdate >= 1000) {
                fps = Math.round(frameCount * 1000 / (currentTime - lastFpsUpdate));
                frameCount = 0;
                lastFpsUpdate = currentTime;
                
                // Update stats
                document.getElementById('stats').innerHTML = 
                    'FPS: ' + fps + '<br>' +
                    'Triangles: ' + triangleCount + '<br>' +
                    'Vertices: ' + (triangleCount * 3) + '<br>' +
                    'WebGL: ' + (gl instanceof WebGL2RenderingContext ? '2.0' : '1.0');
                
                // Send FPS to Android
                if (typeof AndroidBridge !== 'undefined') {
                    AndroidBridge.updateFPS(fps);
                }
                console.log('FPS: ' + fps);
            }
            
            // Update rotation
            rotation += 0.01;
            
            // Clear
            gl.clearColor(0.1, 0.1, 0.15, 1.0);
            gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
            
            // Setup matrices
            const aspect = canvas.width / canvas.height;
            const projection = perspective(Math.PI / 4, aspect, 0.1, 100.0);
            
            const eye = [
                Math.cos(rotation) * 3,
                1.5,
                Math.sin(rotation) * 3
            ];
            const modelView = lookAt(eye, [0, 0, 0], [0, 1, 0]);
            
            gl.uniformMatrix4fv(uProjection, false, projection);
            gl.uniformMatrix4fv(uModelView, false, modelView);
            
            // Bind buffers and draw
            gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
            gl.vertexAttribPointer(aPosition, 3, gl.FLOAT, false, 0, 0);
            gl.enableVertexAttribArray(aPosition);
            
            gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
            gl.vertexAttribPointer(aColor, 3, gl.FLOAT, false, 0, 0);
            gl.enableVertexAttribArray(aColor);
            
            gl.drawArrays(gl.TRIANGLES, 0, triangleCount * 3);
            
            requestAnimationFrame(render);
        }
        
        // External control from Android
        window.updateTriangleCount = function(count) {
            if (count !== triangleCount) {
                triangleCount = count;
                generateGeometry(triangleCount);
                updateBuffers();
                console.log('Triangle count updated to:', count);
            }
        };
        
        // Start rendering
        requestAnimationFrame(render);
        
        console.log('WebGL rendering started');
    </script>
</body>
</html>
    """.trimIndent()
}