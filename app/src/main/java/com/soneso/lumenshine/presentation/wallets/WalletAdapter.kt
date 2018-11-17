package com.soneso.lumenshine.presentation.wallets

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.Wallet

class WalletAdapter : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    private val walletData = ArrayList<Wallet>()

    fun setWalletData(wallets: List<Wallet>) {

        walletData.clear()
        walletData.addAll(wallets)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {

        val view = FundedWalletCardView(parent.context)
        val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.marginStart = parent.resources.getDimensionPixelSize(R.dimen.size_5)
        params.marginEnd = parent.resources.getDimensionPixelSize(R.dimen.size_5)
        view.layoutParams = params
        return WalletViewHolder(view)
    }

    override fun getItemCount() = walletData.size

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {

        holder.walletView.populate(walletData[position])
    }

    inner class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val walletView = view as FundedWalletCardView
    }
}