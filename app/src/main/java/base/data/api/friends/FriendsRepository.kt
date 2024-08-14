package base.data.api.friends

import base.data.api.authentication.model.FansChatCommonMessage
import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.friends.model.DeleteFriendsRequest
import base.data.api.friends.model.GetListOfFriendsResponse
import base.data.api.friends.model.TranslateMessage
import base.data.api.users.model.FansChatUserDetails
import io.reactivex.Single

class FriendsRepository (private val friendsRetrofitAPI: FriendsRetrofitAPI) {

    fun getFriendList(friendRequestRequest: GetFriendRequestRequest): Single<List<FansChatUserDetails>> {
        return friendsRetrofitAPI.getListOfFriends(friendRequestRequest.page, friendRequestRequest.perPage).map { it.listOfUser ?: listOf() }
    }

    fun getMutualFriendList(userId: String, getFriendRequestRequest: GetFriendRequestRequest): Single<GetListOfFriendsResponse> {
        return friendsRetrofitAPI.getListOfMutualFriends(userId, getFriendRequestRequest)
            .map { it }
    }

    fun deleteFriend(userId: String, deleteFriendsRequest: DeleteFriendsRequest): Single<FansChatCommonMessage> {
        return friendsRetrofitAPI.deleteFriend(userId, deleteFriendsRequest)
            .map { it }
    }

    fun getTranslateMessage(postId: String, targetLanguage: String): Single<TranslateMessage> {
        return friendsRetrofitAPI.getTranslateMessage(postId, targetLanguage)
            .map { it }
    }
}