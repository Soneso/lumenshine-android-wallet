package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.operations.Operation

class OtherFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    fun updateOtherTypesFilter(otherTypesFilter: HashSet<Operation.Type>) {
        if (otherTypesFilter.size > 0) transactionsUseCases.operationFilter.othersFilter.active = true
        transactionsUseCases.operationFilter.othersFilter.otherOperations = otherTypesFilter
    }

    fun getOtherTypesFilter(): Set<Operation.Type> =
            transactionsUseCases.operationFilter.othersFilter.otherOperations
}