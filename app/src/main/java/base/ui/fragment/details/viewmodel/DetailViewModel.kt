package base.ui.fragment.details.viewmodel

import base.application.FansChat
import base.data.api.wall.WallPostsRepository
import base.data.api.wall.model.*
import base.data.model.CommonFeedItem
import base.data.network.NetworkException
import base.data.network.model.ErrorResult
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import base.util.Constants
import base.util.languageForTranslation
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class DetailViewModel(private val wallPostsRepository: WallPostsRepository) : BaseViewModel() {

    private val feedPostStateSubject: PublishSubject<DetailsViewState> = PublishSubject.create()
    val feedPostState: Observable<DetailsViewState> = feedPostStateSubject.hide()

    private var PER_PAGE = 20

    private var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false

    fun getPostDetail(postId: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.getNewsPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.getSocialPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.getRumoursPostData(postId, FansChat.context.languageForTranslation)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.getVideoPostData(postId,
                FansChat.context.languageForTranslation)
            else -> wallPostsRepository.getWallPostData(postId, FansChat.context.languageForTranslation)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.WallPostDetails(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "", when (it) {
                is NetworkException -> (it.exception as HttpException).code()
                is CompositeException -> if (it.exceptions.isNotEmpty()) (it.exceptions[0] as HttpException).code() else 0
                else -> 0
            }))
        }).autoDispose()
    }

    fun deleteWallPost(postId: String) {
        wallPostsRepository.deleteWallPost(postId).doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.DeletePostDetails("Post deleted"))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun updateWallPost(postId: String, wallPostRequest: WallPostRequest) {
        wallPostsRepository.updateWallPost(postId, wallPostRequest).doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.UpdatePost(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun addWallPostComment(addCommentRequest: AddCommentRequest, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.addNewsPostComment(addCommentRequest)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.addSocialPostComment(addCommentRequest)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.addRumoursPostComment(addCommentRequest)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.addVideoPostComment(
                addCommentRequest)
            else -> wallPostsRepository.addWallPostComment(addCommentRequest)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.AddPostComment(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun deleteWallPostComment(deleteWallPostRequest: DeleteWallPostCommentRequest, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.deleteNewsPostComment(deleteWallPostRequest)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.deleteSocialPostComment(deleteWallPostRequest)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.deleteRumoursPostComment(deleteWallPostRequest)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.deleteVideoPostComment(
                deleteWallPostRequest)
            else -> wallPostsRepository.deleteWallPostComment(deleteWallPostRequest)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeWithErrorPars({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            feedPostStateSubject.onNext(DetailsViewState.DeletePostComment("Comment deleted"))
        }, {
            when (it) {
                is ErrorResult.ErrorMessage -> {
                    feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.errorMessage, 0))
                }
                is ErrorResult.ErrorThrowable -> {
                    Timber.e(it.throwable)
                }
            }
        }).autoDispose()
    }

    fun resetLoadingComments(postId: String, type: String) {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
        getWallPostComment(postId, type)
    }

    private fun getWallPostComment(postId: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.getNewsPostComment(postId, pageNumber, PER_PAGE)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.getSocialPostComment(postId, pageNumber, PER_PAGE)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.getRumoursPostComment(postId, pageNumber, PER_PAGE)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.getVideoPostComment(postId,
                pageNumber,
                PER_PAGE)
            else -> wallPostsRepository.getWallPostComment(postId, pageNumber, PER_PAGE)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            isLoading = false
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))

            isLoading = false
            if (it.data?.size ?: 0 < PER_PAGE) {
                isLoadMore = false
            }

            it?.let { comments ->
                feedPostStateSubject.onNext(DetailsViewState.ListOfComments(comments))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun loadMore(postId: String, type: String) {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                getWallPostComment(postId, type)
            }
        }
    }

    fun updateWallPostComment(commentId: String, addCommentRequest: UpdateComment, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.updateNewsPostComment(commentId, addCommentRequest)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.updateSocialPostComment(commentId, addCommentRequest)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.updateRumorsPostComment(commentId, addCommentRequest)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.updateVideoPostComment(
                commentId,
                addCommentRequest)
            else -> wallPostsRepository.updateWallPostComment(commentId, addCommentRequest)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.UpdatePostComment(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun translatePostComment(commentId: String, targetLanguage: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.translateNewsComment(commentId, targetLanguage)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.translateSocialComment(commentId, targetLanguage)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.translateRumorsComment(commentId, targetLanguage)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.translateVideoComment(
                commentId,
                targetLanguage)
            else -> wallPostsRepository.translateWallComment(commentId, targetLanguage)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.TranslateComment(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun likeWallPost(postId: String, type: String, updateLike: UpdateLike) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.likeNewsPosts(postId, updateLike)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.likeSocialPosts(postId, updateLike)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.likeRumoursPosts(postId, updateLike)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.likeVideoPosts(postId,
                updateLike)
            else -> wallPostsRepository.likeWallPosts(postId, updateLike)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.LikePostResponse(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun translatePost(postId: String, targetLanguage: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.translateNewsPost(postId, targetLanguage)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.translateSocialPost(postId, targetLanguage)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.translateRumorsPost(postId, targetLanguage)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.translateVideoPost(postId,
                targetLanguage)
            else -> wallPostsRepository.translateWallPost(postId, targetLanguage)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { posts ->
                feedPostStateSubject.onNext(DetailsViewState.TranslatePost(posts))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun getSharingUrl(postId: String, type: String) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.getShareNewsUrl(postId)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.getShareSocialUrl(postId)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.getShareRumoursUrl(postId)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.getShareVideoUrl(postId)
            else -> wallPostsRepository.getShareWallUrl(postId)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { sharingResponse ->
                feedPostStateSubject.onNext(DetailsViewState.SharePost(sharingResponse))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }

    fun reportPost(type: String, reportPostRequest: ReportPostRequest) {
        when (type) {
            Constants.POST_TYPE_NEWS -> wallPostsRepository.reportNewsPost(reportPostRequest)
            Constants.POST_TYPE_SOCIAL -> wallPostsRepository.reportSocialPost(reportPostRequest)
            Constants.POST_TYPE_RUMOURS -> wallPostsRepository.reportRumoursPost(reportPostRequest)
            Constants.POST_TYPE_CLUB_TV, Constants.POST_TYPE_STREAMING -> wallPostsRepository.reportVideoPost(
                reportPostRequest)
            else -> wallPostsRepository.reportWallPost(reportPostRequest)
        }.doOnSubscribe {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(true))
        }.doAfterTerminate {
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
        }.subscribeOnIoAndObserveOnMainThread({
            feedPostStateSubject.onNext(DetailsViewState.LoadingState(false))
            it?.let { reportPostResponse ->
                feedPostStateSubject.onNext(DetailsViewState.ReportPost(reportPostResponse))
            }
        }, {
            Timber.e(it)
            feedPostStateSubject.onNext(DetailsViewState.ErrorMessage(it.localizedMessage ?: "",
                if (it is NetworkException) (it.exception as HttpException).code() else 0))
        }).autoDispose()
    }
}

sealed class DetailsViewState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : DetailsViewState()
    data class WallPostDetails(val wallPost: CommonFeedItem) : DetailsViewState()
    data class DeletePostDetails(val deleteWallPost: String) : DetailsViewState()
    data class AddPostComment(val addCommentResponse: AddCommentsResponse) : DetailsViewState()
    data class DeletePostComment(val deleteComment: String) : DetailsViewState()
    data class UpdatePostComment(val commentList: Comment) : DetailsViewState()
    data class ListOfComments(val addCommentResponse: CommentResponse) : DetailsViewState()
    data class LikePostResponse(val specificItemResponse: CommonFeedItem) : DetailsViewState()
    data class UpdatePost(val postWallResponse: CommonFeedItem) : DetailsViewState()
    data class LoadingState(val isLoading: Boolean) : DetailsViewState()
    data class TranslateComment(val translatedComment: TranslateResponse) : DetailsViewState()
    data class TranslatePost(val translatedPost: TranslateResponse) : DetailsViewState()
    data class SharePost(val sharingResponse: SharingResponse) : DetailsViewState()
    data class ReportPost(val reportPostResponse: ReportPostResponse) : DetailsViewState()
}