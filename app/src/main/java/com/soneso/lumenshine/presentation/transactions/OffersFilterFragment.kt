package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsFragment
import kotlinx.android.synthetic.main.operation_offer_filter.*

class OffersFilterFragment : LsFragment() {

    private lateinit var offersFilterViewModel: OffersFilterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offersFilterViewModel = ViewModelProviders.of(this, viewModelFactory)[OffersFilterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.operation_offer_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sellingCurrencySwitch.setOnCheckedChangeListener { _, isChecked ->
            sellingCurrencyText.isEnabled = isChecked
            if (!isChecked) sellingCurrencyText.text.clear()
        }

        buyingCurrencySwitch.setOnCheckedChangeListener { _, isChecked ->
            buyingCurrencyText.isEnabled = isChecked
            if (!isChecked) sellingCurrencyText.text.clear()
        }

        clearOffersFilterButton.setOnClickListener { clearAllFilters() }
    }

    private fun clearAllFilters() {
        sellingCurrencySwitch.isChecked = false
        buyingCurrencySwitch.isChecked = false
    }

    companion object {
        const val TAG = "OffersFilterFragment"

        fun newInstance() = OffersFilterFragment()
    }
}