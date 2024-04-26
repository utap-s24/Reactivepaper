package com.example.reactivepaper

import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel : ViewModel() {

    private var storeFiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "ReactivePaper").listFiles()

    private val chosenFilePosition = MutableLiveData<Int>()

    private val files = MutableLiveData<List<String>>().apply {
        val tempList = mutableListOf<String>()
        for (file in this@MainViewModel.storeFiles!!) {
            tempList.add(file.name)
        }
        postValue(tempList)
    }

    init {
        chosenFilePosition.value = -1
    }

    private fun getFiles(): List<String> {
        val tempList = mutableListOf<String>()
        for (file in this.storeFiles!!) {
            tempList.add(file.name)
        }
        return tempList
    }

    fun refreshFiles() {
        storeFiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ReactivePaper").listFiles()
        Log.d("MainViewModel", "refreshFiles: ${storeFiles?.size}")
        files.postValue(getFiles())
    }

    fun oberveFiles(): LiveData<List<String>> {
        return files
    }

    fun setChosenFilePosition(position: Int) {
        Log.d("MainViewModel", "setChosenFilePosition: $position")
        chosenFilePosition.value = position
    }

    fun observeChosenFilePosition(): LiveData<Int> {
        return chosenFilePosition
    }

    fun getChosenFilePosition(): Int {
        Log.d("MainViewModel", "getChosenFilePosition2: ${chosenFilePosition.value!!}")
        return chosenFilePosition.value!!
    }

    fun getChosenFileName(): String? {
        Log.d("MainViewModel", "getChosenFilePosition: ${getChosenFilePosition()}")
        if (chosenFilePosition.value == -1) {
            return null
        }
        return storeFiles?.get(getChosenFilePosition())?.absolutePath
    }
}