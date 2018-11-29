package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        clearPaymentFilterButton.setOnClickListener { clearAllFilters() }
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
