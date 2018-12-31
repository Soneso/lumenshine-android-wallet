package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.domain.util.SortType

class OperationsSort(
        val sortByDate: Boolean = true,
        val dateSortType: SortType = SortType.ASC,
        val sortByType: Boolean = false,
        val typeSortType: SortType = SortType.ASC,
        val sortByAmount: Boolean = false,
        val amountSortType: SortType = SortType.ASC,
        val sortByCurrency: Boolean = false,
        val currencySortType: SortType = SortType.ASC
)