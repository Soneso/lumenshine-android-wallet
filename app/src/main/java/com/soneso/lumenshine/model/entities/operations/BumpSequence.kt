package com.soneso.lumenshine.model.entities.operations

class BumpSequence(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val bumpTo: Long
) : Operation(id, Type.BUMP_SEQUENCE, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    override fun getSortAmount(): Nothing? = null
    override fun getSortCurrency(): Nothing? = null
}