package base.ui.fragment.login.viewmodel

import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.model.FacebookLoginRequest
import base.data.api.authentication.model.LoginRequest
import base.data.model.NotificationSettings
import base.data.network.model.ErrorResult
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class LoginViewModel(
    private val authenticationRepository: AuthenticationRepository,
) : BaseViewModel() {

    private val loginStateSubject: PublishSubject<LoginViewState> = PublishSubject.create()
    val loginState: Observable<LoginViewState> = loginStateSubject.hide()

    fun login(loginRequest: LoginRequest) {
        authenticationRepository.login(loginRequest)
            .doOnSubscribe {
                loginStateSubject.onNext(LoginViewState.LoadingState(true, hideButton = true))
            }
            .subscribeWithErrorPars({
                loginStateSubject.onNext(LoginViewState.LoadingState(false, hideButton = true))
                loginStateSubject.onNext(LoginViewState.SuccessMessage(it.toString()))
            }, {
                loginStateSubject.onNext(LoginViewState.LoadingState(false, hideButton = false))
                Timber.e(it.toString())
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        loginStateSubject.onNext(
                            LoginViewState.ErrorMessage(
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

    fun facebookLogin(facebookLoginRequest: FacebookLoginRequest) {
        authenticationRepository.facebookLoginUser(facebookLoginRequest)
            .doOnSubscribe {
                loginStateSubject.onNext(LoginViewState.LoadingState(true, hideButton = true))
            }
            .subscribeWithErrorPars({
                loginStateSubject.onNext(LoginViewState.LoadingState(false, hideButton = true))
                loginStateSubject.onNext(LoginViewState.SuccessMessage(it.toString()))
            }, {
                loginStateSubject.onNext(LoginViewState.LoadingState(false, hideButton = false))
                Timber.e(it.toString())
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        loginStateSubject.onNext(
                            LoginViewState.ErrorMessage(
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

    fun getNotificationToggles() {
        authenticationRepository.getNotificationToggles().doOnSubscribe {
            loginStateSubject.onNext(LoginViewState.LoadingState(true, hideButton = true))
        }.doAfterTerminate {
            loginStateSubject.onNext(LoginViewState.LoadingState(false, hideButton = false))
        }.subscribeOnIoAndObserveOnMainThread({
            loginStateSubject.onNext(LoginViewState.ToggleNotificationMessage(it))
        }, {
            Timber.e(it)
            //consider logged in user does not matter if this api fails
            loginStateSubject.onNext(LoginViewState.ToggleNotificationMessage(null))
        }).autoDispose()
    }
}

sealed class LoginViewState {
    data class ErrorMessage(val errorMessage: String) : LoginViewState()
    data class SuccessMessage(val successMessage: String) : LoginViewState()
    data class LoadingState(val isLoading: Boolean, val hideButton: Boolean = false) :
        LoginViewState()

    data class ToggleNotificationMessage(val notificationSettings: NotificationSettings?) :
        LoginViewState()
}