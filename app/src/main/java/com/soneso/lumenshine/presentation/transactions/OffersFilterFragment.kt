package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
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

        subscribeForLiveData()
        initiateFilterData()
        initiateViews()

        clearOffersFilterButton.setOnClickListener { clearAllFilters() }
    }

    private fun subscribeForLiveData() {
        offersFilterViewModel.liveTransactionsFilter.observe(this, Observer {
            offersFilterViewModel.loadWalletDetails(it.wallet)
        })

        offersFilterViewModel.liveWalletCurrencies.observe(this, Observer {
            val arrayAdapter = ArrayAdapter<String>(context!!, R.layout.currency_spinner_item)
            arrayAdapter.setDropDownViewResource(R.layout.currency_spinner_item)
            arrayAdapter.add("All")
            arrayAdapter.addAll(it)
            arrayAdapter.add("Other")
            currencySpinner.adapter = arrayAdapter
            currencySpinner.setSelection(0)

            if (offersFilterViewModel.getOfferFilterData().buyingCurrency != null) {
                for (i in 0 until currencySpinner.adapter.count) {
                    if (currencySpinner.adapter.getItem(i) == offersFilterViewModel.getOfferFilterData().buyingCurrency)
                        currencySpinner.setSelection(i)
                }
                if (currencySpinner.selectedItemPosition == 0) {
                    currencySpinner.visibility = View.GONE
                    currencyArrow.visibility = View.GONE
                } else {
                    buyingCurrencyText.text = null
                    buyingCurrencyText.visibility = View.GONE
                }

            } else {
                buyingCurrencyText.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        offersFilterViewModel.updateSellingCurrency(
                sellingCurrencySwitch.isChecked,
                if (sellingCurrencyText.text.isEmpty()) null else sellingCurrencyText.text.toString()
        )

        val selectedCurrency: String? =
                if (currencySpinner.isVisible) {
                    if (currencySpinner.selectedItem == "All") null else currencySpinner.selectedItem as String
                } else if (buyingCurrencyText.text.isEmpty()) null else buyingCurrencyText.text.toString()

        offersFilterViewModel.updateBuyingCurrency(buyingCurrencySwitch.isChecked, selectedCurrency)

        super.onDestroyView()
    }

    private fun initiateFilterData() {
        val offerFilterData = offersFilterViewModel.getOfferFilterData()

        sellingCurrencySwitch.isChecked = offerFilterData.filterSellingCurrency
        sellingCurrencyText.isEnabled = offerFilterData.filterSellingCurrency
        if (offerFilterData.sellingCurrency != null) sellingCurrencyText.setText(offerFilterData.sellingCurrency)
        buyingCurrencySwitch.isChecked = offerFilterData.filterBuyingCurrency
        buyingCurrencyText.isEnabled = offerFilterData.filterBuyingCurrency
        currencySpinner.isEnabled = offerFilterData.filterBuyingCurrency
        if (offerFilterData.buyingCurrency != null) buyingCurrencyText.setText(offerFilterData.buyingCurrency)
    }

    private fun initiateViews() {
        sellingCurrencySwitch.setOnCheckedChangeListener { _, isChecked ->
            sellingCurrencyText.isEnabled = isChecked
            if (!isChecked) sellingCurrencyText.text.clear()
        }

        buyingCurrencySwitch.setOnCheckedChangeListener { _, isChecked ->
            buyingCurrencyText.isEnabled = isChecked
            currencySpinner.isEnabled = isChecked
            if (!isChecked) {
                buyingCurrencyText.text.clear()
                buyingCurrencyText.visibility = View.GONE
                currencySpinner.visibility = View.VISIBLE
                currencyArrow.visibility = View.VISIBLE
                currencySpinner.setSelection(0)
            }
        }

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (currencySpinner.adapter.getItem(position) == "Other") {
                    currencySpinner.visibility = View.GONE
                    currencyArrow.visibility = View.GONE
                    buyingCurrencyText.visibility = View.VISIBLE
                }
            }
        }
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