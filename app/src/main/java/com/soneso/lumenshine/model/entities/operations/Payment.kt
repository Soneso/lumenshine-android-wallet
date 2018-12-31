package com.soneso.lumenshine.model.entities.operations

class Payment(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        fromSelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val source: String,
        val destination: String,
        val amount: String,
        val currency: String,
        var isReceived: Boolean
) : Operation(id, Type.PAYMENT, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, fromSelectedWallet) {

    override fun getSortAmount() = amount.toDouble()
    override fun getSortCurrency() = currency
}