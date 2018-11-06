package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.model.TransactionRepository
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.Flowable
import java.util.*
import javax.inject.Inject

class TransactionsUseCases @Inject constructor(
        private val transactionRepo: TransactionRepository
) {

    //TODO Load and select from the available Wallet List
    private val selectedWallet: String = "GBN7KBDHWJ2MT4EDQZY3MGINJ4G6TOMZS5WZYNJJLZACRASDGEIBFVPY"
    private val startDate: Date = Date(System.currentTimeMillis() - 7 * DAY_IN_MS)
    private val endDate: Date = Date()

    fun getOperations(): Flowable<Resource<List<Operation>, ServerException>> {
        return transactionRepo.loadOperations(selectedWallet, startDate, endDate)
    }

    companion object {
        private const val DAY_IN_MS = 1000 * 60 * 60 * 24
    }
}
