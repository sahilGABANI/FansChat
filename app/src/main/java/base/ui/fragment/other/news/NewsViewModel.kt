package base.ui.fragment.other.news

import base.application.FansChat
import base.data.api.news.NewsCacheRepository
import base.data.api.news.NewsPostRepository
import base.data.model.CommonFeedItem
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.socket.model.GetWallRequest
import base.ui.base.BaseViewModel
import base.util.Constants
import base.util.languageForTranslation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class NewsViewModel(
    private val newsPostRepository: NewsPostRepository,
    private val newsCacheRepository: NewsCacheRepository
) : BaseViewModel() {

    private var PER_PAGE = 20

    private val newsPostStateSubject: PublishSubject<NewsPostState> = PublishSubject.create()
    val newsPostState: Observable<NewsPostState> = newsPostStateSubject.hide()

    //Local list to clear for smooth loading
    val compositeDisposableTemp: CompositeDisposable = CompositeDisposable()

    private var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false

    fun getNewsFromCache(type: String, isInitialLoad: Boolean = false) {
        newsCacheRepository.getPostList(type).subscribeOnIoAndObserveOnMainThread({
            it.let { posts ->
                newsPostStateSubject.onNext(NewsPostState.ListOfNewsPost(posts))
                if (isInitialLoad) resetLoading(type)

            }
        }, {
            Timber.e(it)
            newsPostStateSubject.onNext(NewsPostState.ErrorMessage(it.localizedMessage ?: ""))
            if (isInitialLoad) resetLoading(type)
        }).autoDisposeTemp()

    }

    private fun getNewsFromRemote(type: String, isInitialLoad: Boolean = false) {
        when (type) {
            Constants.POST_TYPE_NEWS -> newsPostRepository.getNewsPostList(
                GetWallRequest(
                    pageNumber,
                    PER_PAGE,
                    lang = FansChat.context.languageForTranslation
                )
            )
            Constants.POST_TYPE_SOCIAL -> newsPostRepository.getSocialPostList(
                GetWallRequest(
                    pageNumber,
                    PER_PAGE,
                    lang = FansChat.context.languageForTranslation
                )
            )
            Constants.POST_TYPE_CLUB_TV -> newsPostRepository.getClubTvPostList(
                GetWallRequest(
                    pageNumber,
                    PER_PAGE,
                    lang = FansChat.context.languageForTranslation
                )
            )
            else -> newsPostRepository.getRumourPostList(
                GetWallRequest(
                    pageNumber,
                    PER_PAGE,
                    lang = FansChat.context.languageForTranslation
                )
            )
        }.doOnSubscribe {
            newsPostStateSubject.onNext(NewsPostState.LoadingState(true, pageNumber > 1))
        }.doAfterTerminate {
            newsPostStateSubject.onNext(NewsPostState.LoadingState(false))
            isLoading = false
        }.subscribeOnIoAndObserveOnMainThread({
            newsPostStateSubject.onNext(NewsPostState.LoadingState(false))

            isLoading = false
            if (it.data?.size ?: 0 < PER_PAGE) {
                isLoadMore = false
            }

            it.data?.let { posts ->
                newsPostStateSubject.onNext(NewsPostState.ClearOrAddObservables(true))
                if (isInitialLoad) {
                    newsCacheRepository.deletePostsByType(type)
                }
                newsCacheRepository.addPostList(posts)
                newsPostStateSubject.onNext(NewsPostState.ClearOrAddObservables(false))
            }
        }, {
            Timber.e(it)
            newsPostStateSubject.onNext(NewsPostState.ErrorMessage(it.localizedMessage ?: ""))
        }).autoDispose()
    }

    fun loadMore(type: String) {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                getNewsFromRemote(type)
            }
        }
    }

    fun resetLoading(type: String) {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
        getNewsFromRemote(type, true)
    }

    override fun onCleared() {
        compositeDisposableTemp.clear()
        super.onCleared()
    }

    private fun Disposable.autoDisposeTemp() {
        compositeDisposableTemp.add(this)
    }
}

sealed class NewsPostState {
    data class ErrorMessage(val errorMessage: String) : NewsPostState()
    data class ListOfNewsPost(val wallFeed: List<CommonFeedItem>) : NewsPostState()
    data class LoadingState(val isLoading: Boolean, val isForceShow: Boolean = false) :
        NewsPostState()

    data class ClearOrAddObservables(val isToClear: Boolean) : NewsPostState()
}