package com.soneso.lumenshine.presentation.wallets

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.Wallet

class WalletAdapter : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    private val walletData = ArrayList<Wallet>()

    fun addWalletData(wallet: Wallet) {

        var index = walletData.indexOfFirst { it.id == wallet.id }
        if (index > -1) {
            walletData[index] = wallet
            notifyItemChanged(index)
        } else {
            index = walletData.size
            walletData.add(wallet)
            notifyItemInserted(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {

        val view = when (viewType) {
            R.layout.view_funded_wallet_card -> FundedWalletCardView(parent.context)
            R.layout.view_unfunded_wallet_card -> UnfundedWalletCardView(parent.context)
            else -> EmptyWalletCardView(parent.context)
        }
        val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.marginStart = parent.resources.getDimensionPixelSize(R.dimen.size_5)
        params.marginEnd = parent.resources.getDimensionPixelSize(R.dimen.size_5)
        view.layoutParams = params
        return WalletViewHolder(view)

    }

    override fun getItemViewType(position: Int): Int {
        val wallet = walletData[position]
        return if (!wallet.isLoaded()) {
            R.layout.view_empty_wallet_card
        } else if (wallet.hasBalances()) {
            R.layout.view_funded_wallet_card
        } else {
            R.layout.view_unfunded_wallet_card
        }
    }

    override fun getItemCount() = walletData.size

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.walletView.populate(walletData[position])
    }

    inner class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val walletView = view as WalletCardView
    }
}