package base.ui.fragment.other.friends.search.viewmodel

import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.users.UserRepository
import base.data.api.users.model.FansChatUserDetails
import base.data.network.NetworkException
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class SearchFriendViewModel(private val userRepository: UserRepository) : BaseViewModel() {

    private var perPage = 20

    private val searchFriendStateSubject: PublishSubject<SearchFriendViewState> = PublishSubject.create()
    val searchFriendState: Observable<SearchFriendViewState> = searchFriendStateSubject.hide()

    private var pageNumber: Int = 1
    private var isLoadMore: Boolean = true
    private var isLoading: Boolean = false
    private var listOfSearchUser: MutableList<FansChatUserDetails> = mutableListOf()

    fun searchFriendRequest(search: String? = null, isFromLoadMore: Boolean = false) {
        if (!search.isNullOrEmpty() && !isFromLoadMore) {
            resetLoading()
        }
        val searchString = GetFriendRequestRequest(perPage, pageNumber, keywords = search, isAdmin = false)
        userRepository.searchUser(searchString).doOnSubscribe {
                searchFriendStateSubject.onNext(SearchFriendViewState.LoadingState(true))
            }.doAfterTerminate {
                searchFriendStateSubject.onNext(SearchFriendViewState.LoadingState(false))
                isLoading = false
            }.subscribeOnIoAndObserveOnMainThread({
                when {
                    pageNumber == 1 -> {
                        listOfSearchUser.clear()
                        listOfSearchUser = it.toMutableList()
                        val list = listOfSearchUser.distinct()
                        searchFriendStateSubject.onNext(SearchFriendViewState.ListOfUser(list))
                    }
                    pageNumber != 1 && it.isNullOrEmpty() -> {
                        isLoadMore = false
                    }
                    !it.isNullOrEmpty() -> {
                        listOfSearchUser.addAll(it)
                        val list = listOfSearchUser.distinct()
                        searchFriendStateSubject.onNext(SearchFriendViewState.ListOfUser(list))
                    }
                }

            }, {
                Timber.e(it)
                //searchFriendStateSubject.onNext(SearchFriendViewState.ErrorMessage(it.localizedMessage ?: "", if (it is NetworkException) (it.exception as HttpException).code() else 0))
            }).autoDispose()
    }

    fun loadMore(search: String? = null) {
        if (!isLoading) {
            isLoading = true
            if (isLoadMore) {
                pageNumber++
                searchFriendRequest(search, true)
            }
        }
    }

    fun resetLoading() {
        listOfSearchUser.clear()
        pageNumber = 1
        isLoadMore = true
        isLoading = false
    }
}

sealed class SearchFriendViewState {
    data class ErrorMessage(val errorMessage: String, var errorCode: Int) : SearchFriendViewState()
    data class ListOfUser(val listOfSearchUser: List<FansChatUserDetails>) : SearchFriendViewState()
    data class LoadingState(val isLoading: Boolean) : SearchFriendViewState()
}