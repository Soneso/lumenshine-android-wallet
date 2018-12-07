package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases

class PaymentsFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    fun updateReceivedFilter(filterReceived: Boolean, receivedFrom: Int?, receivedTo: Int?) {
        if (filterReceived) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterReceived = filterReceived
        transactionsUseCases.operationFilter.paymentsFilter.receivedFrom = receivedFrom
        transactionsUseCases.operationFilter.paymentsFilter.receivedTo = receivedTo
    }

    fun updateSentFilter(filterSent: Boolean, sentFrom: Int?, sentTo: Int?) {
        if (filterSent) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterSent = filterSent
        transactionsUseCases.operationFilter.paymentsFilter.sentFrom = sentFrom
        transactionsUseCases.operationFilter.paymentsFilter.sentTo = sentTo
    }

    fun updateCurrencyFiler(filterCurrency: Boolean, currency: String?) {
        if (filterCurrency) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterCurrency = filterCurrency
        transactionsUseCases.operationFilter.paymentsFilter.currency = currency
    }

    fun getPaymentFilterData() = transactionsUseCases.operationFilter.paymentsFilter
}
