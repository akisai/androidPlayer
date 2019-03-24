package com.example.androidplayer.utils

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.replaceFragment(fragment: Fragment,
                                      allowStateLoss: Boolean = false,
                                      @IdRes containerViewId: Int) {
    val ft = supportFragmentManager
        .beginTransaction()
        .replace(containerViewId, fragment)
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}