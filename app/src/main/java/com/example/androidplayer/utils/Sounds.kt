package com.example.androidplayer.utils

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class Music(
    val title: String,
    val album: String,
    val duration: String,
    val artist: String,
    val path: String,
    val size: String,
    val albumArt: AlbumArt
) : Parcelable

@Parcelize
data class AlbumArt(val albumId: Int, val albumCoverPath: String?, val image: Bitmap?) : Parcelable

fun getTime(duration: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}