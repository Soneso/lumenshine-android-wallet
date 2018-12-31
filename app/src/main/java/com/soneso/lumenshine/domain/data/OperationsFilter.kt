package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.model.entities.operations.Operation

class OperationsFilter {
    var memo: String = ""
    var paymentsFilter: PaymentsFilter = PaymentsFilter()
    var offersFilter: OffersFilter = OffersFilter()
    var othersFilter: OthersFilter = OthersFilter()

    fun isActive(): Boolean = paymentsFilter.active || offersFilter.active || othersFilter.active || memo != ""

    fun getOperationTypes(): Set<Operation.Type> {
        val operationTypes: HashSet<Operation.Type> = HashSet()

        if (paymentsFilter.active) operationTypes.addAll(getPaymentOperationTypes())
        if (offersFilter.active) operationTypes.addAll(getOfferOperationTypes())
        if (othersFilter.active) {
            if (othersFilter.otherOperations.isEmpty())
                operationTypes.addAll(getOthersOperationTypes())
            else
                operationTypes.addAll(othersFilter.otherOperations)
        }

        return if (!operationTypes.isEmpty()) operationTypes else Operation.Type.values().toHashSet()
    }

    fun resetFilter() {
        memo = ""
        paymentsFilter = PaymentsFilter()
        offersFilter = OffersFilter()
        othersFilter = OthersFilter()
    }
}

class PaymentsFilter {
    var active: Boolean = true
    var filterReceived: Boolean = false
    var receivedFrom: Int? = null
    var receivedTo: Int? = null
    var filterSent: Boolean = false
    var sentFrom: Int? = null
    var sentTo: Int? = null
    var filterCurrency: Boolean = false
    var currency: String? = null

    fun minimumOneSelected() = filterReceived || filterSent || filterCurrency

    fun partialFilter(): Boolean {
        val minOneNotActive = !(filterReceived && filterSent && filterCurrency)
        return minimumOneSelected() && minOneNotActive
    }
}

class OffersFilter {
    var active: Boolean = true
    var filterSellingCurrency: Boolean = false
    var sellingCurrency: String? = null
    var filterBuyingCurrency: Boolean = false
    var buyingCurrency: String? = null

    fun partialFilter(): Boolean {
        val minOneActive = filterSellingCurrency || filterBuyingCurrency
        val minOneNotActive = !(filterSellingCurrency && filterBuyingCurrency)
        return minOneActive && minOneNotActive
    }
}

class OthersFilter {
    var active: Boolean = true
    var otherOperations: HashSet<Operation.Type> = HashSet()

    fun partialFilter(): Boolean = !otherOperations.isEmpty() && otherOperations.size < getOthersOperationTypes().size
}

private fun getPaymentOperationTypes(): HashSet<Operation.Type> {
    val paymentOperations: HashSet<Operation.Type> = HashSet()
    paymentOperations.add(Operation.Type.CREATE_ACCOUNT)
    paymentOperations.add(Operation.Type.PAYMENT)
    paymentOperations.add(Operation.Type.PATH_PAYMENT)
    return paymentOperations
}

private fun getOfferOperationTypes(): HashSet<Operation.Type> {
    val offerOperations: HashSet<Operation.Type> = HashSet()
    offerOperations.add(Operation.Type.OFFER)
    offerOperations.add(Operation.Type.PASSIVE_OFFER)
    return offerOperations
}

private fun getOthersOperationTypes(): HashSet<Operation.Type> {
    val allOperations = Operation.Type.values().toHashSet()
    allOperations.removeAll(getPaymentOperationTypes())
    allOperations.removeAll(getOfferOperationTypes())
//    allOperations.remove(Operation.Type.)
    return allOperations
}