package com.example.tugas2pbb.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tugas2pbb.R
import com.example.tugas2pbb.util.FileUtils
import java.io.File

class GaleriAdapter(
    private val context: Context,
    private val imageList: List<Map<String, String>>,
    private val onClick: (Map<String, String>) -> Unit,
    private val onDoubleClick: (Map<String, String>) -> Unit,
    private val onLongClick: (Map<String, String>) -> Unit
) : RecyclerView.Adapter<GaleriAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "GaleriAdapter"
        private const val DOUBLE_CLICK_DELAY = 250L
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textDate: TextView = view.findViewById(R.id.textDate)
        var lastClickTime: Long = 0L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_galeri, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = imageList[position]
        val namaFile = data["namaFile"]
        val judul = data["judul"].orEmpty().ifBlank { "Tanpa Judul" }
        val tanggal = data["tanggalUpload"].orEmpty().ifBlank { "-" }

        holder.textTitle.text = judul
        holder.textDate.text = tanggal

        try {
            if (!namaFile.isNullOrEmpty()) {
                val file: File = FileUtils.getImageFile(context, namaFile)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    holder.imageView.setImageBitmap(bitmap)
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_image_placeholder)
                    Log.w(TAG, "File tidak ditemukan: $namaFile")
                }
            } else {
                holder.imageView.setImageResource(R.drawable.ic_image_placeholder)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error load gambar: ${e.message}")
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder)
        }

        holder.itemView.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - holder.lastClickTime < DOUBLE_CLICK_DELAY) {
                onDoubleClick(data)
            } else {
                onClick(data)
            }
            holder.lastClickTime = now
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(data)
            true
        }
    }
}
