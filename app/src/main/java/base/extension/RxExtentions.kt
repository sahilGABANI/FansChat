package base.extension

import android.view.View
import base.data.network.model.ErrorResult
import base.data.network.model.FansChatError
import base.data.network.parseRetrofitException
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun View.throttleClicks(): Observable<Unit> {
    return clicks().throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.subscribeAndObserveOnMainThread(onNext: (t: T) -> Unit): Disposable {
    return observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext)
}

fun <T> Observable<T>.subscribeOnIoAndObserveOnMainThread(
    onNext: (t: T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun <T> Single<T>.subscribeOnIoAndObserveOnMainThread(
    onNext: (t: T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun <T> Single<T>.subscribeWithErrorPars(
    onNext: (t: T) -> Unit,
    onError: (ErrorResult) -> Unit,
): Disposable {
    return subscribe(onNext) {
        it.parseRetrofitException<FansChatError>()?.let { errorResponse ->
            onError(
                ErrorResult.ErrorMessage(
                    errorResponse.param?.get(0)?.message ?: "",
                    fansChatError = errorResponse
                )
            )
        } ?: run {
            onError(ErrorResult.ErrorThrowable(it))
        }
    }
}

fun <T> Flowable<T>.subscribeOnIoAndObserveOnMainThread(
    onNext: (t: T) -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}

fun Completable.subscribeOnIoAndObserveOnMainThread(
    onComplete: () -> Unit,
    onError: (Throwable) -> Unit
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onComplete, onError)
}

fun <T> ObservableEmitter<T>.onSafeNext(t: T) {
    if (!isDisposed) onNext(t)
}