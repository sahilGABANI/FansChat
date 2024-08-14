package base.ui.fragment.notifications.viewmodel

import base.data.model.GetNotificationRequest
import base.data.model.NotificationsData
import base.data.network.NetworkException
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.base.BaseViewModel
import base.ui.fragment.notifications.NotificationCacheRepository
import base.ui.fragment.notifications.NotificationRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class NotificationViewModel(private val notificationCacheRepository: NotificationCacheRepository,
    private val notificationRepository: NotificationRepository) : BaseViewModel() {

    private var PER_PAGE = 20

    private val notificationStateSubject: PublishSubject<NotificationViewState> = PublishSubject.create()
    val notificationState: Observable<NotificationViewState> = notificationStateSubject.hide()

    var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false

    //Local list to clear for smooth loading
    val compositeDisposableTemp: CompositeDisposable = CompositeDisposable()

    fun getNotificationsFromCache(isInitialLoad: Boolean = false) {
        notificationCacheRepository.getNotifications().subscribeOnIoAndObserveOnMainThread({
            it.let { posts ->
                notificationStateSubject.onNext(NotificationViewState.ListNotifications(posts))
                if (isInitialLoad) resetLoading()
            }
        }, {
            Timber.e(it)
            notificationStateSubject.onNext(NotificationViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
            if (isInitialLoad) resetLoading()
        }).autoDisposeTemp()
    }

    private fun getNotificationsFromRemote(isInitialLoad: Boolean = false) {
        notificationRepository.getNotifications(GetNotificationRequest(pageNumber, PER_PAGE)).doOnSubscribe {
            notificationStateSubject.onNext(NotificationViewState.LoadingState(true, pageNumber > 1))
        }.doAfterTerminate {
            notificationStateSubject.onNext(NotificationViewState.LoadingState(false))
            isLoading = false
        }.subscribeOnIoAndObserveOnMainThread({
            notificationStateSubject.onNext(NotificationViewState.LoadingState(false))

            isLoading = false
            if (it?.listOfNotifications?.size ?: 0 < PER_PAGE) {
                isLoadMore = false
            }

            it?.let { notifications ->
                notificationStateSubject.onNext(NotificationViewState.ClearOrAddObservables(true))
                if (isInitialLoad) {
                    notificationCacheRepository.deleteAll()
                }
                notificationCacheRepository.addNotifications(notifications.listOfNotifications ?: listOf())
                notificationStateSubject.onNext(NotificationViewState.ClearOrAddObservables(false))
            }
        }, {
            Timber.e(it)
            notificationStateSubject.onNext(NotificationViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun loadMore() {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                getNotificationsFromRemote()
            }
        }
    }

    fun resetLoading() {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
        getNotificationsFromRemote(true)
    }

    override fun onCleared() {
        compositeDisposableTemp.clear()
        super.onCleared()
    }

    private fun Disposable.autoDisposeTemp() {
        compositeDisposableTemp.add(this)
    }
}

sealed class NotificationViewState {
    data class ListNotifications(val listNotifications: List<NotificationsData>) : NotificationViewState()
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : NotificationViewState()
    data class LoadingState(val isLoading: Boolean, val isForceShow: Boolean = false) : NotificationViewState()
    data class ClearOrAddObservables(val isToClear: Boolean) : NotificationViewState()
}