package com.atul.musicplayer.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atul.musicplayer.R
import com.atul.musicplayer.adapter.SongAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {
    lateinit var songrecycler : RecyclerView
    lateinit var recycleradapter : SongAdapter
    lateinit var toolbar: Toolbar
    lateinit var itemsAll : ArrayList<String>
    lateinit var layoutManager : RecyclerView.LayoutManager
    var request : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        songrecycler = findViewById(R.id.songRecycler)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Music"
        layoutManager = LinearLayoutManager(this)
        askPermission()
        if(request){
            displaySongs()
        }
    }
    fun askPermission() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Dexter.withActivity(this).withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        request = true
                        displaySongs()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        val dialog = AlertDialog.Builder(this@MainActivity)
                        dialog.setTitle("Important!!")
                        dialog.setMessage("Please allow all the required permissions :D")
                        dialog.setPositiveButton("Allow") { _, _ ->
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + packageName)
                            )
                            startActivity(intent)
                            finish()
                        }
                        dialog.setNegativeButton("Exit") { _, _ ->
                            ActivityCompat.finishAffinity(this@MainActivity)
                        }
                        dialog.create()
                        dialog.show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken
                    ) {

                    }

                }).check()
        }else{
            request = true
        }
    }
    fun readSongs(file: File):ArrayList<File>{
        val listReceived = ArrayList<File>()
        val allFiles : Array<File> = file.listFiles()
        if(allFiles.isNotEmpty()) {
            for (individualFile: File in allFiles) {
                if (individualFile.isDirectory && !(individualFile.isHidden)) {
                    listReceived.addAll(readSongs(individualFile))
                } else {
                    if ((individualFile.name.endsWith(".aac")) || (individualFile.name.endsWith(".mp3")) || (individualFile.name.endsWith(
                            ".m4a"
                        )) || (individualFile.name.endsWith(".wav")) || (individualFile.name.endsWith(".wma"))
                    ) {
                        listReceived.add(individualFile)
                    }
                }
            }
        }
        return listReceived
    }
    private fun displaySongs(){
        val audioSongs : ArrayList<File> = readSongs(Environment.getExternalStorageDirectory())
        itemsAll = ArrayList(audioSongs.size)
            var songCounter = 0
            while (songCounter < audioSongs.size) {
                itemsAll.add(songCounter,audioSongs.get(songCounter).name)
                songCounter++
            }
        recycleradapter = SongAdapter(this, audioSongs)
        songrecycler.adapter = recycleradapter
        songrecycler.layoutManager = layoutManager
    }
}