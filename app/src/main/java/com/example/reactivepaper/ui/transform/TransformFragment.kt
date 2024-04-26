package com.example.reactivepaper.ui.transform

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reactivepaper.R
import com.example.reactivepaper.MainViewModel
import com.example.reactivepaper.databinding.FragmentTransformBinding
import com.example.reactivepaper.databinding.ItemTransformBinding

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewTransform

        fun highlightChosenFilePosition(position: Int, update: Boolean = false): Boolean {
            val selected = mainViewModel.getChosenFilePosition()
            if (!update) {
                return position == selected
            }
            mainViewModel.setChosenFilePosition(position)
            recyclerView.adapter?.notifyItemChanged(selected)
            recyclerView.adapter?.notifyItemChanged(position)

            return false
        }

        val adapter = TransformAdapter(::highlightChosenFilePosition)
        recyclerView.adapter = adapter

        mainViewModel.refreshFiles()
        mainViewModel.oberveFiles().observe(viewLifecycleOwner) {
            Log.d("TransformFragment", "observeFiles: $it")
            adapter.submitList(it)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TransformAdapter( val highLightListener: (Int, Boolean) -> Boolean) :
        ListAdapter<String, TransformAdapter.TransformViewHolder>(object : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {

        private val drawables = listOf(
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10,
            R.drawable.avatar_11,
            R.drawable.avatar_12,
            R.drawable.avatar_13,
            R.drawable.avatar_14,
            R.drawable.avatar_15,
            R.drawable.avatar_16,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context))
            return TransformViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            holder.textView.text = getItem(position)
            holder.imageView.setImageDrawable(
                ResourcesCompat.getDrawable(holder.imageView.resources,
                    drawables[position % drawables.size], null)
            )
            if (highLightListener(position, false)) {
                holder.itemView.setBackgroundColor(ResourcesCompat.getColor(holder.itemView.resources,
                    R.color.purple_200, null))
            }
            else {
                holder.itemView.setBackgroundColor(holder.itemView.solidColor)
            }
        }

        inner class TransformViewHolder(binding: ItemTransformBinding) :
            RecyclerView.ViewHolder(binding.root) {

            val imageView: ImageView = binding.imageViewItemTransform
            val textView: TextView = binding.textViewItemTransform

            init{
                itemView.setOnClickListener {
                   highLightListener(bindingAdapterPosition, true)
                }
                textView.setOnClickListener {
                    highLightListener(bindingAdapterPosition, true)
                }
            }
        }
    }


}