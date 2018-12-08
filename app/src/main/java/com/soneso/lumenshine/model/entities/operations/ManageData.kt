package com.soneso.lumenshine.model.entities.operations

class ManageData(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val name: String,
        val value: String
) : Operation(id, Type.MANAGE_DATA, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    override fun getSortAmount(): Nothing? = null
    override fun getSortCurrency(): Nothing? = null
}
