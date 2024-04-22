package com.example.reactivepaper

import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.sqrt

class Simulation(private val width: Double, private val height: Double, private val cellSize: Int) {
    private val simData = SimulationInfo()
    private var numParticles = 100

    var particles = ArrayList<Particle>()
    private lateinit var grid : Array<Array<ArrayList<Particle>>>;

    var GRAVITY = 1.0
    var DOWN: Pair<Double, Double> = Pair(0.0, 1.0)



    init {
//        initData()
        initGrid(width.toInt(), height.toInt(), cellSize)

    }
    private fun initData() {
        numParticles = simData.numParticles
        simData.setParticles(particles)
    }

    private fun initGrid(width: Int, height: Int, cellSize: Int){
        val gridWidth = (width / cellSize) + 1
        val gridHeight = (height / cellSize) + 1
        grid = Array(gridWidth) { Array(gridHeight) { ArrayList<Particle>() } }
    }

    private fun save(){
        simData.numParticles = numParticles;
        simData.setParticles(particles)
    }

    fun genParticle(number: Int){
        for (x in 0 until number.toInt()){
            if (particles.size < this.numParticles){
//                val pos = Pair((0..width).random().toDouble(), (0..height).random().toDouble())
                val pos = Pair(width / 3 + x, height / 2)
                val speed = Pair(0.0, 0.0)
                val accel = Pair(0.0, 0.0)

                val particle = Particle(pos, speed, accel, (20..cellSize - 10).random().toDouble(),
                    Color.rgb((0..255).random(), (0..255).random(), (0..255).random()))

                particles.add(particle)

            }
        }
    }


    fun updateParticles(time: Double) {
        val substeps = 12
        for (i in 0 until substeps){
            for (particle in particles) {

                particle.forceSum = calcForceSum()


                particle.position = calcNextPos(particle, time / substeps)

                particle.speed = addVector(particle.speed, particle.forceSum)
                particle.speed = scaleVector(particle.speed, 0.8)
                if (abs(particle.speed.first) < 0.05 && abs(particle.speed.second) < 0.05){
                    particle.speed = Pair(0.0, 0.0)
                }
            }
            fillGrid()
            for (x in 0 until 5){
                fixIntersectedParticles()
            }
        }
    }

    private fun calcNextPos(particle : Particle, time : Double): Pair<Double, Double> {
        //New pos after time
        var next = addVector(particle.position, scaleVector(particle.speed, time))

//        //Check if particle is out of bounds, bounce back
//        if (next.first < particle.radius || next.first > width - particle.radius){
//            particle.speed = Pair(-particle.speed.first, particle.speed.second)
//        }
//        if (next.second < particle.radius || next.second > height - particle.radius){
//            particle.speed = Pair(particle.speed.first, -particle.speed.second)
//        }

        //Move any out of bounds particles back in
        particle.position = next
        return coerceParticle(particle)
    }

    private fun coerceParticle(particle: Particle): Pair<Double, Double> {
        return Pair(particle.position.first.coerceIn(particle.radius, width - particle.radius),
            particle.position.second.coerceIn(particle.radius, height - particle.radius))
    }

    private fun fixIntersectedParticles() {
//        for (particle in particles) {
//            for (other in getSurroundingParticles(particle)) {
//                fixIntersectedPair(particle, other)
//            }
//        }
        for (x in grid.indices){
            for (y in grid[x].indices){
                for (particle in grid[x][y]){
                    for (other in getSurroundingParticles(particle)){
                        fixIntersectedPair(particle, other)
                    }
                }
            }
        }
    }

    private fun fixIntersectedParticlesMultiThreaded() {
        runBlocking {
            val halfSize = (grid.size + 1) / 2
            val deferred1 = async {
                for (x in 0 until halfSize) {
                    for (y in grid[x].indices) {
                        for (particle in grid[x][y]) {
                            for (other in getSurroundingParticles(particle)) {
                                fixIntersectedPair(particle, other)
                            }
                        }
                    }
                }
            }
            val deferred2 = async {
                for (x in halfSize until grid.size) {
                    for (y in grid[x].indices) {
                        for (particle in grid[x][y]) {
                            for (other in getSurroundingParticles(particle)) {
                                fixIntersectedPair(particle, other)
                            }
                        }
                    }
                }
            }
            deferred1.await()
            deferred2.await()
        }
    }

    private fun fixIntersectedPair(particle: Particle, other: Particle) {
        if (particle != other) {
            var diff = subVector(other.position, particle.position)
            val distanceSquared = dotProduct(diff, diff)
            val minDistance = particle.radius + other.radius
            if (distanceSquared < minDistance * minDistance) {
                var distanceLength = sqrt(distanceSquared)
                val distanceCorrection = (minDistance - distanceLength) / 2
                if (distanceLength == 0.0) {
                    diff = Pair(0.0, 1.0)
                    distanceLength = minDistance
                }
                val correction = scaleVector(normalizeVector(diff), distanceCorrection)
                particle.position = subVector(particle.position, correction )

                //elastic collision, equal magnitude, direction of correction
                val magSpeed = sqrt(dotProduct(particle.speed, particle.speed))
//                particle.speed = scaleVector(correction, magSpeed / distanceLength)
                other.position = addVector(other.position, correction)

//                other.speed = scaleVector(correction, magSpeed / distanceLength)
            }
            coerceParticle(particle)
            coerceParticle(other)
        }
    }

    fun getSurroundingParticles(particle: Particle): ArrayList<Particle> {
        val surroundingParticles = ArrayList<Particle>()
        val x = (particle.position.first / width * grid.size).toInt()
        val y = (particle.position.second / height * grid[0].size).toInt()
        for (i in -1..1) {
            for (j in -1..1) {
                val xIndex = x + i
                val yIndex = y + j
                if (xIndex >= 0 && xIndex < grid.size && yIndex >= 0 && yIndex < grid[0].size) {
                    surroundingParticles.addAll(grid[xIndex][yIndex])
                }
            }
        }
        return surroundingParticles
    }

    fun fillGrid() {
        for (x in grid.indices) {
            for (y in 0 until grid[x].size) {
                grid[x][y].clear()
            }
        }
        for (particle in particles) {
            val x = (particle.position.first / cellSize).toInt()
            val y = (particle.position.second / cellSize).toInt()
            if (x >= 0 && x < grid.size && y >= 0 && y < grid[0].size){
                grid[x][y].add(particle)
            }
        }
    }

    private fun borderCheck(position: Pair<Double, Double>): Boolean {

        return true
    }

    private fun calcForceSum(): Pair<Double, Double> {
        //Acceleration
        val down = DOWN
        return scaleVector(down, GRAVITY)
    }

    private fun updateDownDirection(newDown: Pair<Double, Double>) {
        DOWN = newDown;
    }

    private fun calcForceSum(particle: Particle): Pair<Double, Double> {
        var forceSum = Pair(0.0, 0.0)
        for (other in particles) {
            if (particle != other) {
                val distance = subVector(other.position, particle.position)
                val distanceSquared = dotProduct(distance, distance)
                val force = scaleVector(distance, 1.0f / distanceSquared)
                forceSum = addVector(forceSum, force)
            }
        }
        return forceSum
    }

    private fun addVector(a: Pair<Double, Double>, b: Pair<Double, Double>): Pair<Double, Double> {
        return Pair(a.first + b.first, a.second + b.second)
    }

    private fun subVector(a: Pair<Double, Double>, b: Pair<Double, Double>): Pair<Double, Double> {
        return Pair(a.first - b.first, a.second - b.second)
    }

    private fun scaleVector(a: Pair<Double, Double>, b: Double): Pair<Double, Double> {
        return Pair(a.first * b, a.second * b)
    }

    private fun dotProduct(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
        return a.first * b.first + a.second * b.second
    }

    private fun normalizeVector(a: Pair<Double, Double>): Pair<Double, Double> {
        val length = sqrt(a.first * a.first + a.second * a.second)
        return Pair(a.first / length, a.second / length)
    }



    private fun initParticles() {

    }

    private fun update() {
        updateParticles(0.1)
    }

}