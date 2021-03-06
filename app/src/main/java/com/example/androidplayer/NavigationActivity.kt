package com.example.androidplayer

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import com.example.androidplayer.fragments.DashboardFragment
import com.example.androidplayer.fragments.PlaylistFragment
import com.example.androidplayer.fragments.StoreFragment
import com.example.androidplayer.utils.replaceFragment
import kotlinx.android.synthetic.main.navigation_activity.*
import kotlinx.android.synthetic.main.player.*


class NavigationActivity : AppCompatActivity() {

    private val READ_PERMISSION = 222

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_media -> {
                replaceFragment(PlaylistFragment(), true, R.id.bottom_sheet_content)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                replaceFragment(DashboardFragment(), true, R.id.bottom_sheet_content)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                replaceFragment(StoreFragment(), true, R.id.bottom_sheet_content)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)

        checkPermissions()
        initPlayer()
        replaceFragment(PlaylistFragment(), true, R.id.bottom_sheet_content)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun hasNavBar(resources: Resources): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION
                )
            }
            return
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                val plus = getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC) + 1
                getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, plus, 0)
                volume_seek.progress = plus
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val minus = getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC) - 1
                getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, minus, 0)
                volume_seek.progress = minus
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        getMediaPlayer().release()
    }
}
