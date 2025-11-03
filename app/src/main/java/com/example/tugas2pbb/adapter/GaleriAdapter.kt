package com.example.tugas2pbb.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GaleriAdapter(private val images: List<String>) :
    RecyclerView.Adapter<GaleriAdapter.GaleriViewHolder>() {

    inner class GaleriViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgGallery: ImageView = view.findViewById(R.id.imgGallery)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaleriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_galeri, parent, false)
        return GaleriViewHolder(view)
    }

    override fun onBindViewHolder(holder: GaleriViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(images[position])
            .centerCrop()
            .into(holder.imgGallery)
    }

    override fun getItemCount(): Int = images.size
}