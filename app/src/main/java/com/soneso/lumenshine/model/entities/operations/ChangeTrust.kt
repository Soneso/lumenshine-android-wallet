package com.soneso.lumenshine.model.entities.operations

class ChangeTrust(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val limit: String,
        val assetCode: String,
        val assetIssuer: String
) : Operation(id, Type.CHANGE_TRUST, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    fun isAddTrust(): Boolean = limit != "0"
}