package base.ui.fragment.other.profile.viewmodel

import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.ProfileCacheDataRepository
import base.data.api.authentication.model.ChangePasswordRequest
import base.data.api.authentication.model.UpdateAvatarRequest
import base.data.api.authentication.model.UpdateProfileRequest
import base.data.api.friends.model.ProfileToggleRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.model.NotificationSettings
import base.data.network.NetworkException
import base.data.network.model.ErrorResult
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber

class EditProfileViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val profileCacheDataRepository: ProfileCacheDataRepository
) : BaseViewModel() {

    private val editprofileStateSubject: PublishSubject<EditProfileViewState> =
        PublishSubject.create()
    val editprofileState: Observable<EditProfileViewState> = editprofileStateSubject.hide()

    fun getUserProfile() {
        authenticationRepository.getUserProfile()
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                editprofileStateSubject.onNext(EditProfileViewState.UserDetails(it))
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun updateUserProfile(updateProfileRequest: UpdateProfileRequest) {
        authenticationRepository.updateUserProfile(updateProfileRequest)
            .doOnSubscribe {
                editprofileStateSubject.onNext(
                    EditProfileViewState.LoadingState(
                        isLoading = true,
                        hideButton = true
                    )
                )
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(
                    EditProfileViewState.LoadingState(
                        isLoading = false,
                        hideButton = true
                    )
                )
            }
            .subscribeWithErrorPars({
                Timber.i(it.toString())
                editprofileStateSubject.onNext(EditProfileViewState.UpdateUserDetails(it))
                profileCacheDataRepository.addProfile(it)

            }, {
                Timber.e(it.toString())
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        editprofileStateSubject.onNext(
                            EditProfileViewState.ErrorMessages(
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

    fun updateAvatar(updateAvatarRequest: UpdateAvatarRequest) {
        authenticationRepository.updateAvatar(updateAvatarRequest)
            .doOnSubscribe {
                editprofileStateSubject.onNext(
                    EditProfileViewState.LoadingState(
                        isLoading = true,
                        hideButton = true
                    )
                )
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(
                    EditProfileViewState.LoadingState(
                        isLoading = false,
                        hideButton = true
                    )
                )
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())
                editprofileStateSubject.onNext(EditProfileViewState.UpdateUserDetails(it))
                profileCacheDataRepository.addProfile(it)

            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun deleteAvatar() {
        authenticationRepository.deleteAvatar()
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                Timber.i(it.toString())
                editprofileStateSubject.onNext(EditProfileViewState.DeleteUserDetails(it))
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        authenticationRepository.changePassword(
            ChangePasswordRequest(
                oldPassword,
                newPassword,
                newPassword
            )
        )
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                it.message?.let { message ->
                    editprofileStateSubject.onNext(EditProfileViewState.ChangePassword(message))
                }
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun updateNotificationToggles(mute: String?, unmute: String?, message: String) {
        authenticationRepository.updateNotificationToggles(
            ProfileToggleRequest(
                mute,
                unmute
            )
        )
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                editprofileStateSubject.onNext(
                    EditProfileViewState.ToggleNotificationMessage(
                        it,
                        message
                    )
                )
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun getNotificationToggles() {
        authenticationRepository.getNotificationToggles()
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                editprofileStateSubject.onNext(
                    EditProfileViewState.ToggleNotificationMessage(
                        it,
                        null
                    )
                )
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun getListOfFriends(userId: String) {
        authenticationRepository.getListOfFriends(userId)
            .doOnSubscribe {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(true))
            }
            .doAfterTerminate {
                editprofileStateSubject.onNext(EditProfileViewState.LoadingState(false))
            }
            .subscribeOnIoAndObserveOnMainThread({
                it.count.let { count ->
                    editprofileStateSubject.onNext(EditProfileViewState.TotalFriendsCount(count))
                }
            }, {
                Timber.e(it)
                editprofileStateSubject.onNext(
                    EditProfileViewState.ErrorMessage(
                        it.localizedMessage ?: "",
                        if (it is NetworkException) (it.exception as HttpException).code() else 0
                    )
                )
            }).autoDispose()
    }

    fun logout() {
        authenticationRepository.logout().subscribeOnIoAndObserveOnMainThread({
            editprofileStateSubject.onNext(EditProfileViewState.Logout)
        }, {
            editprofileStateSubject.onNext(EditProfileViewState.Logout)
        }).autoDispose()
    }

    sealed class EditProfileViewState {
        data class ErrorMessage(val errorMessage: String, var errorCode: Int) :
            EditProfileViewState()

        data class ErrorMessages(val errorMessage: String) : EditProfileViewState()
        data class SuccessMessage(val successMessage: String) : EditProfileViewState()
        data class TotalFriendsCount(val count: Int) : EditProfileViewState()
        data class ChangePassword(val successMessage: String) : EditProfileViewState()
        data class UserDetails(val fansChatUser: FansChatUserDetails) : EditProfileViewState()
        data class UpdateUserDetails(val fansChatUser: FansChatUserDetails) : EditProfileViewState()
        data class DeleteUserDetails(val fansChatUser: FansChatUserDetails) : EditProfileViewState()
        data class LoadingState(val isLoading: Boolean, val hideButton: Boolean = false) :
            EditProfileViewState()

        object Logout : EditProfileViewState()
        data class DisplayFriendsBadgeCount(val showBadge: Boolean) : EditProfileViewState()
        data class ToggleNotificationMessage(
            val notificationSettings: NotificationSettings,
            val message: String?
        ) : EditProfileViewState()
    }
}
