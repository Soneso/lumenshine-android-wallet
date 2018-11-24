package com.soneso.lumenshine.presentation.auth.setup

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R

class MnemonicWordAdapter : RecyclerView.Adapter<MnemonicWordAdapter.WordViewHolder>() {

    private val words = ArrayList<String>()

    fun setMnemonic(mnemonic: String) {
        words.clear()
        words.addAll(mnemonic.split(" "))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            WordViewHolder(TextView(parent.context).apply {
                typeface = ResourcesCompat.getFont(context, R.font.encodesans_regular)
                setTextColor(ContextCompat.getColor(context, R.color.lightBlack))
            })

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        (holder.itemView as TextView).text = buildString {
            append(position + 1)
            append(". ")
            append(words[position])
        }
    }

    class WordViewHolder(v: View) : RecyclerView.ViewHolder(v)
}