package com.hyphenate.easeui.feature.chat.forward.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.common.ChatType
import com.hyphenate.easeui.databinding.EaseItemForwardLayoutBinding
import com.hyphenate.easeui.feature.chat.forward.viewholder.EaseGroupForwardViewHolder
import com.hyphenate.easeui.feature.group.adapter.EaseGroupListAdapter
import com.hyphenate.easeui.interfaces.OnForwardClickListener

class EaseGroupListForwardAdapter: EaseGroupListAdapter() {
    private var forwardClickListener: OnForwardClickListener? = null
    private val sentGroupList = mutableListOf<Int>()
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatGroup> {
        return EaseGroupForwardViewHolder(EaseItemForwardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatGroup>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if (holder is EaseGroupForwardViewHolder){
            holder.btnForward.isEnabled = !sentGroupList.contains(position)
            holder.btnForward.text = if (sentGroupList.contains(position)) holder.itemView.context.getString(R.string.ease_chat_reply_forwarded)
            else holder.itemView.context.getString(R.string.ease_action_forward)
            holder.btnForward.setOnClickListener { view ->
                sentGroupList.add(position)
                holder.btnForward.isEnabled = false
                holder.btnForward.text = holder.itemView.context.getString(R.string.ease_chat_reply_forwarded)
                item?.let {
                    forwardClickListener?.onForwardClick(view, it.groupId, ChatType.GroupChat)
                }
            }
        }
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }
}