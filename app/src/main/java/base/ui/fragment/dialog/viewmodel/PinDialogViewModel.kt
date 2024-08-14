package base.ui.fragment.dialog.viewmodel

import base.application.FansChat
import base.data.api.wall.WallPostsRepository
import base.data.model.CommonFeedItem
import base.data.network.NetworkException
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.base.BaseViewModel
import base.util.Constants
import base.util.languageForTranslation
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class PinDialogViewModel(private val wallPostsRepository: WallPostsRepository) : BaseViewModel() {

    private val feedPostStateSubject: PublishSubject<PinDialogViewState> = PublishSubject.create()
    val feedPostState: Observable<PinDialogViewState> = feedPostStateSubject.hide()

    fun getPostDetail(postId: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.getNewsPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.getSocialPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.getRumoursPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_CLUB_TV -> wallPostsRepository.getVideoPostData(postId, FansChat.context.languageForTranslation)
            else -> wallPostsRepository.getWallPostData(postId, FansChat.context.languageForTranslation)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(PinDialogViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(PinDialogViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(PinDialogViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(PinDialogViewState.WallPostDetails(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(PinDialogViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }
}

sealed class PinDialogViewState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : PinDialogViewState()
    data class WallPostDetails(val wallPost: CommonFeedItem) : PinDialogViewState()
    data class LoadingState(val isLoading: Boolean) : PinDialogViewState()
}