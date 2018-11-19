package com.soneso.lumenshine.presentation.wallets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.size
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.Wallet
import com.soneso.lumenshine.model.entities.wallet.WalletBalanceEntity
import com.soneso.lumenshine.presentation.util.setStyleCompat
import com.soneso.lumenshine.presentation.widgets.LsCardView
import kotlinx.android.synthetic.main.view_funded_wallet_card.view.*

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
        if (wallet.balances?.size ?: 0 > 1) {
            balanceTitleView.setText(R.string.wallet_balances)
        } else {
            balanceTitleView.setText(R.string.wallet_balance)
        }
        populateBalances(wallet.balances ?: return)
        populateAvailabilities(wallet.minimumAccountBalance(), wallet.balances ?: return)
    }

    private fun populateBalances(balances: List<WalletBalanceEntity>) {
        if (balancesView.childCount > balances.size) {
            for (index in balances.size until balancesView.childCount) {
                balancesView.removeViewAt(index)
            }
        } else {
            for (index in balancesView.size until balances.size) {
                balancesView.addView(TextView(context).apply { setStyleCompat(R.style.WalletCardBalanceValueView) }, index)
            }
        }
        for (index in 0 until balances.size) {
            val balance = balances[index]
            @SuppressLint("SetTextI18n")
            (balancesView.getChildAt(index) as TextView).text = "${"%.2f".format(balance.amount)} ${balance.code}"
        }
    }

    private fun populateAvailabilities(minBalance: Double, balances: List<WalletBalanceEntity>) {
        if (availabilitiesView.childCount > balances.size) {
            for (index in balances.size until availabilitiesView.childCount) {
                availabilitiesView.removeViewAt(index)
            }
        } else {
            for (index in availabilitiesView.size until balances.size) {
                availabilitiesView.addView(TextView(context).apply { setStyleCompat(R.style.WalletCardAvailabilityValueView) }, index)
            }
        }
        for (index in 0 until balances.size) {
            val balance = balances[index]
            @SuppressLint("SetTextI18n")
            (availabilitiesView.getChildAt(index) as TextView).text = "${"%.2f".format(balance.availableAmount(minBalance))} ${balance.code}"
        }
    }
}