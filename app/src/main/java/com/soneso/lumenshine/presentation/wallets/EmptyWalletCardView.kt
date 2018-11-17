package com.soneso.lumenshine.presentation.wallets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.WalletCardData
import com.soneso.lumenshine.presentation.widgets.LsCardView
import kotlinx.android.synthetic.main.view_empty_wallet_card.view.*

class EmptyWalletCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.style.AppTheme_LsCardView
) : LsCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_empty_wallet_card, this)
    }

    fun populate(wallet: WalletCardData) {

        nameView.text = wallet.name
    }
}