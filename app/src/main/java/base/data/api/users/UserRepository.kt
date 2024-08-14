package base.data.api.users

import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.api.users.model.GetUserResponse
import io.reactivex.Single

class UserRepository(private val userRetrofitAPI: UserRetrofitAPI) {

    fun getUserList(getFriendRequestRequest: GetFriendRequestRequest): Single<GetUserResponse> {
        return if (getFriendRequestRequest.displayName.isNullOrEmpty()) {
            userRetrofitAPI.getUserList(
                getFriendRequestRequest.page,
                getFriendRequestRequest.perPage,
                getFriendRequestRequest.isAdmin
            )
                .map { it }
        } else {
            userRetrofitAPI.searchGetUserList(
                getFriendRequestRequest.page, getFriendRequestRequest.perPage,
                getFriendRequestRequest.displayName.toString(),
                getFriendRequestRequest.isAdmin
            ).map { it }
        }
    }

    fun searchUser(friendRequestRequest: GetFriendRequestRequest): Single<List<FansChatUserDetails>> {
        return if (friendRequestRequest.keywords.isNullOrEmpty()) {
            userRetrofitAPI.getUserList(
                friendRequestRequest.page,
                friendRequestRequest.perPage,
                friendRequestRequest.isAdmin
            ).map { it.data ?: listOf() }
        } else {
            userRetrofitAPI.searchGetUserList(
                friendRequestRequest.page,
                friendRequestRequest.perPage,
                friendRequestRequest.keywords,
                friendRequestRequest.isAdmin
            ).map { it.data ?: listOf() }
        }
    }

    fun getUserDetails(userId: String): Single<FansChatUserDetails> {
        return userRetrofitAPI.getUserDetails(userId)
            .map { it }
    }

}