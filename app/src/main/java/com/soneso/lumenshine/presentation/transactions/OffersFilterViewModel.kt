package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases

class OffersFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    fun updateSellingCurrency(filterSellingCurrency: Boolean, sellingCurrency: String?) {
        if (filterSellingCurrency) transactionsUseCases.operationFilter.offersFilter.active
        transactionsUseCases.operationFilter.offersFilter.filterSellingCurrency = filterSellingCurrency
        transactionsUseCases.operationFilter.offersFilter.sellingCurrency = sellingCurrency
    }

    fun updateBuyingCurrency(filterBuyingCurrency: Boolean, buyingCurrency: String?) {
        if (filterBuyingCurrency) transactionsUseCases.operationFilter.offersFilter.active
        transactionsUseCases.operationFilter.offersFilter.filterBuyingCurrency = filterBuyingCurrency
        transactionsUseCases.operationFilter.offersFilter.buyingCurrency = buyingCurrency
    }

    fun getOfferFilterData() = transactionsUseCases.operationFilter.offersFilter
}