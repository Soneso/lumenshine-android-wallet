package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.data.OperationsSort
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.domain.util.SortType

class TransactionsSortViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    fun getOperationSort() = transactionsUseCases.operationsSort

    fun updateOperationSort(
            sortByDate: Boolean,
            dateSortType: SortType,
            sortByType: Boolean,
            typeSortType: SortType,
            sortByAmount: Boolean,
            amountSortType: SortType,
            sortByCurrency: Boolean,
            currencySortType: SortType
    ) {
        val operationsSort = OperationsSort(
                sortByDate, dateSortType, sortByType, typeSortType,
                sortByAmount, amountSortType, sortByCurrency, currencySortType)
        transactionsUseCases.operationsSort = operationsSort
    }
}