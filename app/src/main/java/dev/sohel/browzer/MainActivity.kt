package dev.sohel.browzer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val isFullscreen =
            sharedPreferences.getBoolean(getString(R.string.fullscreen_pref_key), false)
        if (isFullscreen) {
            fullScreenMode()
        }

        setContentView(R.layout.activity_main)

        CookieManager.getInstance().setAcceptCookie(true)

        val mWebView = findViewById<WebView>(R.id.webView)

        // register context menu on long press
        // registerForContextMenu(mWebView)

        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.domStorageEnabled = true
        mWebView.settings.databaseEnabled = true
        // mWebView.settings.javaScriptCanOpenWindowsAutomatically = true;

        mWebView.loadUrl(getHomeUrl())

        mWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                mWebView.loadUrl(getString(R.string.default_error_url))
            }
        }

        // mWebView.setOnLongClickListener {
        //     Toast.makeText(this, "Long click detected", Toast.LENGTH_SHORT).show()
        //     return@setOnLongClickListener true
        // }

        val mSwipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        mSwipeRefresh.setOnRefreshListener {
            Log.i("[setOnRefreshListener]", "onRefresh called from SwipeRefreshLayout")
            // mWebView.reload()

            registerForContextMenu(mWebView);
            openContextMenu(mWebView);
            unregisterForContextMenu(mWebView);

            mSwipeRefresh.isRefreshing = false

            // Timer().schedule(object : TimerTask() {
            //     override fun run() {
            //         mSwipeRefresh.isRefreshing = false
            //     }
            // }, 1000)

        }

    }

    override fun onBackPressed() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val backButtonRemembersHistory = sharedPreferences.getBoolean(
            getString(R.string.back_button_remembers_history_pref_key),
            false
        )

        if (backButtonRemembersHistory) {
            val mWebView = findViewById<WebView>(R.id.webView)
            if (mWebView.isFocused && mWebView.canGoBack()) {
                mWebView.goBack()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu!!.setHeaderTitle(getString(R.string.app_name))

        menu.add(0, v!!.id, 0, getString(R.string.menu_title_reload))
        menu.add(0, v.id, 1, getString(R.string.menu_title_home))
        menu.add(0, v.id, 2, getString(R.string.menu_title_copy_current_url))
        menu.add(0, v.id, 3, getString(R.string.menu_title_settings))
        menu.add(0, v.id, 4, getString(R.string.menu_title_exit))
    }

    private fun getHomeUrl(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val homeUrl = sharedPreferences.getString(
            getString(R.string.home_url_pref_key),
            getString(R.string.default_home_url)
        )

        if (URLUtil.isValidUrl(homeUrl.toString())) {
            return homeUrl.toString()
        }

        return getString(R.string.repo_readme_url)
    }

    private fun fullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedItemOrder = item!!.order
        val selectedItemTitle = item.title

        when (selectedItemTitle) {
            getString(R.string.menu_title_home) -> {
                val mWebView = findViewById<WebView>(R.id.webView)
                mWebView.clearHistory()
                mWebView.loadUrl(getHomeUrl())
            }
            getString(R.string.menu_title_reload) -> {
                val mWebView = findViewById<WebView>(R.id.webView)
                mWebView.reload()
            }
            getString(R.string.menu_title_settings) -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    putExtra("myMsg", "hello world")
                }

                startActivity(intent)
            }
            getString(R.string.menu_title_copy_current_url) -> {
                val mWebView = findViewById<WebView>(R.id.webView)
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", mWebView.url)
                clipboardManager.setPrimaryClip(clipData)
            }
            getString(R.string.menu_title_exit) -> {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.mock_text2),
                    Toast.LENGTH_SHORT
                ).show()

                finishAffinity()
            }
            else -> {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.mock_text1),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return true
    }

}
