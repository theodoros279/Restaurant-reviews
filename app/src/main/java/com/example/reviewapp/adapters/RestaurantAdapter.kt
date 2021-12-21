package com.example.reviewapp.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.ReviewsActivity
import com.example.reviewapp.models.RestaurantModel
import com.example.reviewapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RestaurantAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<RestaurantModel> = ArrayList()
    private val db = Firebase.firestore
    private var mAuth = FirebaseAuth.getInstance()
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_restaurant_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = items[position]
        val btn = holder.itemView.findViewById<Button>(R.id.card_btn)

        when(holder){
            is ViewHolder -> {
                holder.bind(info)
                btn.setOnClickListener {
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

    fun submitList(restaurantList: MutableList<RestaurantModel>) {
        items = restaurantList
    }

    inner class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val restaurantImage = itemView.findViewById<ImageView>(R.id.card_image)
        private val restaurantName = itemView.findViewById<TextView>(R.id.card_title)

        fun bind(modelItem: RestaurantModel){
            restaurantName.text = modelItem.name
            Glide.with(itemView)
                .load(modelItem.image)
                .placeholder(R.drawable.ic_launcher_background)
                .into(restaurantImage)
        }
    }
}