package base.ui.fragment.other.friends.requests.viewmodel

import base.data.api.friendrequest.FriendRequestRepository
import base.data.api.friendrequest.model.*
import base.data.api.users.UserRepository
import base.data.api.users.model.FansChatUserDetails
import base.data.network.NetworkException
import base.data.network.model.ErrorResult
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class FriendRequestViewModel(
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private var PER_PAGE = 20

    private val friendRequestStateSubject: PublishSubject<FriendRequestViewState> =
        PublishSubject.create()
    val friendRequestState: Observable<FriendRequestViewState> = friendRequestStateSubject.hide()

    var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false

    private var fpageNumber: Int = 1
    private var fisLoadMore: Boolean = true
    private var fisLoading: Boolean = false

    fun getFriendRequestList() {
        friendRequestRepository.getFriendRequest(GetFriendRequestRequest(PER_PAGE, pageNumber))
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
                isLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())

                it.data?.let { friendRequest ->
                    friendRequestStateSubject.onNext(
                        FriendRequestViewState.ListofFriendRequest(
                            friendRequest as ArrayList<FriendRequest>
                        )
                    )
                }
            }, {
                Timber.e(it)
//                friendRequestStateSubject.onNext(
//                    FriendRequestViewState.ErrorMessage(
//                        it.localizedMessage ?: "",
//                        if (it is NetworkException) (it.exception as HttpException).code() else 0
//                    )
//                )
            }).autoDispose()
    }

    fun loadMore() {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                getFriendRequestList()
            }
        }
    }

    fun resetLoading() {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
    }

    fun acceptDetails(userId: String, friendRequest: FriendRequest? = null) {
        friendRequestRepository.acceptFriendRequest(userId)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.AcceptFriendRequest(
                        it,
                        friendRequest
                    )
                )
            }, {
                Timber.e(it)
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun declineDetails(userId: String, friendRequest: FriendRequest? = null) {
        friendRequestRepository.declineFriendRequest(userId)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.DeclineFriendRequest(
                        "Declined friend request",
                        friendRequest
                    )
                )
            }, {
                Timber.e(it)
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun sendFriendsRequest(friendRequestRequest: FriendRequestRequest) {
        friendRequestRepository.sendFriendsRequest(friendRequestRequest)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
            }
            .subscribeWithErrorPars({
                Timber.i(it.toString())
                friendRequestStateSubject.onNext(FriendRequestViewState.SuccessMessage(it.message))
            }, {
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        friendRequestStateSubject.onNext(
                            FriendRequestViewState.ErrorMessages(
                                it.errorMessage
                            )
                        )
                    }
                    is ErrorResult.ErrorThrowable -> {
                        Timber.e(it.throwable)
                    }
                }
            }).autoDispose()
    }

    fun getSpecificUserDetails(userId: String) {
        userRepository.getUserDetails(userId)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingStates(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingStates(false))
//                isLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())

                it?.let { userDetails ->
                    friendRequestStateSubject.onNext(FriendRequestViewState.UserDetails(userDetails))
                }
            }, {
                Timber.e(it)
//                friendRequestStateSubject.onNext(
//                    FriendRequestViewState.ErrorMessage(
//                        it.localizedMessage ?: "",
//                        if (it is NetworkException) (it.exception as HttpException).code() else 0
//                    )
//                )
            }).autoDispose()
    }

    fun deleteFriends(userId: String) {
        friendRequestRepository.deleteFriends(userId)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
//                isLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())

                friendRequestStateSubject.onNext(FriendRequestViewState.DeleteFriends("Unfriend successfully"))
            }, {
                Timber.e(it)
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun getListOfFriends(userId: String) {
        friendRequestRepository.getListOfFriends(userId, fpageNumber, PER_PAGE)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
                fisLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                friendRequestStateSubject.onNext(FriendRequestViewState.ListofFriends(it.count, it.listOfUser ?: listOf()))
            }, {
                Timber.e(it)
            }).autoDispose()
    }

    fun reportUser(reportRequest: ReportRequest) {
        friendRequestRepository.reportUser(reportRequest)
            .doOnSubscribe {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(true))
            }
            .doAfterTerminate {
                friendRequestStateSubject.onNext(FriendRequestViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())
                friendRequestStateSubject.onNext(FriendRequestViewState.ReportUserSuccess("User Reported Successfully"))
            }, {
                Timber.e(it)
                friendRequestStateSubject.onNext(
                    FriendRequestViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun loadMoreFriends(userId: String) {
        if (!fisLoading) {
            fisLoading = true
            if (fisLoadMore) {
                fpageNumber++
                getListOfFriends(userId)
            }
        }
    }
}

sealed class FriendRequestViewState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : FriendRequestViewState()
    data class ErrorMessages(val errorMessage: String) : FriendRequestViewState()
    data class SuccessMessage(val successMessage: String) : FriendRequestViewState()
    data class ListofFriendRequest(val listofuser: ArrayList<FriendRequest>) :
        FriendRequestViewState()

    data class ListofFriends(val count: Int, val listofuser: List<FansChatUserDetails>) :
        FriendRequestViewState()

    data class UserDetails(val userDetails: FansChatUserDetails) : FriendRequestViewState()
    data class AcceptFriendRequest(
        val acceptFriendRequest: AcceptFriendRequestResponse,
        val friendRequest: FriendRequest? = null
    ) :
        FriendRequestViewState()

    data class DeclineFriendRequest(
        val successMessage: String,
        val friendRequest: FriendRequest? = null
    ) : FriendRequestViewState()

    data class DeleteFriends(val successMessage: String) : FriendRequestViewState()
    data class LoadingState(val isLoading: Boolean) : FriendRequestViewState()
    data class LoadingStates(val isLoading: Boolean) : FriendRequestViewState()
    data class ReportUserSuccess(val successMessage: String) : FriendRequestViewState()
}