package com.soneso.lumenshine.presentation.wallets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.Wallet
import com.soneso.lumenshine.presentation.util.setTextStyle
import com.soneso.lumenshine.presentation.widgets.LsCardView
import kotlinx.android.synthetic.main.view_unfunded_wallet_card.view.*

class UnfundedWalletCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LsCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_unfunded_wallet_card, this)
    }

    fun populate(wallet: Wallet) {

        subtitleView.text = wallet.federationAddress
        // TODO: cristi.paval, 8/23/18 - integrate balances here
        val balances = emptyList<Any>()

//        balancesLayout.removeAllViews()
//        availabilityLayout.removeAllViews()
        if (balances.isEmpty()) {
//            renderNotFundedWallet()
        } else {
//            renderBalances(balances)
        }
    }

    private fun renderBalances(balances: List<Any>) {


        balanceTitleView.setText(
                if (balances.size > 1) R.string.wallet_balances
                else R.string.wallet_balance
        )
//        availabilityTitleView.visibility = View.VISIBLE
//        availabilityLayout.visibility = View.VISIBLE
        sendButton.visibility = View.VISIBLE
        receiveButton.visibility = View.VISIBLE
        detailsButton.visibility = View.VISIBLE
        fundButton.visibility = View.GONE
        for (b in balances) {
//            availabilityLayout.addView(getBalanceView("1000.000000 XLM"))
//            balancesLayout.addView(getBalanceView("1000.000000 XLM"))
        }
    }

    private fun renderNotFundedWallet() {

        subtitleView.setText(R.string.wallet_not_funded)
        balanceTitleView.setText(R.string.wallet_balance)
//        availabilityTitleView.visibility = View.GONE
//        availabilityLayout.visibility = View.GONE
//        balancesLayout.addView(getBalanceView(resources.getString(R.string.wallet_empty_balance)))
        sendButton.visibility = View.GONE
        receiveButton.visibility = View.GONE
        detailsButton.visibility = View.GONE
        fundButton.visibility = View.VISIBLE
    }

    private fun getBalanceView(text: String): TextView {

        val textView = TextView(context)
        textView.setTextStyle(R.style.LsCardText)
        textView.text = text
        return textView
    }
}