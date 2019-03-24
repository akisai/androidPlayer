package com.example.androidplayer.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.androidplayer.R
import com.example.androidplayer.utils.Music


class SoundAdapter(private val context: Context?, private val music: MutableList<Music>) : BaseAdapter() {

    private val inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return music.size
    }

    override fun getItem(position: Int): Any {
        return music[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_music, parent, false)
        val title = view.findViewById<TextView>(R.id.atitle)
        val artist = view.findViewById<TextView>(R.id.artist)
        val duration = view.findViewById<TextView>(R.id.duration)
        val size = view.findViewById<TextView>(R.id.size)
        val image = view.findViewById<ImageView>(R.id.albumArt)

        val music = getItem(position) as Music

        title.text = music.title
        artist.text = music.artist
        duration.text = music.duration
        size.text = music.size
        image.clipToOutline = true
        if (music.albumArt.image != null)
            image.setImageBitmap(music.albumArt.image)
        return view
    }
}