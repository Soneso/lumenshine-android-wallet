package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsFragment
import kotlinx.android.synthetic.main.operation_payment_filter.*

class PaymentsFilterFragment : LsFragment() {

    private lateinit var paymentsFilterViewModel: PaymentsFilterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentsFilterViewModel = ViewModelProviders.of(this, viewModelFactory)[PaymentsFilterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.operation_payment_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeForLiveData()
        initiateSwitches()
        initiateFilterData()

        clearPaymentFilterButton.setOnClickListener { clearAllFilters() }
    }

    private fun subscribeForLiveData() {
        paymentsFilterViewModel.liveTransactionsFilter.observe(this, Observer {
            paymentsFilterViewModel.loadWalletDetails(it.wallet)
        })

        paymentsFilterViewModel.liveWalletCurrencies.observe(this, Observer {
            val arrayAdapter = ArrayAdapter<String>(context!!, R.layout.currency_spinner_item)
            arrayAdapter.add("All")
            arrayAdapter.addAll(it)
            arrayAdapter.setDropDownViewResource(R.layout.currency_spinner_item)
            currencySpinner.adapter = arrayAdapter
            currencySpinner.setSelection(0)
            if (paymentsFilterViewModel.getPaymentFilterData().currency != null) {
                for (i in 0 until currencySpinner.adapter.count) {
                    if (currencySpinner.adapter.getItem(i) == paymentsFilterViewModel.getPaymentFilterData().currency) {
                        currencySpinner.setSelection(i)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        paymentsFilterViewModel.updateReceivedFilter(
                receivedSwitch.isChecked,
                if (receivedFromText.text.isEmpty()) null else receivedFromText.text.toString().toInt(),
                if (receivedToText.text.isEmpty()) null else receivedToText.text.toString().toInt()
        )

        paymentsFilterViewModel.updateSentFilter(
                sentSwitch.isChecked,
                if (amountFromText.text.isEmpty()) null else amountFromText.text.toString().toInt(),
                if (amountToText.text.isEmpty()) null else amountToText.text.toString().toInt()
        )

        currencySpinner.selectedItem
        paymentsFilterViewModel.updateCurrencyFiler(
                currencySwitch.isChecked,
                if (!currencySwitch.isChecked || currencySpinner.selectedItem == "All") null else currencySpinner.selectedItem as String
        )

        super.onDestroyView()
    }

    private fun initiateFilterData() {
        val paymentFilterData = paymentsFilterViewModel.getPaymentFilterData()

        receivedSwitch.isChecked = paymentFilterData.filterReceived
        if (paymentFilterData.receivedFrom != null) receivedFromText.setText(paymentFilterData.receivedFrom!!.toString())
        if (paymentFilterData.receivedTo != null) receivedToText.setText(paymentFilterData.receivedTo!!.toString())
        sentSwitch.isChecked = paymentFilterData.filterSent
        if (paymentFilterData.sentFrom != null) amountFromText.setText(paymentFilterData.sentFrom!!.toString())
        if (paymentFilterData.sentTo != null) amountToText.setText(paymentFilterData.sentTo!!.toString())
        currencySwitch.isChecked = paymentFilterData.filterCurrency
        currencySpinner.isEnabled = paymentFilterData.filterCurrency
        currencyArrow.isEnabled = currencySpinner.isEnabled
    }

    private fun initiateSwitches() {
        receivedSwitch.setOnCheckedChangeListener { _, isChecked ->
            receivedFromText.isEnabled = isChecked
            receivedToText.isEnabled = isChecked
            if (!isChecked) {
                receivedFromText.text.clear()
                receivedToText.text.clear()
            }
        }

        sentSwitch.setOnCheckedChangeListener { _, isChecked ->
            amountFromText.isEnabled = isChecked
            amountToText.isEnabled = isChecked
            if (!isChecked) {
                amountFromText.text.clear()
                amountToText.text.clear()
            }
        }

        currencySwitch.setOnCheckedChangeListener { _, isChecked ->
            currencySpinner.isEnabled = isChecked
            currencyArrow.isEnabled = currencySpinner.isEnabled
            if (!isChecked) currencySpinner.setSelection(0)
        }
    }

    private fun clearAllFilters() {
        receivedSwitch.isChecked = false
        sentSwitch.isChecked = false
        currencySwitch.isChecked = false
    }


    companion object {
        const val TAG = "PaymentsFilterFragment"

        fun newInstance() = PaymentsFilterFragment()
    }
}
