package com.soneso.lumenshine.domain.data

data class OperationsFilter(
        val paymentsFilter: PaymentsFilter
) {

    inner class PaymentsFilter(
            val active: Boolean
    )
}