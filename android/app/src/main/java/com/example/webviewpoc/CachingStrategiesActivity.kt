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
 * POC 5: Caching Strategies
 *
 * Demonstrates different WebView caching modes and their impact on:
 * - Load times
 * - Data usage
 * - Offline capability
 */
class CachingStrategiesActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var metricsText: TextView
    private lateinit var cacheInfoText: TextView
    private lateinit var cacheModeGroup: RadioGroup
    private lateinit var loadButton: Button
    private lateinit var clearCacheButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var loadStartTime: Long = 0
    private val loadTimes = mutableMapOf<String, MutableList<Long>>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caching)

        supportActionBar?.title = "💾 Caching Strategies"

        webView = findViewById(R.id.webView)
        metricsText = findViewById(R.id.metricsText)
        cacheInfoText = findViewById(R.id.cacheInfoText)
        cacheModeGroup = findViewById(R.id.cacheModeGroup)
        loadButton = findViewById(R.id.loadButton)
        clearCacheButton = findViewById(R.id.clearCacheButton)

        configureWebView()
        setupCacheModeSelection()
        displayCacheInfo()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings

        // Basic optimizations
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setAppCacheEnabled(true)
        settings.setAppCachePath(cacheDir.absolutePath)
        settings.databaseEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                loadStartTime = System.currentTimeMillis()
                updateMetrics("Loading...")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val loadTime = System.currentTimeMillis() - loadStartTime
                val currentMode = getCacheModeName(webView.settings.cacheMode)

                if (!loadTimes.containsKey(currentMode)) {
                    loadTimes[currentMode] = mutableListOf()
                }
                loadTimes[currentMode]?.add(loadTime)

                updateMetrics("Loaded in ${loadTime}ms")
                displayCacheInfo()
            }
        }
    }

    private fun setupCacheModeSelection() {
        loadButton.setOnClickListener {
            val selectedMode = when (cacheModeGroup.checkedRadioButtonId) {
                R.id.radioLoadDefault -> WebSettings.LOAD_DEFAULT
                R.id.radioLoadCacheElseNetwork -> WebSettings.LOAD_CACHE_ELSE_NETWORK
                R.id.radioLoadNoCache -> WebSettings.LOAD_NO_CACHE
                R.id.radioLoadCacheOnly -> WebSettings.LOAD_CACHE_ONLY
                else -> WebSettings.LOAD_DEFAULT
            }

            applyCacheMode(selectedMode)
            webView.loadUrl("file:///android_asset/test_page.html")
        }

        clearCacheButton.setOnClickListener {
            webView.clearCache(true)
            webView.clearHistory()
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            loadTimes.clear()

            handler.post {
                cacheInfoText.text = "✓ Cache cleared!"
                displayCacheInfo()
            }
        }

        // Load with default mode initially
        cacheModeGroup.check(R.id.radioLoadCacheElseNetwork)
        applyCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)
    }

    private fun applyCacheMode(mode: Int) {
        webView.settings.cacheMode = mode
        displayCacheModeInfo(mode)
    }

    private fun displayCacheModeInfo(mode: Int) {
        val info = when (mode) {
            WebSettings.LOAD_DEFAULT -> """
                📱 LOAD_DEFAULT (Recommended for most cases)

                Behavior:
                • Uses cached resources when available
                • Checks freshness based on HTTP headers
                • Falls back to network if cache is stale
                • Respects Cache-Control headers

                Best for:
                • Normal web browsing
                • Apps with good network
                • Dynamic content

                Performance: ★★★★☆
                Data Usage: ★★★☆☆
                Offline: ★★☆☆☆
            """.trimIndent()

            WebSettings.LOAD_CACHE_ELSE_NETWORK -> """
                ⚡ LOAD_CACHE_ELSE_NETWORK (Best Performance)

                Behavior:
                • Always uses cache if available
                • Ignores cache expiration
                • Only goes to network if not cached
                • Fastest loading times

                Best for:
                • Content that doesn't change often
                • Offline-first apps
                • Maximum performance

                Performance: ★★★★★
                Data Usage: ★★★★★
                Offline: ★★★★☆
            """.trimIndent()

            WebSettings.LOAD_NO_CACHE -> """
                🔄 LOAD_NO_CACHE (Always Fresh)

                Behavior:
                • Never uses cache
                • Always fetches from network
                • Slowest loading times
                • Highest data usage

                Best for:
                • Real-time data
                • Testing/debugging
                • When cache causes issues

                Performance: ★☆☆☆☆
                Data Usage: ★☆☆☆☆
                Offline: ☆☆☆☆☆
            """.trimIndent()

            WebSettings.LOAD_CACHE_ONLY -> """
                💾 LOAD_CACHE_ONLY (Offline Only)

                Behavior:
                • Only uses cached resources
                • Never goes to network
                • Fails if resource not cached
                • True offline mode

                Best for:
                • Offline apps
                • Kiosk mode
                • Pre-cached content

                Performance: ★★★★★
                Data Usage: ★★★★★
                Offline: ★★★★★
            """.trimIndent()

            else -> "Unknown cache mode"
        }

        cacheInfoText.text = info
    }

    private fun displayCacheInfo() {
        handler.postDelayed({
            val cacheSize = calculateCacheSize()
            val cacheStats = buildString {
                append("📊 CACHE STATISTICS\n\n")
                append("Cache Size: ${formatBytes(cacheSize)}\n")
                append("Cache Location: ${cacheDir.absolutePath}\n\n")

                append("⏱️ LOAD TIME COMPARISON:\n\n")
                loadTimes.forEach { (mode, times) ->
                    val avg = times.average().toLong()
                    val count = times.size
                    append("$mode:\n")
                    append("  Average: ${avg}ms\n")
                    append("  Loads: $count\n\n")
                }

                if (loadTimes.size > 1) {
                    val noCacheTimes = loadTimes["LOAD_NO_CACHE"]
                    val cachedTimes = loadTimes["LOAD_CACHE_ELSE_NETWORK"]

                    if (noCacheTimes != null && cachedTimes != null &&
                        noCacheTimes.isNotEmpty() && cachedTimes.isNotEmpty()) {
                        val noCacheAvg = noCacheTimes.average()
                        val cachedAvg = cachedTimes.average()
                        val improvement = ((noCacheAvg - cachedAvg) / noCacheAvg * 100).toInt()

                        append("✨ CACHE BENEFIT:\n")
                        append("  ${improvement}% faster with cache\n")
                        append("  ${(noCacheAvg - cachedAvg).toLong()}ms saved\n")
                    }
                }
            }

            metricsText.text = cacheStats
        }, 100)
    }

    private fun calculateCacheSize(): Long {
        var size = 0L
        cacheDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun getCacheModeName(mode: Int): String {
        return when (mode) {
            WebSettings.LOAD_DEFAULT -> "LOAD_DEFAULT"
            WebSettings.LOAD_CACHE_ELSE_NETWORK -> "LOAD_CACHE_ELSE_NETWORK"
            WebSettings.LOAD_NO_CACHE -> "LOAD_NO_CACHE"
            WebSettings.LOAD_CACHE_ONLY -> "LOAD_CACHE_ONLY"
            else -> "UNKNOWN"
        }
    }

    private fun updateMetrics(status: String) {
        handler.post {
            supportActionBar?.subtitle = status
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
