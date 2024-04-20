package com.example.reactivepaper

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.ContextCompat.getSystemService

class SimulationViewer(width: Int, height: Int){

    private val simulation = Simulation(width.toDouble(), height.toDouble(), cellSize = 70)
    private var fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    init{
        simulation.genParticle(10)
        for (particle in simulation.particles) {
            particle.color = Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
        }
    }

    fun drawFrame(canvas: Canvas) {
        fillPaint.color = Color.BLACK
        canvas.drawPaint(fillPaint)
        for (particle in simulation.particles) {
            fillPaint.color = particle.color
            canvas.drawCircle(
                particle.position.first.toFloat(),
                particle.position.second.toFloat(),
                particle.radius.toFloat(),
                fillPaint
            )
        }
        simulation.genParticle(1)
        simulation.updateParticles(1.0)
    }

    fun changeGravity(){
        simulation.GRAVITY *= -1
    }

    fun updateDown(newDown: Pair<Double, Double>){
        simulation.DOWN = newDown;
    }


}