package com.example.reactivepaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.security.AccessController.getContext
import java.util.concurrent.Executor

class SimWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return SimEngine()
    }

    inner class SimEngine : Engine() {

        private lateinit var simulation : SimulationViewer;

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable { nextFrame() }

        private val rotation = GyroSensor(this@SimWallpaperService)

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            val canvas = holder?.lockCanvas() ?: return
            simulation = SimulationViewer(canvas.width, canvas.height)
            simulation.drawFrame(canvas)
            holder.unlockCanvasAndPost(canvas)
            handler.postDelayed(runnable, 17)
        }

//        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
//            val canvas = holder?.lockCanvas() ?: return
//            simulation.drawFrame(canvas)
//            holder.unlockCanvasAndPost(canvas)
//        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            event?.let {
                if (it.action == MotionEvent.ACTION_DOWN) {
//                    simulation.changeGravity()
                }

            }

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

}