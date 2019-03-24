package com.example.androidplayer.fragments


import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidplayer.R
import com.example.androidplayer.basicLogic
import com.example.androidplayer.fragments.adapter.SoundAdapter
import com.example.androidplayer.utils.AlbumArt
import com.example.androidplayer.utils.Music
import com.example.androidplayer.utils.getTime
import kotlinx.android.synthetic.main.media_fragment.*
import java.text.DecimalFormat


class MediaFragment : Fragment() {

    private lateinit var list: MutableList<Music>
    private lateinit var adapter: SoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.media_fragment, container, false)
    }

    override fun onStart() {
        list = musicFiles()
        adapter = SoundAdapter(context, list)
        list_view.adapter = adapter
        list_view.setOnItemClickListener { _, _, position, _ ->
            activity?.basicLogic(list[position])
        }
        super.onStart()
    }

    fun musicFiles(): MutableList<Music> {

        val list: MutableList<Music> = mutableListOf()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //val uri: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = activity?.contentResolver?.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.moveToFirst()) {
            val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val albumIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val pathIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumIdIdx = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)
            val sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

            do {
                val title = cursor.getString(titleIdx)
                val album = cursor.getString(albumIdx)
                val duration = getTime(cursor.getInt(durationIdx))
                val artist = cursor.getString(artistIdx)
                val path = cursor.getString(pathIdx)
                val albumId = cursor.getInt(albumIdIdx)
                val size = getSize(cursor.getDouble(sizeIdx))
                val cursorAlbumArt = activity?.contentResolver?.query(
                    albumUri,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=" + albumId,
                    null,
                    null
                )
                if (cursorAlbumArt != null && cursorAlbumArt.moveToFirst()) {
                    val albumCoverPath =
                        cursorAlbumArt.getString(cursorAlbumArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))

                    val bm = if (albumCoverPath.isNullOrEmpty()) null else BitmapFactory.decodeFile(albumCoverPath)
                    list.add(Music(title, album, duration, artist, path, size, AlbumArt(albumId, albumCoverPath, bm)))
                }
                cursorAlbumArt?.close()
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return list
    }

    private fun getSize(size: Double): String {
        val dec = DecimalFormat("0.00")
        val byte = size / 8.0
        val kByte = size / 1024.0
        val mByte = size / 1048576.0
        val gByte = size / 1073741824.0
        val fSize: String
        fSize = when {
            gByte > 1 -> dec.format(gByte).plus("GB")
            mByte > 1 -> dec.format(mByte).plus("MB")
            kByte > 1 -> dec.format(kByte).plus("kB")
            byte > 1 -> dec.format(byte).plus("Byte")
            else -> dec.format(size).plus("bit")
        }
        return fSize
    }
}
