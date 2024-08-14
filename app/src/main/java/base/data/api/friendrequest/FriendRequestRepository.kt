package base.data.api.friendrequest

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.friendrequest.model.*
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.users.model.FansChatUserDetails
import io.reactivex.Single
import okhttp3.ResponseBody

class FriendRequestRepository(private val friendRequestRetrofitAPI: FriendRequestRetrofitAPI) {

    fun sendFriendsRequest(friendRequestRequest: FriendRequestRequest): Single<FriendRequestResponse> {
        return friendRequestRetrofitAPI.sendFriendsRequest(friendRequestRequest)
            .map { it }
    }

    fun getFriendRequest(getFriendRequestRequest: GetFriendRequestRequest): Single<ListOfFriendRequest> {
        return friendRequestRetrofitAPI.getFriendRequest(getFriendRequestRequest.page, getFriendRequestRequest.perPage)
            .map { it }
    }

    fun pendingFriendRequestCount(getFriendRequestRequest: GetFriendRequestRequest): Single<Int> {
        return friendRequestRetrofitAPI.getFriendRequest(getFriendRequestRequest.page, getFriendRequestRequest.perPage)
            .map { it.count ?: 0 }
    }

    fun declineFriendRequest(userId: String): Single<ResponseBody> {
        return friendRequestRetrofitAPI.declineFriendRequest(userId)
            .map { it }
    }

    fun acceptFriendRequest(userId: String): Single<AcceptFriendRequestResponse> {
        return friendRequestRetrofitAPI.acceptFriendRequest(userId)
            .map { it }
    }

    fun getUserDetails(userId: Int): Single<FansChatUserDetails> {
        return friendRequestRetrofitAPI.getUserDetails(userId)
            .map { it }
    }

    fun deleteFriends(userId: String): Single<ResponseBody> {
        return friendRequestRetrofitAPI.deleteFriends(userId)
            .map { it }
    }

    fun getListOfFriends(userId: String, page: Int, perPage: Int): Single<GetListOfFriendsResponse> {
        return friendRequestRetrofitAPI.getListOfFriends(userId, page, perPage)
            .map { it }
    }

    fun reportUser(reportRequest: ReportRequest): Single<ReportResponse> {
        return friendRequestRetrofitAPI.reportUser(reportRequest)
            .map { it }
    }
}