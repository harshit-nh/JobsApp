package com.example.testui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testui.R
import com.example.testui.models.JobData
import com.google.android.material.button.MaterialButton

class JobAdapter(private val items: List<JobData>):
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private var showProceedBtn:Boolean = false

    var onItemClick: ((JobData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.total_jobs_item,parent,false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {

        val jobs = items[position]
        holder.companyName.text = jobs.companyName
        holder.contact.text = jobs.contactName
        holder.workAssigned.text = jobs.workAssigned
        holder.address.text = jobs.address
        holder.distance.text = jobs.distance

        holder.proceedBtn.visibility = if(showProceedBtn) View.VISIBLE else View.GONE

        holder.proceedBtn.setOnClickListener {
            onItemClick?.invoke(jobs)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setShowProceedBtn(show: Boolean){
        showProceedBtn = show
        notifyDataSetChanged()
    }


    class JobViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val companyName:TextView = itemView.findViewById(R.id.companyName)
        val contact:TextView = itemView.findViewById(R.id.contactName)
        val workAssigned:TextView = itemView.findViewById(R.id.work)
        val address:TextView = itemView.findViewById(R.id.address)
        val distance:TextView = itemView.findViewById(R.id.distanceTxt)
        val proceedBtn:MaterialButton = itemView.findViewById(R.id.proceedBtn)

    }
}