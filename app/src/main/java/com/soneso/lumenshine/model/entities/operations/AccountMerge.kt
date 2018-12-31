package com.soneso.lumenshine.model.entities.operations

class AccountMerge(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val mergedAccount: String
) : Operation(id, Type.ACCOUNT_MERGE, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    override fun getSortAmount(): Nothing? = null
    override fun getSortCurrency(): Nothing? = null
}