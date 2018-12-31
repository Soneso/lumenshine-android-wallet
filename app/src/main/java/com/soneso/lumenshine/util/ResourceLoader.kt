package com.soneso.lumenshine.util

import com.soneso.lumenshine.networking.NetworkStateObserver
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

fun <ResponseType> Single<Response<ResponseType>>.asHttpResourceLoader(networkStateObserver: NetworkStateObserver, retryCount: Int = 3): Flowable<Resource<ResponseType, ServerException>> {

    val retryMechanism = Function<Flowable<Throwable>, Flowable<Boolean>> { networkThrowable ->
        val counter = AtomicInteger()
        networkThrowable
                .takeWhile { counter.getAndIncrement() < retryCount }
                .doOnNext { throwable ->
                    Timber.d("Trying number: ${counter.get()}")
                    if (counter.get() == 1) {
                        Timber.e(throwable)
                    }
                    if (!throwable.isWorthRetry()) {
                        throw throwable
                    }
                }
                .flatMap { networkStateObserver.observeApiAccess() }
                .doOnNext { Timber.d("--- Api accessible: $it") }
                .filter { it }
    }

    return Flowable.create<Resource<ResponseType, ServerException>>({ emitter ->

        emitter.onNext(Resource(Resource.LOADING))
        val disposable = this.retryWhen(retryMechanism)
                .subscribe({
                    if (it.isSuccessful && it.body() != null) {
                        Timber.d("_______Response is success!")
                        emitter.onNext(Success(it.body()!!))
                    } else {
                        Timber.d("_______Response is errorBody!")
                        emitter.onNext(Failure(ServerException(it.errorBody())))
                    }
                }, {
                    emitter.onNext(Failure(ServerException(it)))
                })

        emitter.setCancellable {
            disposable.dispose()
        }
    }, BackpressureStrategy.LATEST)
}

private fun Throwable.isWorthRetry(): Boolean {

    return when (this) {
        is IOException -> true
        else -> false
    }
}

fun <SuccessType, FailureType> Flowable<Resource<SuccessType, FailureType>>.refreshWith(
        refresher: Flowable<Resource<SuccessType, FailureType>>,
        cacheFunction: ((SuccessType) -> Unit)? = null
): Flowable<Resource<SuccessType, FailureType>> {

    return Flowable.create({ emitter ->

        var state = Resource.LOADING

        val thisD = this.subscribe {
            Timber.d("Emitting result from initial source...")
            val resource = if (it.state == Resource.SUCCESS) {
                Resource(state, it.success())
            } else {
                it
            }
            emitter.onNext(resource)
        }
        val refresherD = refresher
                .subscribe({
                    Timber.d("Emitting result from refresher...")
                    state = it.state
                    if (it.state == Resource.SUCCESS && cacheFunction != null) {
                        cacheFunction.invoke(it.success())
                    } else {
                        emitter.onNext(it)
                    }
                }, {
                    Timber.d("Emitting error...")
                    emitter.onError(it)
                })

        emitter.setCancellable {
            Timber.d("Disposing...")
            thisD.dispose()
            refresherD.dispose()
        }
    }, BackpressureStrategy.LATEST)
}