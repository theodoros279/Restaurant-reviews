package com.example.reviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.R
import com.example.reviewapp.models.MyReviewsModel

class MyReviewsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items: MutableList<MyReviewsModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_my_reviews, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = items[position]

        when(holder){
            is MyReviewsAdapter.ViewHolder -> { 
                holder.bind(info)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(reviewsList: MutableList<MyReviewsModel>) {
        items = reviewsList
    }

    inner class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        private val review = itemView.findViewById<TextView>(R.id.display_my_review)
        private val rating = itemView.findViewById<TextView>(R.id.display_my_rating)
        private val location = itemView.findViewById<TextView>(R.id.display_my_location)
        private val reviewImage = itemView.findViewById<ImageView>(R.id.display_my_review_image)

        fun bind(modelItem: MyReviewsModel) {
            review.text = modelItem.review
            rating.text = modelItem.rating
            location.text = modelItem.location

            if (modelItem.image != "") {
                Glide.with(itemView)
                    .load(modelItem.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(reviewImage)
            } else {
                reviewImage.visibility = View.GONE
            }
        }
    }
}