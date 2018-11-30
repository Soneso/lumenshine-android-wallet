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
            val dateFrom = dateFormat.parse(dateFromText.text.toString())
            var dateTo = dateFormat.parse(dateToText.text.toString())
            val calendar = Calendar.getInstance()
            calendar.time = dateTo
            calendar.add(Calendar.HOUR_OF_DAY, 23)
            calendar.add(Calendar.MINUTE, 59)
            calendar.add(Calendar.SECOND, 59)
            dateTo = calendar.time
            filterViewModel.updateFilters(walletSpinner.selectedItem as WalletEntity, dateFrom, dateTo)
            activity!!.supportFragmentManager.popBackStack()
        }

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

    private fun subscribeForLiveData() {
        filterViewModel.liveWallets.observe(this, Observer {
            when (it.state) {
                Resource.SUCCESS -> {
                    val arrayAdapter = ArrayAdapter<WalletEntity>(context!!, R.layout.support_simple_spinner_dropdown_item, emptyArray())
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    walletSpinner.adapter = arrayAdapter
                }
            }
        })

        filterViewModel.liveTransactionsFilter.observe(this, Observer {
            //TODO question in Trello
            for (i in 0 until walletSpinner.adapter.count) {
                if (walletSpinner.adapter.getItem(i) == it.wallet) {
                    walletSpinner.setSelection(i)
                }
            }
            updateDates(it.dateFrom, it.dateTo)
        })
    }

    private fun updateDates(dateFrom: Date, dateTo: Date) {
        dateFromText.text = dateFormat.format(dateFrom)
        dateToText.text = dateFormat.format(dateTo)

        val calendar = Calendar.getInstance()
        calendar.time = dateFrom

        dateFromDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val selectedDate: String = dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year
            dateFromText.text = selectedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dateFromDialog.datePicker.minDate = Date(dateTo.time - 14 * TransactionsFilter.DAY_IN_MS).time
        dateFromDialog.datePicker.maxDate = Date(dateTo.time - TransactionsFilter.DAY_IN_MS).time

        calendar.time = dateTo
        dateToDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val selectedDate: String = dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year
            dateToText.text = selectedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dateFromDialog.datePicker.minDate = Date(dateTo.time - 14 * TransactionsFilter.DAY_IN_MS).time
        dateFromDialog.datePicker.maxDate = Date(dateTo.time - TransactionsFilter.DAY_IN_MS).time
    }

    companion object {
        const val TAG = "TransactionsFilterFragment"

        fun newInstance() = TransactionsFilterFragment()
    }
}