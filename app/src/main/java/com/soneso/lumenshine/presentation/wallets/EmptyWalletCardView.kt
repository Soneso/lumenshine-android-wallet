package com.soneso.lumenshine.presentation.wallets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.Wallet
import kotlinx.android.synthetic.main.view_empty_wallet_card.view.*

class EmptyWalletCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.style.AppTheme_LsCardView
) : WalletCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_empty_wallet_card, this)
    }

    override fun populate(wallet: Wallet) {

        nameView.text = wallet.name
    }
}