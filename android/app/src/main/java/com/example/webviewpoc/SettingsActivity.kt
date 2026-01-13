package com.example.webviewpoc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ScrollView

/**
 * POC 8: Interactive WebView Settings Explorer
 *
 * Allows you to toggle all WebView settings and see their effects in real-time.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var settingsScrollView: ScrollView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "⚙️ Settings Explorer"

        webView = findViewById(R.id.webView)
        settingsScrollView = findViewById(R.id.settingsScrollView)

        configureWebView()
        populateSettings()

        webView.loadUrl("file:///android_asset/test_page.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings

        // Enable basic functionality
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()
    }

    private fun populateSettings() {
        val container: View = findViewById(R.id.settingsContainer)
        val settings = webView.settings

        // Helper function to add setting switches
        fun addSettingSwitch(name: String, description: String, getter: () -> Boolean, setter: (Boolean) -> Unit) {
            // Create switch programmatically or use layout inflation
            // For brevity, just configure existing switches
        }

        // Configure pre-defined switches
        findViewById<Switch>(R.id.switchJavaScript)?.apply {
            isChecked = settings.javaScriptEnabled
            text = "JavaScript Enabled"
            setOnCheckedChangeListener { _, isChecked ->
                settings.javaScriptEnabled = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchDOMStorage)?.apply {
            isChecked = settings.domStorageEnabled
            text = "DOM Storage"
            setOnCheckedChangeListener { _, isChecked ->
                settings.domStorageEnabled = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchHardwareAccel)?.apply {
            isChecked = webView.layerType == View.LAYER_TYPE_HARDWARE
            text = "Hardware Acceleration"
            setOnCheckedChangeListener { _, isChecked ->
                webView.setLayerType(
                    if (isChecked) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
                    null
                )
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchBlockImages)?.apply {
            isChecked = settings.blockNetworkImage
            text = "Block Network Images"
            setOnCheckedChangeListener { _, isChecked ->
                settings.blockNetworkImage = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchLoadImages)?.apply {
            isChecked = settings.loadsImagesAutomatically
            text = "Load Images Automatically"
            setOnCheckedChangeListener { _, isChecked ->
                settings.loadsImagesAutomatically = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchZoom)?.apply {
            isChecked = settings.supportZoom()
            text = "Support Zoom"
            setOnCheckedChangeListener { _, isChecked ->
                settings.setSupportZoom(isChecked)
                settings.builtInZoomControls = isChecked
                settings.displayZoomControls = false
            }
        }

        findViewById<Switch>(R.id.switchWideViewport)?.apply {
            isChecked = settings.useWideViewPort
            text = "Wide Viewport"
            setOnCheckedChangeListener { _, isChecked ->
                settings.useWideViewPort = isChecked
                settings.loadWithOverviewMode = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchDatabase)?.apply {
            isChecked = settings.databaseEnabled
            text = "Database Enabled"
            setOnCheckedChangeListener { _, isChecked ->
                settings.databaseEnabled = isChecked
                webView.reload()
            }
        }

        findViewById<Switch>(R.id.switchGeolocation)?.apply {
            isChecked = settings.javaScriptCanOpenWindowsAutomatically
            text = "JavaScript Can Open Windows"
            setOnCheckedChangeListener { _, isChecked ->
                settings.javaScriptCanOpenWindowsAutomatically = isChecked
            }
        }

        findViewById<Switch>(R.id.switchSafeBrowsing)?.apply {
            isChecked = settings.safeBrowsingEnabled
            text = "Safe Browsing"
            setOnCheckedChangeListener { _, isChecked ->
                settings.safeBrowsingEnabled = isChecked
                webView.reload()
            }
        }

        // Display current settings summary
        updateSettingsSummary()
    }

    private fun updateSettingsSummary() {
        val summaryText: TextView = findViewById(R.id.settingsSummary)
        val settings = webView.settings

        summaryText.text = """
            CURRENT WEBVIEW SETTINGS:

            JavaScript: ${settings.javaScriptEnabled}
            DOM Storage: ${settings.domStorageEnabled}
            Hardware Accel: ${webView.layerType == View.LAYER_TYPE_HARDWARE}
            Cache Mode: ${getCacheModeName(settings.cacheMode)}
            Block Images: ${settings.blockNetworkImage}
            Load Images: ${settings.loadsImagesAutomatically}
            Zoom: ${settings.supportZoom()}
            Wide Viewport: ${settings.useWideViewPort}
            Database: ${settings.databaseEnabled}
            Safe Browsing: ${settings.safeBrowsingEnabled}

            Render Priority: ${settings.renderPriority.name}
            Layout Algorithm: ${settings.layoutAlgorithm.name}
            Mixed Content: ${settings.mixedContentMode}

            User Agent: ${settings.userAgentString}
        """.trimIndent()
    }

    private fun getCacheModeName(mode: Int): String {
        return when (mode) {
            WebSettings.LOAD_DEFAULT -> "DEFAULT"
            WebSettings.LOAD_CACHE_ELSE_NETWORK -> "CACHE_ELSE_NETWORK"
            WebSettings.LOAD_NO_CACHE -> "NO_CACHE"
            WebSettings.LOAD_CACHE_ONLY -> "CACHE_ONLY"
            else -> "UNKNOWN"
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
