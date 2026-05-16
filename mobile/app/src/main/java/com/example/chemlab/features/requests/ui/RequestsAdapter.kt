package com.example.chemlab.features.requests.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chemlab.R
import com.example.chemlab.features.requests.data.dto.RequestDTO

class RequestsAdapter(
    private var items: List<RequestDTO> = emptyList(),
    private val onItemClick: (RequestDTO) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRequestId: TextView = view.findViewById(R.id.tvRequestId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRequester: TextView = view.findViewById(R.id.tvRequester)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = items[position]
        holder.tvRequestId.text = "#${req.id}"
        holder.tvRequester.text = req.requesterUsername ?: "—"
        holder.tvStatus.text = req.status ?: "—"

        val itemCount = req.items?.size ?: 0
        val date = req.createdAt?.substringBefore('T') ?: "—"
        holder.tvDetails.text = "$itemCount item(s) · $date"

        // Status badge color (matches web badge-req-* classes)
        val statusColor = when (req.status?.uppercase()) {
            "PENDING" -> 0xFFF59E0B.toInt()
            "APPROVED" -> 0xFF10B981.toInt()
            "REJECTED" -> 0xFFEF4444.toInt()
            "RELEASED" -> 0xFF3B82F6.toInt()
            "COMPLETED" -> 0xFFA78BFA.toInt()
            else -> 0xFF94A3B8.toInt()
        }
        holder.tvStatus.setTextColor(statusColor)

        holder.itemView.setOnClickListener { onItemClick(req) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<RequestDTO>) {
        items = newItems
        notifyDataSetChanged()
    }
}
