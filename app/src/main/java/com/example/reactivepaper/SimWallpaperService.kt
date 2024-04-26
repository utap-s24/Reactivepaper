package com.example.reactivepaper

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.ViewConfiguration
import java.io.File

class SimWallpaperService : WallpaperService() {

    private lateinit var music: File
    private lateinit var mediaPlayer: MediaPlayer;

    override fun onCreateEngine(): Engine {
        Log.d("SimWallpaperService", "onCreateEngine: ")
        return SimEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            Log.d("SimWallpaperService", "onStartCommand: ")
            music = File(intent?.getStringExtra("File")!!)
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    inner class SimEngine : Engine() {

        private lateinit var simulation : SimulationViewer;
        private var playing = false;

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable { nextFrame() }
        private val mLongPressed: Runnable = Runnable {
            if (playing) {
                mediaPlayer.pause()

            } else {
                mediaPlayer.start()
            }
            playing = !playing
        }
        private val rotation = GyroSensor(this@SimWallpaperService)


        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            val canvas = holder?.lockCanvas() ?: return
            simulation = SimulationViewer(canvas.width, canvas.height)
            simulation.drawFrame(canvas)
            holder.unlockCanvasAndPost(canvas)
            handler.postDelayed(runnable, 17)

            try {
                mediaPlayer.duration
            }
            catch (e: UninitializedPropertyAccessException){
                playMusic()
                playing = true
            }
        }

        override fun onTouchEvent(event: MotionEvent?) {
            if (event != null) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout().toLong())
                    if (!handler.hasCallbacks(mLongPressed)) {
                        simulation.changeGravity()
                    }
                }
            }
            if (event != null) {
                if ((event.action == MotionEvent.ACTION_MOVE) || (event.action == MotionEvent.ACTION_UP))
                    handler.removeCallbacks(mLongPressed)
            }
            return super.onTouchEvent(event)
        }


        private fun nextFrame() {
            val holder = surfaceHolder
            val canvas = holder?.lockCanvas() ?: return
            simulation.drawFrame(canvas)

            holder.unlockCanvasAndPost(canvas)
            updateGravity()
            handler.postDelayed(runnable, 10)
        }


        private fun updateGravity(){
            val rotations = rotation.getRotationValues()
            val x = rotations[0] * -1
            val y = rotations[1]
            simulation.updateDown(Pair(x.toDouble(), y.toDouble()))
        }

    }

    private fun playMusic(){
        val file = music

        val myUri: Uri = Uri.fromFile(file)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, myUri)
            prepare()
            start()
        }
        val audioSessionId = mediaPlayer.audioSessionId
        mediaPlayer.isLooping = true
    }

    private fun getMusicInfo(){
        mediaPlayer.metrics
    }

}