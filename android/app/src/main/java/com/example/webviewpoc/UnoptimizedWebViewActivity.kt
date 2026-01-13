package com.example.webviewpoc

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * POC 1: UNOPTIMIZED WebView
 *
 * This activity demonstrates POOR WebView configuration with common mistakes.
 * DO NOT use these settings in production!
 *
 * Issues demonstrated:
 * - Hardware acceleration disabled
 * - No caching enabled
 * - Blocking all network images
 * - No DOM storage
 * - Synchronous JavaScript
 * - No app cache
 * - Poor WebViewClient implementation
 * - No resource optimization
 */
class UnoptimizedWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var metricsText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_poc)

        supportActionBar?.title = "❌ Unoptimized WebView"
        supportActionBar?.subtitle = "Poor Performance Settings"

        webView = findViewById(R.id.webView)
        metricsText = findViewById(R.id.metricsText)
        val issuesText: TextView = findViewById(R.id.issuesText)

        // Display issues with this configuration
        issuesText.visibility = View.VISIBLE
        issuesText.setBackgroundColor(getColor(android.R.color.holo_red_light))
        issuesText.text = """
            ⚠️ UNOPTIMIZED SETTINGS (DO NOT USE IN PRODUCTION):

            ❌ Hardware acceleration: DISABLED
            ❌ Cache mode: LOAD_NO_CACHE (no caching!)
            ❌ DOM Storage: DISABLED
            ❌ App Cache: DISABLED
            ❌ Database: DISABLED
            ❌ Block network images: ENABLED (blocks images!)
            ❌ Render priority: NORMAL (not optimized)
            ❌ Mixed content mode: NEVER_ALLOW
            ❌ No resource optimization
            ❌ No JavaScript optimization
            ❌ No prefetching
            ❌ Poor WebViewClient (blocks on main thread)

            Expected Impact:
            • Slow page loads
            • High data usage (no caching)
            • Images won't load
            • Poor scrolling performance
            • Increased battery drain
            • Stuttering animations
        """.trimIndent()

        configureUnoptimizedWebView()
        loadTestPage()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureUnoptimizedWebView() {
        val settings = webView.settings

        // POOR SETTING 1: Disable hardware acceleration at view level
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // POOR SETTING 2: Disable all caching
        settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        settings.setAppCacheEnabled(false)
        settings.setAppCachePath("")

        // POOR SETTING 3: Disable DOM storage
        settings.domStorageEnabled = false
        settings.databaseEnabled = false

        // POOR SETTING 4: Block network images
        settings.blockNetworkImage = true

        // POOR SETTING 5: Enable JavaScript but don't optimize it
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = false

        // POOR SETTING 6: Don't enable zoom controls
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false

        // POOR SETTING 7: Normal render priority (not HIGH)
        settings.renderPriority = android.webkit.WebSettings.RenderPriority.NORMAL

        // POOR SETTING 8: Disable content access
        settings.allowContentAccess = false
        settings.allowFileAccess = false

        // POOR SETTING 9: Don't allow mixed content
        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // POOR SETTING 10: Disable viewport and wide viewport
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = false

        // POOR SETTING 11: Don't set user agent optimizations
        // Keep default user agent

        // POOR SETTING 12: Enable safe browsing (adds overhead)
        settings.safeBrowsingEnabled = true

        // POOR SETTING 13: No text reflow
        settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL

        // POOR SETTING 14: Default text size (no optimization)
        settings.textZoom = 100

        // POOR WebViewClient - does nothing to optimize loading
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                startTime = System.currentTimeMillis()
                updateMetrics("Loading...", 0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val loadTime = System.currentTimeMillis() - startTime
                updateMetrics("Loaded", loadTime)

                // Inject performance monitoring
                view?.evaluateJavascript("""
                    (function() {
                        var timing = window.performance.timing;
                        var loadTime = timing.loadEventEnd - timing.navigationStart;
                        var domReady = timing.domContentLoadedEventEnd - timing.navigationStart;
                        return JSON.stringify({
                            loadTime: loadTime,
                            domReady: domReady,
                            firstPaint: performance.getEntriesByType('paint')[0]?.startTime || 0
                        });
                    })();
                """.trimIndent()) { result ->
                    // Process result
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Block navigation - poor UX
                return true
            }
        }

        // No ChromeClient - miss out on progress callbacks and console messages
    }

    private fun loadTestPage() {
        // Load our performance test page
        webView.loadUrl("file:///android_asset/test_page.html")
    }

    private fun updateMetrics(status: String, loadTime: Long) {
        handler.post {
            metricsText.text = """
                Status: $status
                Load Time: ${loadTime}ms

                Performance Metrics:
                - Hardware Acceleration: ❌ DISABLED
                - Caching: ❌ DISABLED
                - Image Loading: ❌ BLOCKED
                - DOM Storage: ❌ DISABLED
                - Render Priority: ⚠️ NORMAL

                Expected Performance:
                - Very Slow Loading
                - High Network Usage
                - Poor Scrolling
                - Stuttering Animations
            """.trimIndent()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
