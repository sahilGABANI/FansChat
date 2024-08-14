package base.util

import io.reactivex.subjects.PublishSubject

object Constants {
    var liveDataAudioFocusObserver: PublishSubject<Any> = PublishSubject.create()

    const val SF_NOTIFICATION_SETTINGS: String = "sf_notification_settings"

    const val VIMEO_SORT_BY: String = "added"
    const val VIMEO_CHANNEL_ID: String = "1740017"
    const val VIMEO_CLIENT_ID: String = "84131dff4f52fac0c8e16b004ee701171a9f830f"
    const val VIMEO_CLIENT_SECRET: String =
        "ba87BHJqdcfPzs4LU/DfuybQEkVm9cYGY6g4qML1O9hRV/RnWPnf6WXfyf6GClheu/yg/PitJZTCu4nBL1ptduw8Fq/8W4UxTy075gEpze64zbH3qfCCE40tSMY35H3I"
    const val VIMEO_ACCESS_TOKEN: String = "873966feba6f9ed9f971effdbf87bfac"//Not using for now

    const val ImsMessage = "ImsMessage"
    const val ImsUpdateChatInfo = "ImsUpdateChatInfo"
    const val Operation_ImsMessage_new = "new"
    const val Operation_ImsMessage_update = "update"
    const val Operation_ImsMessage_delete = "delete"
    const val ImsUpdateUserIsTypingState = "ImsUpdateUserIsTypingState"
    const val ACTION_OPEN_CHAT = "base.util.open_chat"
    const val ACTION_OPEN_FRIEND_REQUEST = "base.util.open_friend_request"
    const val ACTION_OPEN_FRIEND_PROFILE = "base.util.open_friend_profile"
    const val ACTION_REMOVE_FRIENDS = "base.util.remove_friends"
    const val ACTION_FRIEND_REQUEST_DECISION = "base.util.friend_request_decision"
    const val ACTION_COMMENT_UPDATED =
        "base.util.comment_updated"//update comment if post detail page is open

    const val FriendRequest = "FriendRequest"
    const val Operation_sent = "sent"
    const val Operation_accepted = "accepted"

    const val UserInfo = "UserInfo"
    const val Operation_followed = "followed"
    const val Operation_unfollowed = "unfollowed"
    const val Operation_unfriended = "unfriended"

    const val Wall = "Wall"
    const val Operation_like = "like"
    const val Operation_post_delete = "delete"
    const val Operation_post_updated = "post_updated"
    const val Operation_new_post = "new_post"
    const val Operation_comment = "comment"
    const val Operation_comment_updated = "comment updated"
    const val Operation_comment_deleted = "comment deleted"

    const val DELAY_WALL_BANNER = 5000L
    const val DELAY_CONTROLLER_HIDE = 2000L

    /*Module Types*/
    const val Type_Banner = "Banner"
    const val Type_Gallery = "Gallery"
    const val Type_Carousel = "Carousel"
    const val Type_Pager = "Pager"
    const val Type_PortraitCarousel = "PortraitCarousel"
    const val Type_Feed = "Feed"
    /*Module Types END*/

    /*Post Content Types*/
    const val POST_TYPE_VIDEO = "video"
    const val POST_TYPE_IMAGE = "image"
    const val POST_TYPE_TEXT = "text"
    /*Post Content Types END*/

    /*Keep in lowercase so it can be easy to compare*/
    const val POST_TYPE_WALL = "wall"
    const val POST_TYPE_NEWS = "news"
    const val POST_TYPE_SOCIAL = "social"
    const val POST_TYPE_RUMOURS = "rumors"
    const val POST_TYPE_CLUB_TV = "videos"
    const val POST_TYPE_STREAMING = "streaming"
    /*Keep in lowercase so it can be easy to compare END*/

    const val POST_ORIGINAL_TYPE_WALL = "Wall"
    const val POST_ORIGINAL_TYPE_NEWS = "News"
    const val POST_ORIGINAL_TYPE_SOCIAL = "Social"
    const val POST_ORIGINAL_TYPE_RUMOURS = "Rumors"
    const val POST_ORIGINAL_TYPE_CLUB_TV = "Videos"
//    const val POST_ORIGINAL_TYPE_STREAMING = "Streaming"
    //const val POST_TYPE_VIDEO = "Video"


    /*Shared post modules*/
    const val SHARED_POST_TYPE_WALL = "posts"
    const val SHARED_POST_TYPE_NEWS = "news"
    const val SHARED_POST_TYPE_SOCIAL = "social"
    const val SHARED_POST_TYPE_RUMOURS = "rumors"
    const val SHARED_POST_TYPE_CLUB_TV = "videos"
    /*Shared post modules END*/

    /*See all*/
    const val SEE_ALL_POST_TYPE_NEWS = "NEWS"
    const val SEE_ALL_POST_TYPE_SOCIAL = "SOCIAL"
    const val SEE_ALL_POST_TYPE_RUMOURS = "RUMORS"
    const val SEE_ALL_POST_TYPE_CLUB_TV = "VIDEOS"
    /*See all END*/

    /*Notification Actions*/
    const val ACTION_OPEN_WALL = "base.util.open_wall"
    const val ACTION_WALL_RELOAD_INDICATOR = "base.util.open_wall_reload_indicator"
    const val ACTION_NOTIFICATION_RELOAD_INDICATOR = "base.util.notification_reload_indicator"
    const val ACTION_SEND_FRIEND_REQUEST = "base.util.send_friend_request"
    const val ACTION_ACCEPT_SEND_FRIEND_REQUEST = "base.util.accept_send_friend_request"
    const val ACTION_DECLINE_SEND_FRIEND_REQUEST = "base.util.decline_send_friend_request"
    const val ACTION_REMOVE_FRIEND = "base.util.remove_friend"
    const val ACTION_OPEN_POST_DETAIL = "base.util.open_post_detail"
    const val ACTION_POST_DELETED = "base.util.post_deleted"
    const val ACTION_SEND_MESSAGE = "base.util.send_messsage"

    //enabled it on login/register/logout so later we can refresh the wall
    var IS_FORCE_REFRESH = false
    var SHOW_REFRESH_INDICATOR = false

    /*Notification Settings*/
    const val NS_WALL = "wall"
    const val NS_NEWS = "news"
    const val NS_SOCIAL = "social"
    const val NS_RUMOR = "rumor"
    const val NS_VIDEO = "video"
    const val NS_IMS = "ims"

}