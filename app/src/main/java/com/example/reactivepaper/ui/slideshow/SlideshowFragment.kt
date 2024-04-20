package com.example.reactivepaper.ui.slideshow

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reactivepaper.SimWallpaperService
import com.example.reactivepaper.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        return root
    }

    private fun changeWallpaper(context: Context){
        try{



//            val contract = ActivityResultContracts.StartActivityForResult()
//            contract.createIntent(
//                context,
//                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
//                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                    ComponentName(context, SimWallpaperService::class.java)
//                )
//            )
//
//            val launcher = registerForActivityResult(contract) { result ->
//                if (result.resultCode == RESULT_OK) {
//
//                }
//            }

            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, SimWallpaperService::class.java)
            )
            startActivity(intent)


        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}