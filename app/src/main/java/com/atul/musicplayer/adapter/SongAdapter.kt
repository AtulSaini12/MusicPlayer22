package com.atul.musicplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.atul.musicplayer.activity.FunctionActivity
import com.atul.musicplayer.R
import java.io.File

class SongAdapter(val context: Context, val songList : ArrayList<File>):RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View):RecyclerView.ViewHolder(view){
        val llContent : LinearLayout = view.findViewById(R.id.llContent)
        val txtSongName : TextView = view.findViewById(R.id.txtSongname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_recycler_list,parent,false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        holder.txtSongName.text = song.name

        holder.llContent.setOnClickListener {
            val intent = Intent(context, FunctionActivity::class.java)
            intent.putExtra("song",songList)
            intent.putExtra("name",song.name.toString())
            intent.putExtra("position",position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }
}