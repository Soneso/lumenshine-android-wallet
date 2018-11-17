package com.soneso.lumenshine.presentation.wallets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.Wallet
import com.soneso.lumenshine.presentation.widgets.LsCardView
import kotlinx.android.synthetic.main.view_unfunded_wallet_card.view.*

class FundedWalletCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.style.AppTheme_LsCardView
) : LsCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_funded_wallet_card, this)
    }

    fun populate(wallet: Wallet) {

        nameView.text = wallet.name
        if (wallet.federationAddress.isBlank()) {
            subtitleView.setText(R.string.no_stellar_address)
        } else {
            subtitleView.text = wallet.federationAddress
        }
    }
}