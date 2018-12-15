package com.soneso.lumenshine.presentation.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.presentation.util.getDate
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_transactions_filter.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFilterFragment : LsFragment() {

    private lateinit var filterViewModel: TransactionsFilterViewModel

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private lateinit var dateFromDialog: DatePickerDialog
    private lateinit var dateToDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filterViewModel = ViewModelProviders.of(this, viewModelFactory)[TransactionsFilterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_transactions_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeForLiveData()

        dateFromView.setOnClickListener {
            dateFromDialog.show()
        }

        dateToView.setOnClickListener {
            dateToDialog.show()
        }

        applyFilterButton.setOnClickListener {
            filterViewModel.updateFilters(walletSpinner.selectedItem as WalletEntity, dateFromDialog.datePicker.getDate(), dateToDialog.datePicker.getDate())
            filterViewModel.updateOperationFilter(memoText.text.toString(), paymentsSwitch.isChecked, offersSwitch.isChecked, othersSwitch.isChecked)

            activity!!.supportFragmentManager.popBackStack()
        }

        initiateOperationFilterData()
        initiateFilterContainers()
        initiateSwitches()

        clearAllFilterButton.setOnClickListener {
            filterViewModel.resetOperationFilter()
            initiateOperationFilterData()
            initiateTransactionFilterData(filterViewModel.initialTransactionFilterState)
        }

    }

    private fun subscribeForLiveData() {
        filterViewModel.liveWallets.observe(this, Observer {
            when (it.state) {
                Resource.SUCCESS -> {
                    val arrayAdapter = ArrayAdapter<WalletEntity>(context!!, R.layout.support_simple_spinner_dropdown_item, it.success())
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    walletSpinner.adapter = arrayAdapter
                }
            }
        })

        filterViewModel.liveTransactionsFilter.observe(this, Observer {
            initiateTransactionFilterData(it)
            filterViewModel.initialTransactionFilterState = it
        })
    }

    private fun initiateTransactionFilterData(it: TransactionsFilter) {
        for (i in 0 until walletSpinner.adapter.count) {
            if (walletSpinner.adapter.getItem(i) == it.wallet) {
                walletSpinner.setSelection(i)
            }
        }
        updateDates(it.dateFrom, it.dateTo)
    }

    private fun initiateOperationFilterData() {
        val operationFilter = filterViewModel.getOperationFilter()
        memoText.setText(operationFilter.memo)

        //#Zica change later
        paymentsSwitch.postDelayed({
            paymentsSwitch.isChecked = operationFilter.paymentsFilter.active
            paymentsSwitch.isSelected = !operationFilter.paymentsFilter.partialFilter()
        }, 200)
        offersSwitch.postDelayed({
            offersSwitch.isChecked = operationFilter.offersFilter.active
            offersSwitch.isSelected = !operationFilter.offersFilter.partialFilter()
        }, 200)
        othersSwitch.postDelayed({
            othersSwitch.isChecked = operationFilter.othersFilter.active
            othersSwitch.isSelected = !operationFilter.othersFilter.partialFilter()
        }, 200)
    }

    private fun initiateSwitches() {
        paymentsSwitch.setOnCheckedChangeListener { _, isChecked ->
            paymentsSwitch.isSelected = true
            filterViewModel.updatePaymentOperationFilter(isChecked)
        }

        offersSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterViewModel.updateOfferOperationFilter(isChecked)
        }

        othersSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterViewModel.updateOtherOperationFilter(isChecked)
        }
    }

    private fun initiateFilterContainers() {
        paymentContainer.setOnClickListener {
            activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, PaymentsFilterFragment.newInstance(), PaymentsFilterFragment.TAG)
                    .addToBackStack(PaymentsFilterFragment.TAG)
                    .commit()
        }

        offersContainer.setOnClickListener {
            activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, OffersFilterFragment.newInstance(), OffersFilterFragment.TAG)
                    .addToBackStack(OffersFilterFragment.TAG)
                    .commit()
        }

        otherContainer.setOnClickListener {
            activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, OtherFilterFragment.newInstance(), OtherFilterFragment.TAG)
                    .addToBackStack(OtherFilterFragment.TAG)
                    .commit()
        }
    }

    private fun updateDates(dateFrom: Date, dateTo: Date) {
        dateFromText.text = dateFormat.format(dateFrom)
        dateToText.text = dateFormat.format(dateTo)

        val calendar = Calendar.getInstance()
        calendar.time = dateFrom

        dateFromDialog = DatePickerDialog(context!!, R.style.AppTheme_LsDatePicker, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate: String = dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year
            dateFromText.text = selectedDate

            dateToDialog.datePicker.minDate = dateFromDialog.datePicker.getDate().time + TransactionsFilter.DAY_IN_MS
            dateToDialog.datePicker.maxDate = dateFromDialog.datePicker.getDate().time + (14 * TransactionsFilter.DAY_IN_MS)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dateFromDialog.datePicker.minDate = dateTo.time - 14 * TransactionsFilter.DAY_IN_MS
        dateFromDialog.datePicker.maxDate = dateTo.time - TransactionsFilter.DAY_IN_MS

        calendar.time = dateTo
        dateToDialog = DatePickerDialog(context!!, R.style.AppTheme_LsDatePicker, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate: String = dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year
            dateToText.text = selectedDate


            dateFromDialog.datePicker.minDate = dateToDialog.datePicker.getDate().time - (14 * TransactionsFilter.DAY_IN_MS)
            dateFromDialog.datePicker.maxDate = dateToDialog.datePicker.getDate().time - TransactionsFilter.DAY_IN_MS
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dateToDialog.datePicker.minDate = dateFrom.time + TransactionsFilter.DAY_IN_MS
        dateToDialog.datePicker.maxDate = dateFrom.time + 14 * TransactionsFilter.DAY_IN_MS
    }

    companion object {
        const val TAG = "TransactionsFilterFragment"

        fun newInstance() = TransactionsFilterFragment()
    }
}