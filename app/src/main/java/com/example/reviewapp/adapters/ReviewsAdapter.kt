package com.example.reviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.R
import com.example.reviewapp.models.ReviewsModel
import de.hdodenhof.circleimageview.CircleImageView

class ReviewsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<ReviewsModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_review_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = items[position]

        when(holder){
            is ViewHolder -> {
                holder.bind(info)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(reviewsList: MutableList<ReviewsModel>) {
        items = reviewsList
    }

    inner class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val userName = itemView.findViewById<TextView>(R.id.user_review_name)
        private val userPhoto = itemView.findViewById<CircleImageView>(R.id.user_review_image)
        private val review = itemView.findViewById<TextView>(R.id.display_review)
        private val rating = itemView.findViewById<TextView>(R.id.display_rating)
        private val location = itemView.findViewById<TextView>(R.id.display_location)
        private val reviewImage = itemView.findViewById<ImageView>(R.id.display_review_image)

        fun bind(modelItem: ReviewsModel) {
            userName.text = modelItem.userName
            review.text = modelItem.review
            rating.text = modelItem.rating
            location.text = modelItem.location
            if(modelItem.userPhoto != "") {
                Glide.with(itemView)
                    .load(modelItem.userPhoto)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(userPhoto)
            }
            if(modelItem.image != "") {
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