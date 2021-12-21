package com.example.reviewapp.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.R
import com.example.reviewapp.ReviewsActivity
import com.example.reviewapp.models.FavoritesModel

class FavoritesAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items: MutableList<FavoritesModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_favorites, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = items[position]

        when(holder){
            is FavoritesAdapter.ViewHolder -> {
                holder.bind(info)
                holder.itemView.setOnClickListener {
                    val activity = holder.itemView.context as Activity
                    val intent = Intent(activity, ReviewsActivity::class.java)
                    intent.putExtra("name", info.name)
                    intent.putExtra("image", info.image)
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(reviewsList: MutableList<FavoritesModel>) {
        items = reviewsList
    }

    inner class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.favorite_rest_name)
        private val image = itemView.findViewById<ImageView>(R.id.favorite_rest_image)

        fun bind(modelItem: FavoritesModel) {
            name.text = modelItem.name
            Glide.with(itemView)
                .load(modelItem.image)
                .placeholder(R.drawable.ic_launcher_background)
                .into(image)
        }
    }
}