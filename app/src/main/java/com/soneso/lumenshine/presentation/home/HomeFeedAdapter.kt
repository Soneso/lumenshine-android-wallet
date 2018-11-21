package com.soneso.lumenshine.presentation.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.BlogPostPreview
import com.soneso.lumenshine.domain.data.InternalLink
import com.soneso.lumenshine.domain.data.Wallet
import com.soneso.lumenshine.domain.util.Mock
import com.soneso.lumenshine.presentation.wallets.EmptyWalletCardView
import com.soneso.lumenshine.presentation.wallets.FundedWalletCardView
import com.soneso.lumenshine.presentation.wallets.UnfundedWalletCardView
import com.soneso.lumenshine.presentation.wallets.WalletCardView
import kotlinx.android.synthetic.main.item_home_chart.view.*
import kotlinx.android.synthetic.main.item_home_internal_link.view.*
import kotlinx.android.synthetic.main.item_home_web_link.view.*

/**
 * Adapter.
 * Created by cristi.paval on 3/8/18.
 */
class HomeFeedAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val walletData = ArrayList<Wallet>()

    fun addWalletData(wallet: Wallet): Int {

        var index = walletData.indexOfFirst { it.id == wallet.id }
        if (index > -1) {
            walletData[index] = wallet
            notifyItemChanged(index)
        } else {
            index = walletData.size
            walletData.add(wallet)
            notifyItemInserted(index)
        }
        return index
    }

    var onBlogLinkClickListener: ((BlogPostPreview) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.view_funded_wallet_card, R.layout.view_unfunded_wallet_card, R.layout.view_empty_wallet_card -> {
                val view = when (viewType) {
                    R.layout.view_funded_wallet_card -> FundedWalletCardView(parent.context)
                    R.layout.view_unfunded_wallet_card -> UnfundedWalletCardView(parent.context)
                    else -> EmptyWalletCardView(parent.context)
                }
                view.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply {
                            marginStart = parent.resources.getDimensionPixelSize(R.dimen.size_5)
                            marginEnd = parent.resources.getDimensionPixelSize(R.dimen.size_5)
                        }
                return WalletViewHolder(view)
            }
            R.layout.item_home_web_link -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_web_link, parent, false)
                BlogPostHolder(view)
            }
            R.layout.item_home_internal_link -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_internal_link, parent, false)
                InternalLinkHolder(view)
            }
            R.layout.item_home_chart -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_chart, parent, false)
                ChartHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_load_more, parent, false)
                LoadMoreHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            walletData.size -> R.layout.item_home_chart
            walletData.size + 1 -> R.layout.item_home_web_link
            walletData.size + 2 -> R.layout.item_home_internal_link
            walletData.size + 3 -> R.layout.item_home_load_more
            else -> {
                val wallet = walletData[position]
                return if (!wallet.isLoaded()) {
                    R.layout.view_empty_wallet_card
                } else if (wallet.hasBalances()) {
                    R.layout.view_funded_wallet_card
                } else {
                    R.layout.view_unfunded_wallet_card
                }
            }
        }
    }

    override fun getItemCount() = walletData.size + 4

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.view_funded_wallet_card, R.layout.view_unfunded_wallet_card, R.layout.view_empty_wallet_card -> {
                (holder as WalletViewHolder).populate(walletData[position])
            }
            R.layout.item_home_web_link -> {
                (holder as BlogPostHolder).fillData(Mock.mockBlogPost())
            }
            R.layout.item_home_internal_link -> {
                (holder as InternalLinkHolder).fillData(Mock.mockInternalLink())
            }
            R.layout.item_home_chart -> {
                (holder as ChartHolder).fillData()
            }
            else -> {

            }
        }
    }

    class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun populate(wallet: Wallet) {
            (itemView as WalletCardView).populate(wallet)
        }

    }

    inner class LoadMoreHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ChartHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val chartView = view.line_chart

        fun fillData() {
            chartView.legend.isEnabled = false
            chartView.description.isEnabled = false
            chartView.axisLeft.setDrawLabels(false)
            chartView.axisLeft.gridColor = ContextCompat.getColor(chartView.context, R.color.cyan_300)
            chartView.axisLeft.axisLineColor = Color.TRANSPARENT
            chartView.axisRight.isEnabled = false
            chartView.xAxis.setDrawLabels(false)
            chartView.xAxis.setDrawGridLines(false)
            chartView.xAxis.axisLineColor = Color.TRANSPARENT
            val dataSet = LineDataSet(listOf(
                    Entry(0f, 0.5f),
                    Entry(1f, 0.5f),
                    Entry(2f, 0.55f),
                    Entry(3f, 0.6f),
                    Entry(4f, 0.625f)
            ), null)
            dataSet.color = Color.WHITE
            dataSet.lineWidth = chartView.resources.getDimension(R.dimen.size_05)
            dataSet.circleRadius = chartView.resources.getDimension(R.dimen.size_1_5)
            dataSet.valueTextSize = 8f
            val data = LineData(dataSet)
            data.setValueTextColor(Color.WHITE)
            chartView.data = data
        }
    }

    inner class BlogPostHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private val imageView = view.blog_image
        private val titleView = view.blog_title
        private val subtitleView = view.blog_subtitle
        private val paragraphView = view.blog_paragraph
        private val readMoreButton = view.blog_button

        fun fillData(blog: BlogPostPreview) {
            Glide.with(itemView.context)
                    .load(blog.imageUrl)
                    .into(imageView)
            titleView.text = blog.title
            subtitleView.text = blog.subtitle
            paragraphView.text = blog.paragraph
            readMoreButton.setOnClickListener {
                onBlogLinkClickListener?.invoke(blog)
            }
        }
    }

    inner class InternalLinkHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private val imageView = view.card_image
        private val titleView = view.card_title
        private val paragraphView = view.card_paragraph

        fun fillData(link: InternalLink) {
            imageView.setImageResource(link.iconResId)
            titleView.text = link.title
            paragraphView.text = link.description
        }
    }
}