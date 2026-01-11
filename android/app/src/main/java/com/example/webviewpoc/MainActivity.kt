package com.example.webviewpoc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class POCItem(
    val title: String,
    val description: String,
    val activity: Class<*>,
    val badge: String,
    val badgeColor: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val pocItems = listOf(
        POCItem(
            "Unoptimized WebView",
            "WebView with poor settings and no optimizations. Demonstrates common mistakes and performance issues.",
            UnoptimizedWebViewActivity::class.java,
            "❌ SLOW",
            android.R.color.holo_red_dark
        ),
        POCItem(
            "Optimized WebView",
            "WebView with best practices and all optimizations enabled. Maximum performance configuration.",
            OptimizedWebViewActivity::class.java,
            "✓ FAST",
            android.R.color.holo_green_dark
        ),
        POCItem(
            "Side-by-Side Comparison",
            "Compare unoptimized vs optimized WebView performance in real-time with metrics.",
            ComparisonActivity::class.java,
            "⚖️ COMPARE",
            android.R.color.holo_orange_dark
        ),
        POCItem(
            "WebViewClient Optimizations",
            "Demonstrates WebViewClient and ChromeClient optimizations for resource loading and rendering.",
            WebViewClientOptimizationsActivity::class.java,
            "🔧 CLIENT",
            android.R.color.holo_blue_dark
        ),
        POCItem(
            "Caching Strategies",
            "Different caching modes and their impact on load times and data usage.",
            CachingStrategiesActivity::class.java,
            "💾 CACHE",
            android.R.color.holo_purple
        ),
        POCItem(
            "Rendering Performance",
            "Hardware acceleration, layer types, and rendering optimizations.",
            RenderingPerformanceActivity::class.java,
            "🎨 RENDER",
            android.R.color.holo_blue_light
        ),
        POCItem(
            "JavaScript Bridge",
            "Optimized JavaScript-to-Native communication patterns and performance.",
            JavaScriptBridgeActivity::class.java,
            "🌉 BRIDGE",
            android.R.color.holo_orange_light
        ),
        POCItem(
            "Settings Explorer",
            "Interactive explorer for all WebView settings with real-time preview.",
            SettingsActivity::class.java,
            "⚙️ SETTINGS",
            android.R.color.darker_gray
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "WebView Performance POCs"

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = POCAdapter(pocItems) { pocItem ->
            startActivity(Intent(this, pocItem.activity))
        }
    }
}

class POCAdapter(
    private val items: List<POCItem>,
    private val onItemClick: (POCItem) -> Unit
) : RecyclerView.Adapter<POCAdapter.POCViewHolder>() {

    class POCViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        val badgeText: TextView = view.findViewById(R.id.badgeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POCViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poc, parent, false)
        return POCViewHolder(view)
    }

    override fun onBindViewHolder(holder: POCViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.badgeText.text = item.badge
        holder.badgeText.setBackgroundColor(
            holder.itemView.context.getColor(item.badgeColor)
        )
        holder.cardView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
