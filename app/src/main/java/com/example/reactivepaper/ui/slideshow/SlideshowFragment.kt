package com.example.reactivepaper.ui.slideshow

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.reactivepaper.MainActivity
import com.example.reactivepaper.MainViewModel
import com.example.reactivepaper.SimWallpaperService
import com.example.reactivepaper.databinding.FragmentSlideshowBinding
import com.yausername.youtubedl_android.YoutubeDL.getInstance
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private val progressText = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.changeWallpaperButton.setOnClickListener {
            changeWallpaper(requireContext())
        }

        binding.downloadVideoButton.setOnClickListener {
            if (binding.selectedVideoText.text.toString().isEmpty()) {
                Toast.makeText(context, "Please enter a link", Toast.LENGTH_SHORT).show()
            }
            else{
                this.binding.errorText.visibility = View.INVISIBLE
                getLink(binding.selectedVideoText.text.toString())
            }
        }

        progressText.observe(viewLifecycleOwner) {
            binding.errorText.text = it
        }

        mainViewModel.refreshFiles()

        return root
    }

    private fun changeWallpaper(context: Context){
        try{
            val fileName = mainViewModel.getChosenFileName()
            Log.d("SlideshowFragment", "changeWallpaper: $fileName")
            if (fileName != null) {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, SimWallpaperService::class.java)
                )
                intent.putExtra("File", fileName)

                activity?.startService(
                    Intent(this.context, SimWallpaperService::class.java)
                    .putExtra("File", fileName)
                )
                startActivity(intent)
            }
            else {
                Toast.makeText(context, "Please select a file", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            this.binding.errorText.text = "Error: ${e.message}"
            this.binding.errorText.visibility = View.VISIBLE
        }
    }

    private fun getLink(link: String){
        //Create background thread to download video
        var error = false
        this.binding.errorText.visibility = View.VISIBLE
        this.binding.errorText.setTextColor(Color.GRAY)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val youtubeDLDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "ReactivePaper"
                )
                val request = YoutubeDLRequest(link)
                request.addOption("-x")
                request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
                getInstance().execute(request) { progress, etaInSeconds, line ->
                    println("$progress% (ETA $etaInSeconds seconds), $line")
                    if (line.contains("Deleting original file")) {
                        mainViewModel.refreshFiles()
                        Log.d("TAG", "refreshed")
                    }
                    this@SlideshowFragment.binding.errorText.setTextColor(Color.GRAY)
                    if (progress.toInt() == -1) {
                        progressText.postValue("Download Starting")
                    }
                    else if (progress.toInt() == 100) {
                        progressText.postValue("Download Complete")
                    }
                    else {
                        progressText.postValue("Downloading: $progress%")
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "failed to get", e)
                error = true
                progressText.postValue("Error: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}