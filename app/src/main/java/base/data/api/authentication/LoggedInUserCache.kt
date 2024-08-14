package base.data.api.authentication

import base.data.prefs.LocalPrefs
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 *
 * This class is responsible for caching the logged in user so that the user can
 * be accessed without having to contact the server meaning it's faster and can
 * be accessed offline
 */
class LoggedInUserCache(
    private val localPrefs: LocalPrefs
) {

    private val userAuthenticationFailSubject = PublishSubject.create<Unit>()
    val userAuthenticationFail: Observable<Unit> = userAuthenticationFailSubject.hide()

    private val friendRequestScreenOpenSubject = PublishSubject.create<Unit>()
    val friendRequestScreenOpen: Observable<Unit> = friendRequestScreenOpenSubject.hide()

    enum class PreferenceKey(val identifier: String) {
        LOGGED_IN_USER_TOKEN("token"),
        LOGGED_IN_USER_REFRESH_TOKEN("refreshToken"),
        LOGGED_IN_FIREBASE_TOKEN("firebaseToken"),
        LOGGED_IN_USER_ID("userId"),
        LOGGED_IN_USER_PASSWORD("userInfo"),
        LOGGED_IN_USER_PROFILE("userProfile"),
        LOGGED_IN_USER_DISPLAY_NAME("userName"),
        LOGGED_IN_LANGUAGE("language"),
    }
    private var userUnauthorized: Boolean = false

    init {
        userUnauthorized = false
    }

    private var loggedInUserTokenLocalPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_TOKEN.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_TOKEN.identifier, value)
        }

    private var loggedInUserIdPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_ID.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_ID.identifier, value)
        }

    private var loggedInUserPasswordPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_PASSWORD.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_PASSWORD.identifier, value)
        }

    private var loggedInUserRefreshTokenLocalPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_REFRESH_TOKEN.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_REFRESH_TOKEN.identifier, value)
        }

    private var loggedInUserProfileLocalPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_PROFILE.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_PROFILE.identifier, value)
        }

    private var loggedInFirebaseTokenLocalPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_FIREBASE_TOKEN.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_FIREBASE_TOKEN.identifier, value)
        }

    private var loggedInLanguageLocalPref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_LANGUAGE.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_LANGUAGE.identifier, value)
        }

    private var loggedInUserNamePref: String?
        get() {
            return localPrefs.getString(PreferenceKey.LOGGED_IN_USER_DISPLAY_NAME.identifier, null)
        }
        set(value) {
            localPrefs.putString(PreferenceKey.LOGGED_IN_USER_DISPLAY_NAME.identifier, value)
        }


    fun setUserName(displayName: String?) {
        loggedInUserNamePref = displayName
    }

    fun getUserName(): String? {
        return loggedInUserNamePref
    }

    fun setLanguage(language: String?) {
        loggedInLanguageLocalPref = language
    }

    fun getLanguage(): String? {
        return loggedInLanguageLocalPref
    }

    fun setFirebaseToken(token: String?) {
        loggedInFirebaseTokenLocalPref = token
    }

    fun getFirebaseToken(): String? {
        return loggedInFirebaseTokenLocalPref
    }

    fun setLoggedInUserToken(token: String?) {
        loggedInUserTokenLocalPref = token
    }

    fun getLoginUserToken(): String? {
        return loggedInUserTokenLocalPref
    }

    fun setLoggedInUserRefreshToken(token: String?) {
        loggedInUserRefreshTokenLocalPref = token
    }

    fun getLoginUserRefreshToken(): String? {
        return loggedInUserRefreshTokenLocalPref
    }

    fun setLoggedInUserId(userId: String?) {
        loggedInUserIdPref = userId
    }

    fun getLoggedInUserId(): String? {
        return loggedInUserIdPref
    }

    fun setLoggedInPassword(password: String?) {
        loggedInUserPasswordPref = password
    }

    fun getLoggedInPassword(): String? {
        return loggedInUserPasswordPref
    }

    fun setLoggedInUserProfile(profile: String?) {
        loggedInUserProfileLocalPref = profile
    }

    fun getLoggedInUserProfile(): String? {
        return loggedInUserProfileLocalPref
    }

    fun clearLoggedInUserLocalPrefs() {
        // These preferences are currently only saved locally so we only want to wipe
        // them when a new user logs in, otherwise if the user logs out and logs back
        // in the settings will be gone
        clearUserPreferences()
    }

    fun userUnauthorized() {
        if (!userUnauthorized) {
            userAuthenticationFailSubject.onNext(Unit)
        }
        userUnauthorized = true
    }

    fun friendRequestScreenOpen() {
        friendRequestScreenOpenSubject.onNext(Unit)
    }

    /**
     * Clear previous user preferences, if the current logged in user is different
     */
    private fun clearUserPreferences() {
        try {
            for (preferenceKey in PreferenceKey.values()) {
                localPrefs.removeValue(preferenceKey.identifier)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
