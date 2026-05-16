package com.example.chemlab.features.inventory.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chemlab.R
import com.example.chemlab.features.inventory.data.dto.InventoryItemDTO

class InventoryAdapter(
    private var items: List<InventoryItemDTO> = emptyList(),
    private val onItemClick: (InventoryItemDTO) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemCode: TextView = view.findViewById(R.id.tvItemCode)
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemCode.text = item.itemCode ?: "—"
        holder.tvItemName.text = item.itemName ?: "Unknown"
        holder.tvStatus.text = item.status ?: "—"

        val details = listOfNotNull(
            item.itemType,
            item.quantity?.let { "${it.toInt()} ${item.unit ?: ""}" },
            item.storageLocation
        ).joinToString(" · ")
        holder.tvDetails.text = details.ifEmpty { "—" }

        // Status badge color (matches web CSS badge colors)
        val statusColor = when (item.status?.uppercase()) {
            "AVAILABLE" -> 0xFF10B981.toInt()
            "LOW_STOCK" -> 0xFFF59E0B.toInt()
            "OUT_OF_STOCK" -> 0xFFEF4444.toInt()
            "QUARANTINED" -> 0xFFC084FC.toInt()
            "UNDER_MAINTENANCE" -> 0xFF22D3EE.toInt()
            "DISPOSED" -> 0xFFCBD5E1.toInt()
            else -> 0xFF94A3B8.toInt()
        }
        holder.tvStatus.setTextColor(statusColor)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<InventoryItemDTO>) {
        items = newItems
        notifyDataSetChanged()
    }
}
