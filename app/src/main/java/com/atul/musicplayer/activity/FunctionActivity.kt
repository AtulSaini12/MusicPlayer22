package com.atul.musicplayer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.atul.musicplayer.R
import com.atul.musicplayer.model.parceableClass
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.util.*

@Suppress("UNCHECKED_CAST")
class FunctionActivity : AppCompatActivity() {
    lateinit var relativeParent : RelativeLayout
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var speechRecognizerIntent: Intent
    lateinit var btnBack : ImageButton
    lateinit var imgSongLogo : ImageView
    lateinit var txtSongName : TextView
    lateinit var timeBar : SeekBar
    lateinit var relativeButtons : RelativeLayout
    lateinit var btnPrevious : ImageButton
    lateinit var btnPlayPause : Button
    lateinit var btnNext : ImageButton
    lateinit var btnVoice : Button
    lateinit var txtTimeLable : TextView
    lateinit var txtElapsedLable : TextView
    var keeper = ""
    var mode = "ON"
    var totalTime : Int ?= null
    lateinit var handler : Handler
    lateinit var runnable : Runnable
    var myMediaPlayer: MediaPlayer ?= null
    var position : Int = 0
    lateinit var mySongs : ArrayList<File>
    lateinit var mySongName : String

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_function)

        checkPermissions()

        init()
        relativeButtons.visibility = View.GONE

        btnVoice.setOnClickListener {
            if(mode.equals("ON")){
                mode = "OFF"
                btnVoice.text = getString(R.string.voice_control_off)
                relativeButtons.visibility = View.VISIBLE
            }else{
                mode = "ON"
                btnVoice.text = getString(R.string.voice_control_on)
                relativeButtons.visibility = View.GONE
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                val matchFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matchFound != null) {
                    keeper = matchFound.get(0)

                    if (mode.equals("ON")) {
                        if (keeper.equals("pause the song") || keeper.equals("pause") || keeper.equals(
                                "pause song"
                            ) || keeper.equals("pose") || keeper.equals("pose the song") || keeper.equals(
                                "pose song"
                            )
                        ) {
                            Toast.makeText(this@FunctionActivity,"Song Paused",Toast.LENGTH_SHORT).show()
                            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
                            myMediaPlayer?.pause()
                            imgSongLogo.setImageResource(R.drawable.splash)
                        } else if (keeper.equals("play the song") || keeper.equals("play") || keeper.equals(
                                "play song"
                            )
                        ) {
                            Toast.makeText(this@FunctionActivity,"Song started playing",Toast.LENGTH_SHORT).show()
                            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
                            myMediaPlayer?.start()
                            changeTimeBar()
                            imgSongLogo.setImageResource(R.drawable.m3)
                        } else if (keeper.equals("play previous") || keeper.equals("play previous song")) {
                            Toast.makeText(this@FunctionActivity,"Playing Previous Song",Toast.LENGTH_SHORT).show()
                            playPreviousSong()
                        } else if (keeper.equals("play next") || keeper.equals("play next song")) {
                            Toast.makeText(this@FunctionActivity,"Playing Next Song",Toast.LENGTH_SHORT).show()
                            playNextSong()
                        }
                    }
                }
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        if(mode.equals("ON")){
            relativeParent.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        speechRecognizer.startListening(speechRecognizerIntent)
                        keeper = ""
                    }
                    MotionEvent.ACTION_UP -> {
                        speechRecognizer.stopListening()
                    }
                }
                return@OnTouchListener true
            })
        }

        startPlaying()
        btnPlayPause.setOnClickListener {
            playPauseSong()
        }
        btnBack.setOnClickListener {
            myMediaPlayer?.pause()
            myMediaPlayer?.stop()
            onBackPressed()
        }
        btnPrevious.setOnClickListener {
            playPreviousSong()
        }
        btnNext.setOnClickListener {
            playNextSong()
        }
        setUpSeekBar()
    }

    fun init(){
        relativeParent = findViewById(R.id.relativeParent)
        btnBack = findViewById(R.id.btnBack)
        imgSongLogo = findViewById(R.id.imgSongLogo)
        txtSongName = findViewById(R.id.txtSongName)
        timeBar = findViewById(R.id.timeBar)
        relativeButtons = findViewById(R.id.relativeButtons)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnVoice = findViewById(R.id.btnVoice)
        txtTimeLable = findViewById(R.id.txtTimeLable)
        txtElapsedLable = findViewById(R.id.txtElapsedLable)
        btnPlayPause.setBackgroundResource(R.drawable.ic_pause)

        txtSongName.marqueeRepeatLimit = 1000
    }
    fun askPermissions(): Boolean {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
           return false
        }else{
            return true
        }
    }
    fun checkPermissions(){
        if(!askPermissions()) {
            Dexter.withActivity(this).withPermission(android.Manifest.permission.RECORD_AUDIO)
                .withListener(
                    object : PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            val dialog = AlertDialog.Builder(this@FunctionActivity)
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
                                ActivityCompat.finishAffinity(this@FunctionActivity)
                            }
                            dialog.create()
                            dialog.show()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken
                        ) {
                            p1.continuePermissionRequest()
                        }

                    }).check()
        }
    }
    private fun startPlaying(){
        if(myMediaPlayer != null){
            myMediaPlayer?.stop()
            myMediaPlayer?.release()
        }
        val intent = intent
        if(intent != null){
            val bundle : Bundle ?= intent.getExtras()
            mySongs = bundle?.getParcelableArrayList<parceableClass>("song") as ArrayList<File>
            mySongName = mySongs.get(position).name
            val songName : String = intent.getStringExtra("name").toString()
            txtSongName.text = songName
            txtSongName.setTextIsSelectable(true)
            txtSongName.isSelected = true

            position = bundle.getInt("position", 0)
            val uri = Uri.parse(mySongs.get(position).toString())
            imgSongLogo.setImageResource(R.drawable.m1)
            myMediaPlayer = MediaPlayer.create(this, uri)
            myMediaPlayer?.start()
            changeTimeBar()
        }
    }
    private fun playPauseSong(){
        if(myMediaPlayer?.isPlaying == true){
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
            myMediaPlayer?.pause()
            imgSongLogo.setImageResource(R.drawable.m2)
        }else{
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            myMediaPlayer?.start()
            changeTimeBar()
            imgSongLogo.setImageResource(R.drawable.m3)
        }
    }
    private fun playNextSong(){
        myMediaPlayer?.pause()
        myMediaPlayer?.stop()
        myMediaPlayer?.release()

        position = (position+1)%mySongs.size
        val uri : Uri = Uri.parse(mySongs.get(position).toString())
        myMediaPlayer = MediaPlayer.create(this@FunctionActivity, uri)
        mySongName = mySongs.get(position).name
        txtSongName.text = mySongName.toString()
        myMediaPlayer?.start()

        if(myMediaPlayer?.isPlaying == true){
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
        }else{
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
        changeTimeBar()
        imgSongLogo.setImageResource(R.drawable.m4)
    }
    private fun playPreviousSong(){
        myMediaPlayer?.pause()
        myMediaPlayer?.stop()
        myMediaPlayer?.release()

        position = if (position - 1 < 0) mySongs.size - 1 else position - 1
        val uri : Uri = Uri.parse(mySongs.get(position).toString())
        myMediaPlayer = MediaPlayer.create(this@FunctionActivity, uri)
        mySongName = mySongs.get(position).name
        txtSongName.text = mySongName.toString()

        myMediaPlayer?.start()
        imgSongLogo.setImageResource(R.drawable.splsh)
        if(myMediaPlayer?.isPlaying==true){
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
        }else{
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
        changeTimeBar()
    }
    fun setUpSeekBar(){
        myMediaPlayer?.isLooping = true
        myMediaPlayer?.seekTo(0)
        totalTime = myMediaPlayer?.duration

        timeBar.max = totalTime!!.toInt()
        timeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    myMediaPlayer?.seekTo(p1)
                    timeBar.progress = p1
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        myMediaPlayer?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(p0: MediaPlayer?) {
                timeBar.max = myMediaPlayer!!.duration.toInt()
                if(myMediaPlayer?.isPlaying == true)
                    changeTimeBar()
            }

        })

        /*val handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                val currPostion = msg.what
                timeBar.progress = currPostion
                val elapsedTime = createTimeLable(currPostion)
                txtTimeLable.text = elapsedTime.toString()
                val remainingTime = createTimeLable(totalTime!!.toInt() - currPostion)
                val remaining = "-" + remainingTime.toString()
                txtElapsedLable.text = remaining
            }
        }
        val mHandler = Handler()
        //Make sure you update Seekbar on UI thread
        this@FunctionActivity.runOnUiThread(object : Runnable {
            override fun run() {
                if (myMediaPlayer != null) {
                    val mCurrentPosition: Int = myMediaPlayer!!.currentPosition/ 1000
                    timeBar.progress = mCurrentPosition
                }
                mHandler.postDelayed(this, 1000)
            }
        })

        Thread {
            while (myMediaPlayer != null) {
                try {
                    val message = Message()
                    message.what = myMediaPlayer!!.currentPosition
                    mHandler.sendMessage(message)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }*/

    }
    private fun changeTimeBar(){
        timeBar.progress = myMediaPlayer!!.currentPosition
        if(myMediaPlayer?.isPlaying == true){
            runnable = Runnable { changeTimeBar() }
        }
        handler = Handler()
        handler.postDelayed(runnable,1000)
    }
    /*private fun createTimeLable(time: Int): String? {
        var timeLable = ""
        val min = time / 1000 / 60
        val sec = time / 1000 % 60
        timeLable = "$min:"
        if (sec < 10) {
            timeLable = timeLable + "0"
        }
        timeLable += sec
        return timeLable
    }*/
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
