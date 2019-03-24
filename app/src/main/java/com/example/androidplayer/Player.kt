package com.example.androidplayer

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.SeekBar
import com.example.androidplayer.utils.Music
import com.example.androidplayer.utils.getTime
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet_content.*
import kotlinx.android.synthetic.main.navigation_activity.*
import kotlinx.android.synthetic.main.player.*
import kotlinx.android.synthetic.main.player_min.*
import org.jetbrains.anko.longToast


private lateinit var mediaPlayer: MediaPlayer
private lateinit var sounds: ArrayList<Music?>
private lateinit var runnableMusic: Runnable
private lateinit var runnableTime: Runnable
private lateinit var handlerMusic: Handler
private lateinit var handlerTime: Handler
private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
private lateinit var audioManager: AudioManager
private var repeatAll = false
private var position = 0
private var state = 0
private var idx = 0
private const val dX = 165f
private const val dY = 130f

fun Activity.initPlayer() {

    bottom_sheet.visibility = View.INVISIBLE

    minPlayer()
    player()
    volumeController()
}

fun getMediaPlayer() = mediaPlayer
fun getAudioManager() = audioManager

private fun Activity.minPlayer() {
    val scaleDp = resources.displayMetrics.density
    album.translationX = -dX * scaleDp
    album.translationY = -dY * scaleDp
    val rectangleSheet = GradientDrawable()
    val rectangleContent = GradientDrawable()
    rectangleSheet.shape = GradientDrawable.RECTANGLE
    rectangleContent.shape = GradientDrawable.RECTANGLE
    bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottom_sheet)
    bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(p0: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    play_min.isEnabled = false
                    next_min.isEnabled = false
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    play_min.isEnabled = true
                    music_seek.isEnabled = true
                    if (sounds.size > 1)
                        next_min.isEnabled = true
                }
            }
        }

        override fun onSlide(p0: View, slideOffset: Float) {
            val scale = 0.5f + slideOffset * 1.7f
            val offsetX = (dX * slideOffset - dX) * scaleDp
            val offsetY = (dY * slideOffset - dY) * scaleDp
            val offsetElements = (240f * slideOffset - 240f) * scaleDp
            val offsetAlpha = 1f - slideOffset * 4f
            val alpha = if (offsetAlpha > 0) offsetAlpha else 0f
            val alphaColor = if (255f * (1f - alpha) > 255f * 0.9f) 255f * (1f - alpha) else 255f * 0.9f

            val rectangleRad = if (slideOffset > 0.2f) 13f * scaleDp else slideOffset * scaleDp * 65f
            rectangleSheet.cornerRadii = floatArrayOf(rectangleRad, rectangleRad, rectangleRad, rectangleRad, 0f, 0f, 0f, 0f)
            rectangleSheet.setColor(Color.argb(alphaColor.toInt(), 255, 255, 255))
            bottom_sheet.background = rectangleSheet

            rectangleContent.cornerRadius = rectangleRad
            bottom_sheet_content.background = rectangleContent

            navigation.animate().translationY(46f * scaleDp * slideOffset).setDuration(0).start()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bottom_sheet_content.foreground = ColorDrawable(Color.argb((slideOffset * 240).toInt(), 132, 133, 137))
                navigation.foreground = ColorDrawable(Color.argb((slideOffset * 240).toInt(), 255, 255, 255))
                if (slideOffset > 0.5f)
                    window.decorView.systemUiVisibility = 0
                else
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            bottom_sheet_content.animate().scaleX(1f - slideOffset * 0.07f).scaleY(1f - slideOffset * 0.07f)
                .setDuration(0)
                .start()
            player_border.animate().alpha(alpha).setDuration(0).start()
            player_min.animate().alpha(alpha).setDuration(0).start()
            close.animate().alpha(1f - alpha).setDuration(0).start()
            player.animate().translationY(offsetElements).setDuration(0).start()
            album.animate().scaleX(scale).scaleY(scale).translationY(offsetY).translationX(offsetX).setDuration(0)
                .start()
        }
    })

    clickable.setOnClickListener {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

private fun Activity.player() {
    album.clipToOutline = true
    bottom_sheet_content.clipToOutline = true
    bottom_sheet.clipToOutline = true
    sounds = ArrayList()
    mediaPlayer = MediaPlayer()
    music_seek.isEnabled = false

    mediaPlayer.setOnCompletionListener {
        play.setImageResource(R.drawable.ic_action_play)
        play_min.setImageResource(R.drawable.ic_action_play)
    }

    play()
    next()
    previous()
    repeat()
}

private fun Activity.volumeController() {
    audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    volume_seek.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    volume_seek.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    volume_seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    })
}

private fun Activity.musicController() {

    music_seek.max = mediaPlayer.duration
    music_seek.isEnabled = true

    handlerMusic = Handler()
    runnableMusic = object : Runnable {
        override fun run() {
            runOnUiThread {
                music_seek.progress = mediaPlayer.currentPosition
            }
            handlerMusic.postDelayed(this, 100)
        }
    }

    handlerTime = Handler()
    runnableTime = object : Runnable {
        override fun run() {
            runOnUiThread {
                time_to.text = "-".plus(getTime(mediaPlayer.duration - mediaPlayer.currentPosition))
                time_left.text = getTime(mediaPlayer.currentPosition)
            }
            handlerTime.postDelayed(this, 1000)
        }
    }


    music_seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                position = progress
                time_to.text = "-".plus(getTime(mediaPlayer.duration - position))
                time_left.text = getTime(position)
                if (mediaPlayer.isPlaying) {
                    stop()
                    mediaPlayer.seekTo(position)
                    start()
                } else
                    mediaPlayer.seekTo(position)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    })

}

private fun start() {
    mediaPlayer.start()
    runnableMusic.run()
    runnableTime.run()
}

private fun stop() {
    mediaPlayer.pause()
    handlerMusic.removeCallbacks(runnableMusic)
    handlerTime.removeCallbacks(runnableTime)
}

fun Activity.basicLogic(song: Music?) {
    song.let {
        sounds.add(song!!)
        bottom_sheet.visibility = View.VISIBLE
    }
    if (sounds.size > 1 && !sounds.isNullOrEmpty()) {
        next.isEnabled = true
        next_min.isEnabled = true
        previous.isEnabled = true
    }
    idx = 0
    if (idx < sounds.size) {
        val next = sounds[idx]!!
        if (sounds.size == 1)
            updateTrack(next)
        play.isEnabled = true
        play_min.isEnabled = true
        repeat.isEnabled = true
    } else {
        longToast("Choose song")
    }
}

private fun Activity.play() {
    play.isEnabled = false
    play_min.isEnabled = false
    play.setOnClickListener {
        playLogic()
    }
    play_min.setOnClickListener {
        playLogic()
    }
}

private fun Activity.playLogic() {
    if (mediaPlayer.isPlaying) {
        stop()
        position = mediaPlayer.currentPosition
        play.setImageResource(R.drawable.ic_action_play)
        play_min.setImageResource(R.drawable.ic_action_play)

    } else {
        mediaPlayer.seekTo(position)
        start()
        play.setImageResource(R.drawable.ic_action_pause)
        play_min.setImageResource(R.drawable.ic_action_pause)
    }
}

private fun Activity.next() {
    next.isEnabled = false
    next_min.isEnabled = false
    next.setOnClickListener {
        if (idx + 1 < sounds.size) {
            idx += 1
            nextLogic()
        } else {
            idx = 0
            nextLogic()
        }
    }
    next_min.setOnClickListener {
        if (idx + 1 < sounds.size) {
            idx += 1
            nextLogic()
        } else {
            idx = 0
            nextLogic()
        }
    }
}

private fun Activity.nextLogic() {
    if (mediaPlayer.isPlaying) {
        stop()
        setNext()
        mediaPlayer.seekTo(position)
        start()
    } else {
        setNext()
    }
}

private fun Activity.setNext() {
    val next = sounds[idx]!!
    updateTrack(next)
    position = 0
}

private fun Activity.previous() {
    previous.isEnabled = false
    previous.setOnClickListener {
        if (idx - 1 >= 0) {
            idx -= 1
            previousLogic()
        } else {
            idx = sounds.size - 1
            previousLogic()
        }
    }
}

private fun Activity.previousLogic() {
    if (mediaPlayer.isPlaying) {
        stop()
        setPrevious()
        mediaPlayer.seekTo(position)
        start()
    } else {
        setPrevious()
    }
}

private fun Activity.setPrevious() {
    val previous = sounds[idx]!!
    updateTrack(previous)
    position = 0
}

private fun Activity.repeat() {
    repeat.isEnabled = true
    repeat.setOnClickListener {
        state += 1
        mediaPlayer.isLooping = false
        repeatAll = false
        when (state) {
            1 -> {
                repeatAll = true
                repeat.setBackgroundColor(ContextCompat.getColor(this, R.color.MusicRed))
                repeat.setTextColor(Color.argb(255, 255, 255, 255))
                val img = ContextCompat.getDrawable(this, R.drawable.ic_action_repeat_pressed)
                img?.setBounds(0, 0, 60, 60)
                repeat.setCompoundDrawables(
                    img,
                    null,
                    null,
                    null
                )
            }
            2 -> {
                mediaPlayer.isLooping = true
                val img = ContextCompat.getDrawable(this, R.drawable.ic_action_repeat_one_pressed)
                img?.setBounds(0, 0, 60, 60)
                repeat.setCompoundDrawables(
                    img,
                    null,
                    null,
                    null
                )
            }
            else -> {
                repeat.setBackgroundColor(ContextCompat.getColor(this, R.color.MusicGrey))
                repeat.setTextColor(ContextCompat.getColor(this, R.color.MusicRed))
                val img = ContextCompat.getDrawable(this, R.drawable.ic_action_repeat)
                img?.setBounds(0, 0, 60, 60)
                repeat.setCompoundDrawables(
                    img,
                    null,
                    null,
                    null
                )
                state = 0
            }
        }
    }
}

private fun shuffle() {

}

private fun Activity.updateTrack(track: Music) {
    if (track.albumArt.image != null)
        album.setImageBitmap(track.albumArt.image)
    mediaPlayer.apply {
        reset()
        setDataSource(track.path)
        prepare()
    }
    stitle.text = track.title
    title_min.text = track.title
    sartist.text = track.artist

    musicController()
}

