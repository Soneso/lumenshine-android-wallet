package com.soneso.lumenshine.model.entities.operations

class Payment(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val destination: String,
        val amount: String,
        val currency: String,
        var isReceived: Boolean
) : Operation(id, Type.PAYMENT, fee, order, date, transactionHash, transactionMemoType, transactionMemo)