package base.util

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import base.BuildConfig
import base.BuildConfig.DEBUG
import base.FansChatApplication.Companion.firebaseAnalytics
import base.data.cache.Cache.Companion.cache
import java.util.*

object Analytics {

    private fun sendEvent(
        action: String,
        params: Map<String, String> = emptyMap()
    ) {
        if (DEBUG) return
        sendEventsToFirebase(action, params)
    }

    private fun sendEventsToFirebase(action: String, params: Map<String, String>) {
        val bundle = Bundle()
        params.forEach { (key, value) -> bundle.putString(key, value) }
        firebaseAnalytics.logEvent(action, bundle)
    }

    private fun sendEventWithSession(
        action: String,
        eventDetails: Map<String, String> = emptyMap()
    ) {
        val userId = cache.daoUsers().getCurrentImmediately()?.id ?: return

        val sessionData = mutableMapOf(
            "Date/Time" to Date().toString(),
            "anonymousUserID" to userId,
            "SessionID" to userId
        )

        val params = mutableMapOf<String, String>()
        params.putAll(sessionData)
        params.putAll(eventDetails)

        sendEvent(action, params)
    }

    /**
     * Interface for analytics
     */
    @SuppressLint("HardwareIds")

    @JvmStatic
    fun trackSessionStarted() {
        sendEventWithSession(
            "AppStart", mapOf(
                "BuildID" to BuildConfig.VERSION_NAME,
                "DeviceID" to deviceId
            )
        )
        launchTime = SystemClock.elapsedRealtime()
    }

    private var launchTime: Long = 0

    @JvmStatic
    fun trackDeepLinkOpened() {
        sendEventWithSession(
            "OpenedFromLink", mapOf(
                // Supply 'SocialPostID'
            )
        )
    }

    @JvmStatic
    fun trackAppClosed() {
        val exitTime = SystemClock.elapsedRealtime()
        val delta = exitTime - launchTime
        val sessionDuration = delta / 1000

        sendEventWithSession(
            "LeaveApp", mapOf(
                "TimeID" to sessionDuration.toString()
            )
        )
    }


    @JvmStatic
    fun trackUserRegistered() {
        sendEventWithSession(
            "RegistrationConfirm", mapOf(
                // Supply 'typeID' - user registers using email, or fb etc
            )
        )
    }

    @JvmStatic
    fun trackUserLoggedIn() {
        sendEventWithSession("LoginConfirm")
    }


    @JvmStatic
    fun trackHomeScreenDisplayed() {
        sendEventWithSession("MainScreenDisplay")
    }

    @JvmStatic
    fun trackSettingsChanged(settingName: String) {
        sendEventWithSession(
            "SettingsChange", mapOf(
                "SettingID" to settingName
            )
        )
    }

    @JvmStatic
    fun trackWallDisplayed() {
        sendEventWithSession(
            "WallSelected", mapOf(
                "FeatureID" to "Wall"
            )
        )
    }


    @JvmStatic
    fun trackPostViewed(postId: String) {
        sendEventWithSession(
            "WallPostOpen", mapOf(
                "PostID" to postId
            )
        )
    }

    @JvmStatic
    fun trackPostComposing() {
        sendEventWithSession(
            "WallPostStarted", mapOf(
                "TypeID" to "Creating new post"
            )
        )
    }

    @JvmStatic
    fun trackPostSubmitted(source: String) {
        sendEventWithSession(
            "WallPostSent", mapOf(
                "TypeID" to source
            )
        )
    }

    @JvmStatic
    fun trackPostCommentSubmitted(postId: String) {
        sendEventWithSession(
            "WallComment", mapOf(
                "PostID" to postId
            )
        )
    }

    @JvmStatic
    fun trackPostLiked(postId: String) {
        sendEventWithSession(
            "WallPostLiked", mapOf(
                "PostID" to postId
            )
        )
    }

    @JvmStatic
    fun trackPostShared(postId: String) {
        sendEventWithSession(
            "WallPostShared", mapOf(
                "PostID" to postId
            )
        )
    }

    @JvmStatic
    fun trackPostPin(postId: String) {
        sendEventWithSession(
            "WallPostPin", mapOf(
                "PostID" to postId
            )
        )
    }


    @JvmStatic
    fun trackChatOpened() {
        sendEventWithSession(
            "IMSSelected", mapOf(
                "FeatureID" to "IMS"
            )
        )
    }

    @JvmStatic
    fun trackChatCreated() {
        sendEventWithSession(
            "IMSChatCreated", mapOf(
                "TypeID" to "Private group" // Only private chats are currently allowed
            )
        )
    }

    @JvmStatic
    fun trackChatJoined() {
        sendEventWithSession(
            "IMSChatJoined", mapOf(
                "TypeID" to "Private group" // Only private chats are currently allowed
            )
        )
    }

    @JvmStatic
    fun trackMessageSent(chatId: String) {
        sendEventWithSession(
            "IMSmessage", mapOf(
                "TypeID" to "Private group", // Only private chats are currently allowed
                "chatID" to chatId
            )
        )
    }


    @JvmStatic
    fun trackTvOpened() {
        sendEventWithSession(
            "TVSelected", mapOf(
                "FeatureID" to "Club TV"
            )
        )
    }

    @JvmStatic
    fun trackVideoStarted(videoId: String) {
        sendEventWithSession(
            "TVShowStarted", mapOf(
                "showID" to videoId
            )
        )
    }

    @JvmStatic
    fun trackVideoEnded(videoId: String) {
        sendEventWithSession(
            "TVShowEnds", mapOf(
                "showID" to videoId
            )
        )
    }

    @JvmStatic
    fun trackTicketsOpened() {
        sendEventWithSession(
            "TicketsSelected", mapOf(
                "FeatureID" to "Tickets"
            )
        )
    }

    @JvmStatic
    fun trackCalendarsOpened() {
        sendEventWithSession(
            "CalendarSelected", mapOf(
                "FeatureID" to "Calendar"
            )
        )
    }

    @JvmStatic
    fun trackNewsSectionOpened() {
        sendEventWithSession(
            "NewsSelected", mapOf(
                "FeatureID" to "News"
            )
        )
    }

    @JvmStatic
    fun trackNewsItemOpened(postId: String) {
        sendEventWithSession(
            "NewsItemSelected", mapOf(
                "ItemID" to postId
            )
        )
    }

    @JvmStatic
    fun trackNewsCommentSubmitted(postId: String) {
        sendEventWithSession(
            "NewsItemComment", mapOf(
                "ItemID" to postId
            )
        )
    }

    @JvmStatic
    fun trackNewsLiked(postId: String) {
        sendEventWithSession(
            "NewsItemLiked", mapOf(
                "ItemID" to postId
            )
        )
    }

    @JvmStatic
    fun trackNewsShared(postId: String) {
        sendEventWithSession(
            "NewsItemShared", mapOf(
                "ItemID" to postId
            )
        )
    }


    @JvmStatic
    fun trackSocialSectionOpened() {
        sendEvent(
            "SocialSelected", mapOf(
                "FeatureID" to "Social"
            )
        )
    }

    @JvmStatic
    fun trackClubTvSectionOpened() {
        sendEvent(
            "ClubTVSelected", mapOf(
                "FeatureID" to "ClubTv"
            )
        )
    }

    @JvmStatic
    fun trackSocialItemOpened() {
        sendEvent("SocialPostOpen")
    }

    @JvmStatic
    fun trackSocialItemPinned() {
        sendEvent("SocialPostSent")
    }

    @JvmStatic
    fun trackSocialLiked() {
        sendEvent("SocialPostLiked")
    }

    @JvmStatic
    fun trackSocialShared(postId: String) {
        sendEventWithSession(
            "SocialPostShared", mapOf(
                "ItemID" to postId
            )
        )

    }

    @JvmStatic
    fun trackRumourShared(postId: String) {
        sendEventWithSession(
            "RumourPostShared", mapOf(
                "ItemID" to postId
            )
        )
    }

    @JvmStatic
    fun trackVideoShared(postId: String) {
        sendEventWithSession(
            "VideoPostShared", mapOf(
                "ItemID" to postId
            )
        )

    }

    @JvmStatic
    fun trackSocialCommentSubmitted() {
        sendEvent("SocialPostComment")
    }


    @JvmStatic
    fun trackStoreOpened() {
        sendEventWithSession(
            "ShopSelected", mapOf(
                "FeatureID" to "Shop"
            )
        )
    }

    @JvmStatic
    fun trackProductViewed() {
        sendEvent("ShopPageSelected")
    }

    @JvmStatic
    fun trackStatsOpened() {
        sendEventWithSession(
            "StatsSelected", mapOf(
                "FeatureID" to "Stats"
            )
        )
    }


    @JvmStatic
    fun trackRumoursOpened() {
        sendEventWithSession(
            "RumoursSelected", mapOf(
                "FeatureID" to "Web Rumors"
            )
        )
    }

    @JvmStatic
    fun trackFriendsViewed() {
        sendEventWithSession(
            "FriendSelected", mapOf(
                "FeatureID" to "Friends"
            )
        )
    }

    @JvmStatic
    fun trackFriendRequestSent() {
        sendEventWithSession(
            "FriendAdded", mapOf(
                "WhereFromID" to "",
                "newfriendType" to "",
                "numFriends" to ""
            )
        )
    }

    @JvmStatic
    fun trackFriendRequestAccepted() {
        sendEvent("FriendApproved")
    }

    @JvmStatic
    fun trackFriendRequestRejected() {
        sendEvent("FriendRejected")
    }

    @JvmStatic
    fun trackUnfollowed() {
        sendEvent("FriendUnfollow")
    }

    @JvmStatic
    fun trackUnfriended() {
        sendEvent("FriendRemoved")
    }

    @JvmStatic
    fun trackWebviewDisplayed() {
        sendEvent("<WebView>Selected")
    }

    @JvmStatic
    fun trackWebviewPageSelected() {
        sendEvent("<WebView>PageSelected")
    }
}