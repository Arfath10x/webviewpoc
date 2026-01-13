package com.example.webviewpoc

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * POC 6: Rendering Performance
 *
 * Demonstrates different rendering modes and their impact on:
 * - Scrolling performance
 * - Animation smoothness
 * - Battery usage
 * - Memory usage
 */
class RenderingPerformanceActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var metricsText: TextView
    private lateinit var renderModeGroup: RadioGroup
    private lateinit var applyButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var frameCount = 0
    private var fpsStartTime = 0L

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rendering)

        supportActionBar?.title = "🎨 Rendering Performance"

        webView = findViewById(R.id.webView)
        metricsText = findViewById(R.id.metricsText)
        renderModeGroup = findViewById(R.id.renderModeGroup)
        applyButton = findViewById(R.id.applyButton)

        configureWebView()
        setupRenderModeSelection()
        startFPSMonitoring()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings

        // Basic optimizations
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.renderPriority = WebSettings.RenderPriority.HIGH

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Inject FPS counter
                view?.evaluateJavascript("""
                    (function() {
                        var fps = 0;
                        var lastTime = performance.now();
                        var frames = 0;

                        function countFPS() {
                            var currentTime = performance.now();
                            frames++;

                            if (currentTime >= lastTime + 1000) {
                                fps = Math.round((frames * 1000) / (currentTime - lastTime));
                                frames = 0;
                                lastTime = currentTime;

                                // Send FPS to Android
                                console.log('FPS: ' + fps);
                            }

                            requestAnimationFrame(countFPS);
                        }

                        requestAnimationFrame(countFPS);

                        return 'FPS monitoring started';
                    })();
                """.trimIndent(), null)
            }
        }

        // Load animation-heavy test page
        webView.loadUrl("file:///android_asset/test_page_animations.html")
    }

    private fun setupRenderModeSelection() {
        applyButton.setOnClickListener {
            val selectedMode = when (renderModeGroup.checkedRadioButtonId) {
                R.id.radioHardware -> View.LAYER_TYPE_HARDWARE
                R.id.radioSoftware -> View.LAYER_TYPE_SOFTWARE
                R.id.radioNone -> View.LAYER_TYPE_NONE
                else -> View.LAYER_TYPE_HARDWARE
            }

            applyRenderMode(selectedMode)
        }

        // Start with hardware acceleration
        renderModeGroup.check(R.id.radioHardware)
        applyRenderMode(View.LAYER_TYPE_HARDWARE)
    }

    private fun applyRenderMode(layerType: Int) {
        webView.setLayerType(layerType, null)
        displayRenderModeInfo(layerType)

        // Reload to see effect
        webView.reload()
    }

    private fun displayRenderModeInfo(layerType: Int) {
        val info = when (layerType) {
            View.LAYER_TYPE_HARDWARE -> """
                ⚡ HARDWARE ACCELERATION (Recommended)

                Rendering:
                • Uses GPU for rendering
                • Offloads work from CPU
                • Smooth 60 FPS animations
                • Fast scrolling performance

                Best for:
                • All modern devices
                • Complex animations
                • Smooth scrolling
                • Video playback
                • Canvas/WebGL

                Performance: ★★★★★
                Smoothness: ★★★★★
                Battery: ★★★★☆ (GPU efficient)
                Memory: ★★★☆☆ (GPU mem)

                Settings Applied:
                ✓ Hardware layer enabled
                ✓ GPU rendering active
                ✓ Hardware compositor
                ✓ Smooth animations

                Expected FPS: 55-60 FPS
            """.trimIndent()

            View.LAYER_TYPE_SOFTWARE -> """
                💻 SOFTWARE RENDERING (Legacy)

                Rendering:
                • Uses CPU for all rendering
                • No GPU acceleration
                • Slower performance
                • Lower FPS

                Best for:
                • Debugging rendering issues
                • Devices without GPU
                • Compatibility testing
                • Screenshot quality

                Performance: ★★☆☆☆
                Smoothness: ★☆☆☆☆
                Battery: ★★☆☆☆ (CPU intensive)
                Memory: ★★★★☆ (less mem)

                Settings Applied:
                ✓ Software layer enabled
                ✓ CPU rendering only
                ✓ No hardware compositor
                ✓ Skia software renderer

                Expected FPS: 15-30 FPS
            """.trimIndent()

            View.LAYER_TYPE_NONE -> """
                🔧 NO LAYER (Default Behavior)

                Rendering:
                • System decides rendering mode
                • Usually same as hardware
                • Depends on Android version
                • May switch dynamically

                Best for:
                • Default configuration
                • Letting system optimize
                • Mixed content

                Performance: ★★★★☆
                Smoothness: ★★★★☆
                Battery: ★★★★☆
                Memory: ★★★☆☆

                Settings Applied:
                ✓ Default layer type
                ✓ System-managed rendering
                ✓ Auto optimization

                Expected FPS: 45-60 FPS
            """.trimIndent()

            else -> "Unknown render mode"
        }

        metricsText.text = info
    }

    private fun startFPSMonitoring() {
        fpsStartTime = System.currentTimeMillis()

        handler.postDelayed(object : Runnable {
            override fun run() {
                updatePerformanceMetrics()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun updatePerformanceMetrics() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)

        val currentMode = when (webView.layerType) {
            View.LAYER_TYPE_HARDWARE -> "Hardware Acceleration"
            View.LAYER_TYPE_SOFTWARE -> "Software Rendering"
            View.LAYER_TYPE_NONE -> "No Layer (Default)"
            else -> "Unknown"
        }

        handler.post {
            supportActionBar?.subtitle = "$currentMode | Mem: ${usedMemory}MB"
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }
}
