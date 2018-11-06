package com.soneso.lumenshine.di

import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.domain.usecases.WalletsUseCase
import dagger.Component
import javax.inject.Singleton

/**
 * Dagger component.
 * Created by cristi.paval on 3/9/18.
 */
@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    val walletsUseCase: WalletsUseCase

    val userUseCases: UserUseCases

    val transactionsUseCase: TransactionsUseCases
}