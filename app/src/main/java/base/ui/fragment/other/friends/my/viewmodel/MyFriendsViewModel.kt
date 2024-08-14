package base.ui.fragment.other.friends.my.viewmodel

import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.friends.FriendsRepository
import base.data.api.users.model.FansChatUserDetails
import base.data.network.NetworkException
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class MyFriendsViewModel(
    private val friendsRepository: FriendsRepository
) : BaseViewModel() {

    private val myFriendsViewStateSubject: PublishSubject<MyFriendsViewState> =
        PublishSubject.create()
    val myFriendsViewState: Observable<MyFriendsViewState> = myFriendsViewStateSubject.hide()


    private var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false
    private var perPage = 20
    private var listOfFriends: MutableList<FansChatUserDetails> = mutableListOf()

    fun loadMyFriends() {
        friendsRepository.getFriendList(GetFriendRequestRequest(perPage, pageNumber))
            .doOnSubscribe {
                myFriendsViewStateSubject.onNext(MyFriendsViewState.LoadingState(true))
            }
            .doAfterTerminate {
                myFriendsViewStateSubject.onNext(MyFriendsViewState.LoadingState(false))
                isLoading = false
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())
                if (it.isNullOrEmpty() || it.size < perPage) {
                    isLoadMore = false
                }
                listOfFriends.addAll(it)
                myFriendsViewStateSubject.onNext(MyFriendsViewState.ListOfFriends(it))
            }, {
                Timber.e(it)
                myFriendsViewStateSubject.onNext(
                    MyFriendsViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun loadMoreFriends() {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                loadMyFriends()
            }
        }
    }

    fun resetLoading() {
        pageNumber = 1
        isLoadMore = true
        isLoading = false
    }
}

sealed class MyFriendsViewState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : MyFriendsViewState()
    data class ListOfFriends(val listOfFriends: List<FansChatUserDetails>) : MyFriendsViewState()
    data class LoadingState(val isLoading: Boolean) : MyFriendsViewState()
}