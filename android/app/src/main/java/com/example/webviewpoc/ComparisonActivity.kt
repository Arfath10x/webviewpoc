package com.example.webviewpoc

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * POC 3: Side-by-Side WebView Comparison
 *
 * This activity shows unoptimized vs optimized WebView side by side
 * with real-time performance metrics comparison.
 */
class ComparisonActivity : AppCompatActivity() {

    private lateinit var unoptimizedWebView: WebView
    private lateinit var optimizedWebView: WebView
    private lateinit var unoptimizedMetrics: TextView
    private lateinit var optimizedMetrics: TextView
    private lateinit var comparisonMetrics: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var unoptimizedStartTime: Long = 0
    private var optimizedStartTime: Long = 0
    private var unoptimizedLoadTime: Long = 0
    private var optimizedLoadTime: Long = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        supportActionBar?.title = "⚖️ WebView Comparison"

        unoptimizedWebView = findViewById(R.id.unoptimizedWebView)
        optimizedWebView = findViewById(R.id.optimizedWebView)
        unoptimizedMetrics = findViewById(R.id.unoptimizedMetrics)
        optimizedMetrics = findViewById(R.id.optimizedMetrics)
        comparisonMetrics = findViewById(R.id.comparisonMetrics)

        configureUnoptimizedWebView()
        configureOptimizedWebView()

        // Load same page in both
        val testUrl = "file:///android_asset/test_page_heavy.html"
        unoptimizedWebView.loadUrl(testUrl)
        optimizedWebView.loadUrl(testUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureUnoptimizedWebView() {
        val settings = unoptimizedWebView.settings

        // UNOPTIMIZED SETTINGS
        unoptimizedWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setAppCacheEnabled(false)
        settings.domStorageEnabled = false
        settings.databaseEnabled = false
        settings.blockNetworkImage = false // Allow images for fair comparison
        settings.javaScriptEnabled = true
        settings.renderPriority = WebSettings.RenderPriority.NORMAL

        unoptimizedWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                unoptimizedStartTime = System.currentTimeMillis()
                updateUnoptimizedMetrics("Loading...", 0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                unoptimizedLoadTime = System.currentTimeMillis() - unoptimizedStartTime
                updateUnoptimizedMetrics("Loaded", unoptimizedLoadTime)
                updateComparison()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureOptimizedWebView() {
        val settings = optimizedWebView.settings

        // OPTIMIZED SETTINGS
        optimizedWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.setAppCacheEnabled(true)
        settings.setAppCachePath(cacheDir.absolutePath)
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.blockNetworkImage = false
        settings.javaScriptEnabled = true
        settings.renderPriority = WebSettings.RenderPriority.HIGH
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        optimizedWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                optimizedStartTime = System.currentTimeMillis()
                updateOptimizedMetrics("Loading...", 0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                optimizedLoadTime = System.currentTimeMillis() - optimizedStartTime
                updateOptimizedMetrics("Loaded", optimizedLoadTime)
                updateComparison()
            }
        }

        optimizedWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                // Could show progress bar here
            }
        }
    }

    private fun updateUnoptimizedMetrics(status: String, loadTime: Long) {
        handler.post {
            unoptimizedMetrics.text = """
                ❌ UNOPTIMIZED

                Status: $status
                Load Time: ${loadTime}ms

                Settings:
                • HW Accel: ❌ OFF
                • Cache: ❌ NONE
                • DOM Storage: ❌ OFF
                • Render: ⚠️ NORMAL

                Expected:
                • Slow load
                • High data usage
                • Poor scrolling
            """.trimIndent()
        }
    }

    private fun updateOptimizedMetrics(status: String, loadTime: Long) {
        handler.post {
            optimizedMetrics.text = """
                ✓ OPTIMIZED

                Status: $status
                Load Time: ${loadTime}ms

                Settings:
                • HW Accel: ✓ ON
                • Cache: ✓ SMART
                • DOM Storage: ✓ ON
                • Render: ✓ HIGH

                Benefits:
                • Fast load
                • Low data usage
                • Smooth scrolling
            """.trimIndent()
        }
    }

    private fun updateComparison() {
        if (unoptimizedLoadTime > 0 && optimizedLoadTime > 0) {
            handler.post {
                val timeDiff = unoptimizedLoadTime - optimizedLoadTime
                val percentFaster = if (unoptimizedLoadTime > 0) {
                    ((timeDiff.toFloat() / unoptimizedLoadTime) * 100).toInt()
                } else 0

                val speedMultiplier = if (optimizedLoadTime > 0) {
                    String.format("%.1f", unoptimizedLoadTime.toFloat() / optimizedLoadTime)
                } else "N/A"

                comparisonMetrics.text = """
                    📊 PERFORMANCE COMPARISON

                    Unoptimized: ${unoptimizedLoadTime}ms
                    Optimized: ${optimizedLoadTime}ms

                    ⚡ Difference: ${timeDiff}ms
                    📈 Faster by: $percentFaster%
                    🚀 Speed: ${speedMultiplier}x faster

                    ${if (percentFaster > 0) "✓ Optimization SUCCESSFUL!" else "⚠️ Optimization needed"}

                    Key Improvements:
                    • Hardware acceleration
                    • Smart caching
                    • DOM storage enabled
                    • High render priority
                    • Better resource management
                """.trimIndent()

                comparisonMetrics.setBackgroundColor(
                    if (percentFaster > 0) getColor(android.R.color.holo_green_light)
                    else getColor(android.R.color.holo_orange_light)
                )
            }
        }
    }

    override fun onDestroy() {
        unoptimizedWebView.destroy()
        optimizedWebView.destroy()
        super.onDestroy()
    }
}
