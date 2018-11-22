package com.soneso.lumenshine.presentation.wallets

import android.content.Context
import android.util.AttributeSet
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.Wallet
import com.soneso.lumenshine.presentation.widgets.LsCardView

abstract class WalletCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.style.AppTheme_LsCardView
) : LsCardView(context, attrs, defStyleAttr) {

    abstract fun populate(wallet: Wallet)
}