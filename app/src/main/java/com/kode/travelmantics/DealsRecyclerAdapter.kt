package com.kode.travelmantics

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.kode.travelmantics.utils.FirebaseUtil
import com.squareup.picasso.Picasso

class DealsRecyclerAdapter : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>(){
    var firebaseDatabase: FirebaseDatabase
    var databaseReference: DatabaseReference
    var childEventListener: ChildEventListener
    var deals: ArrayList<TravelDeal>

    init {
        firebaseDatabase = FirebaseUtil.firebaseDatabase
        databaseReference = FirebaseUtil.databaseReference
        deals = FirebaseUtil.deals

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val td = p0.getValue(TravelDeal::class.java)
                Log.d("Deal: ", td?.title)
                td?.id = p0.key!!
                deals.add(td!!)

                notifyItemChanged(deals.size -1)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
        }
        databaseReference.addChildEventListener(childEventListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.deal_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return deals.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deal = deals[position]
        holder.bind(deal)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.tvTitle)
        private val textDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val textPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val imageView: ImageView = itemView.findViewById(R.id.imageDeal)

        fun bind(deal: TravelDeal) {
            titleView.text = deal.title
            textDescription.text = deal.description
            textPrice.text = deal.price
            showImage(deal.imageUrl)
        }

        private fun showImage(url: String?) {
            if ((url != null || url != "") && url?.isEmpty() == false) {
                Picasso.get().load(url).resize(160, 160)
                    .centerCrop()
                    .into(imageView)
            }
        }

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                Log.d("Click: ", position.toString())
                val deal = deals[position]

                val intent = Intent(it.context, DealActivity::class.java)
                intent.putExtra("Deal", deal)
                it.context.startActivity(intent)
            }
        }
    }
}