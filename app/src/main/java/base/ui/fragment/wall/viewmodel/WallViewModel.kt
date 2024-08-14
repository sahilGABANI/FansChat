package base.ui.fragment.wall.viewmodel

import base.application.FansChat
import base.data.api.friendrequest.FriendRequestRepository
import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.wall.WallCacheRepository
import base.data.api.wall.WallPostsRepository
import base.data.api.wall.model.WallPostRequest
import base.data.model.wall.ContentItem
import base.data.model.wall.FeedSetupItem
import base.data.model.wall.TickerData
import base.data.network.NetworkException
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.base.BaseViewModel
import base.util.Constants
import base.util.languageForTranslation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class WallViewModel(
    private val wallPostRepository: WallPostsRepository,
    private val wallCacheRepository: WallCacheRepository,
    private val friendRequestRepository: FriendRequestRepository
) : BaseViewModel() {
    private val wallPostStateSubject: PublishSubject<WallPostState> = PublishSubject.create()
    val wallPostState: Observable<WallPostState> = wallPostStateSubject.hide()

    //Local list to clear for smooth loading of wall
    val compositeDisposableTemp: CompositeDisposable = CompositeDisposable()

    init {
        Constants.SHOW_REFRESH_INDICATOR = false
        getWallFromCache()
        getWallFromRemote()
    }

    fun pendingFriendsRequestCount() {
        friendRequestRepository.pendingFriendRequestCount(GetFriendRequestRequest(20, 1))
            .subscribeOnIoAndObserveOnMainThread({
                wallPostStateSubject.onNext(WallPostState.DisplayFriendsBadgeCount(it > 0))
            }, {
                wallPostStateSubject.onNext(WallPostState.DisplayFriendsBadgeCount(false))
            }).autoDispose()
    }

    fun getWallFromCache() {
        wallCacheRepository.getTicker().subscribeOnIoAndObserveOnMainThread({
            it.let { posts ->
                wallPostStateSubject.onNext(WallPostState.ListTicker(posts))
            }
        }, {
            Timber.e(it)
            wallPostStateSubject.onNext(
                WallPostState.ErrorMessage(
                    it.localizedMessage ?: "",
                    if (it is NetworkException) (it.exception as HttpException).code() else 0
                )
            )
        }).autoDisposeTemp()

        wallCacheRepository.getModuleList().subscribeOnIoAndObserveOnMainThread({
            it.let { posts ->
                wallPostStateSubject.onNext(WallPostState.ListWall(posts))
            }
        }, {
            Timber.e(it)
            wallPostStateSubject.onNext(
                WallPostState.ErrorMessage(
                    it.localizedMessage ?: "",
                    if (it is NetworkException) (it.exception as HttpException).code() else 0
                )
            )
        }).autoDisposeTemp()
    }

    fun getWallFromRemote() {
        wallPostRepository.getNewWall(FansChat.context.languageForTranslation).doOnSubscribe {
            wallPostStateSubject.onNext(WallPostState.LoadingState(true))
        }.doAfterTerminate {
            wallPostStateSubject.onNext(WallPostState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            wallPostStateSubject.onNext(WallPostState.LoadingState(false))
            it?.let { wall ->
                Constants.SHOW_REFRESH_INDICATOR = false
                val listTicker = arrayListOf<TickerData>()
                wallPostStateSubject.onNext(WallPostState.ClearOrAddObservables(true))
                wallCacheRepository.addTicker(wall.ticker.let {
                    it.forEach { strTicker -> listTicker.add(TickerData(strTicker)) }
                    listTicker
                })
                wallCacheRepository.addModuleList(wall.feedSetup)
                wallPostStateSubject.onNext(WallPostState.ClearOrAddObservables(false))
            }
        }, {
            Timber.e(it)
            wallPostStateSubject.onNext(
                WallPostState.ErrorMessage(
                    it.localizedMessage ?: "",
                    if (it is NetworkException) (it.exception as HttpException).code() else 0
                )
            )
        }).autoDispose()

    }

    fun getNextCarousels(
        dataSource: String,
        page: Int,
        perPage: Int,
        position: Int,
        moduleType: String,
    ) {
        wallPostRepository.getNextCarousels(
            dataSource,
            page,
            perPage,
            FansChat.context.languageForTranslation
        )
            .subscribeOnIoAndObserveOnMainThread({
                it.let { contentList ->
                    wallCacheRepository.addContentList(contentList, moduleType)
                    wallPostStateSubject.onNext(
                        WallPostState.CarouselPagingResponse(
                            position,
                            contentList.size < perPage
                        )
                    )
                }
            }, {
                Timber.e(it)
            }).autoDispose()
    }

    fun createWallPost(wallPostRequest: WallPostRequest) {
        wallPostRepository.addWallPost(wallPostRequest).doOnSubscribe {
            wallPostStateSubject.onNext(WallPostState.LoadingState(true))
        }.doAfterTerminate {
            wallPostStateSubject.onNext(WallPostState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            it?.let { posts ->
                wallPostStateSubject.onNext(WallPostState.PostNewWall(it))
                wallCacheRepository.addContentList(listOf(posts), Constants.Type_Feed)
            }
        }, {
            Timber.e(it)
            wallPostStateSubject.onNext(
                WallPostState.ErrorMessage(
                    it.localizedMessage ?: "",
                    if (it is NetworkException) (it.exception as HttpException).code() else 0
                )
            )
        }).autoDispose()
    }

    override fun onCleared() {
        compositeDisposableTemp.clear()
        super.onCleared()
    }

    private fun Disposable.autoDisposeTemp() {
        compositeDisposableTemp.add(this)
    }
}


sealed class WallPostState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : WallPostState()
    data class PostNewWall(val postWall: ContentItem) : WallPostState()
    data class LoadingState(val isLoading: Boolean) : WallPostState()
    data class ListTicker(val listTicker: List<TickerData>) : WallPostState()
    data class ListWall(val list: List<FeedSetupItem>) : WallPostState()
    data class ClearOrAddObservables(val isToClear: Boolean) : WallPostState()

    //To check page is last or it will have next pages after pagination
    data class CarouselPagingResponse(val pos: Int, val isLastPage: Boolean) : WallPostState()

    data class DisplayFriendsBadgeCount(val showBadge: Boolean) : WallPostState()
}