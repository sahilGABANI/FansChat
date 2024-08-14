package base.ui.fragment.login.viewmodel

import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.model.FacebookLoginRequest
import base.data.api.authentication.model.RegisterRequest
import base.data.model.NotificationSettings
import base.data.network.model.ErrorResult
import base.extension.subscribeOnIoAndObserveOnMainThread
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class RegisterViewModel(
    private val authenticationRepository: AuthenticationRepository,
) : BaseViewModel() {

    private val registerStateSubject: PublishSubject<RegisterViewState> = PublishSubject.create()
    val registerState: Observable<RegisterViewState> = registerStateSubject.hide()

    fun register(registerRequest: RegisterRequest) {
        authenticationRepository.registerUser(registerRequest)
            .doOnSubscribe {
                registerStateSubject.onNext(RegisterViewState.LoadingState(true, hideButton = true))
            }.subscribeWithErrorPars({
                Timber.i(it.toString())
                registerStateSubject.onNext(
                    RegisterViewState.LoadingState(
                        false,
                        hideButton = true
                    )
                )
                registerStateSubject.onNext(RegisterViewState.SuccessMessage(it.toString()))
            }, {
                registerStateSubject.onNext(
                    RegisterViewState.LoadingState(
                        false,
                        hideButton = false
                    )
                )
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        registerStateSubject.onNext(RegisterViewState.ErrorMessage(it.errorMessage))
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
                registerStateSubject.onNext(RegisterViewState.LoadingState(true, hideButton = true))
            }.subscribeWithErrorPars({
                Timber.i(it.toString())
                registerStateSubject.onNext(
                    RegisterViewState.LoadingState(
                        false,
                        hideButton = true
                    )
                )
                registerStateSubject.onNext(RegisterViewState.SuccessMessage(it.toString()))
            }, {
                registerStateSubject.onNext(
                    RegisterViewState.LoadingState(
                        false,
                        hideButton = false
                    )
                )
                Timber.e(it.toString())
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        registerStateSubject.onNext(
                            RegisterViewState.ErrorMessage(
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
            registerStateSubject.onNext(RegisterViewState.LoadingState(true, hideButton = true))
        }.doAfterTerminate {
            registerStateSubject.onNext(RegisterViewState.LoadingState(false, hideButton = false))
        }.subscribeOnIoAndObserveOnMainThread({
            registerStateSubject.onNext(RegisterViewState.ToggleNotificationMessage(it))
        }, {
            Timber.e(it)
            //consider registered in user does not matter if this api fails
            registerStateSubject.onNext(RegisterViewState.ToggleNotificationMessage(null))
        }).autoDispose()
    }
}

sealed class RegisterViewState {
    data class ErrorMessage(val errorMessage: String) : RegisterViewState()
    data class SuccessMessage(val successMessage: String) : RegisterViewState()
    data class LoadingState(val isLoading: Boolean, val hideButton: Boolean = false) :
        RegisterViewState()

    data class ToggleNotificationMessage(val notificationSettings: NotificationSettings?) :
        RegisterViewState()
}