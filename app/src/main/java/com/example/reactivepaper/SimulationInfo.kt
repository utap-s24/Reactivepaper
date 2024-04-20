package com.example.reactivepaper

import androidx.lifecycle.ViewModel

class SimulationInfo() : ViewModel() {

    var numParticles = 100
    private var particles = ArrayList<Particle>();

    fun getParticles(): ArrayList<Particle> {
        return particles
    }

    fun setParticles(particles: ArrayList<Particle>) {
        this.particles = particles
    }




}
class Particle(
    var position: Pair<Double, Double>,
    var speed: Pair<Double, Double>,
    var forceSum: Pair<Double, Double>,
    var radius: Double,
    var color: Int) {

    constructor() : this(Pair(0.0, 0.0), Pair(0.0, 0.0), Pair(0.0, 0.0), 0.0, 0)

}
