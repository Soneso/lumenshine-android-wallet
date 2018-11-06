package com.soneso.lumenshine.model.entities.operations

abstract class Operation(
        val id: Long,
        val type: Type,
        val fee: String,
        val order: Int,
        val date: String,
        val transactionHash: String,
        val transactionMemoType: String,
        val transactionMemo: String
) {


    enum class Type(val value: Int) {
        CREATE_ACCOUNT(0),
        PAYMENT(1),
        OFFER(2);
    }
}