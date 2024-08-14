package base.ui.fragment.login.viewmodel

import base.data.api.authentication.AuthenticationRepository
import base.data.api.authentication.model.RequestPasswordRequest
import base.data.api.authentication.model.RequestPasswordTokenRequest
import base.data.network.model.ErrorResult
import base.extension.subscribeWithErrorPars
import base.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class RestorePasswordViewModel(
    private val authenticationRepository: AuthenticationRepository,
) : BaseViewModel() {

    private val resetPasswordStateSubject: PublishSubject<ResetPasswordViewState> =
        PublishSubject.create()
    val resetPasswordState: Observable<ResetPasswordViewState> = resetPasswordStateSubject.hide()

    fun resetPassword(requestPasswordRequest: RequestPasswordRequest) {
        authenticationRepository.resetPassword(requestPasswordRequest)
            .doOnSubscribe {
                resetPasswordStateSubject.onNext(ResetPasswordViewState.LoadingState(true))
            }
            .doAfterTerminate {
                resetPasswordStateSubject.onNext(ResetPasswordViewState.LoadingState(false))
            }
            .subscribeWithErrorPars({
                Timber.i(it.toString())
                resetPasswordStateSubject.onNext(ResetPasswordViewState.SuccessMessage(it.toString()))
            }, {
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        resetPasswordStateSubject.onNext(ResetPasswordViewState.ErrorMessage(it.errorMessage))
                    }
                    is ErrorResult.ErrorThrowable -> {
                        Timber.e(it.throwable)
                    }
                }
            }).autoDispose()
    }

    fun resetPasswordToken(
        token: String,
        requestPasswordTokenRequest: RequestPasswordTokenRequest
    ) {
        authenticationRepository.resetPasswordToken(token, requestPasswordTokenRequest)
            .doOnSubscribe {
                resetPasswordStateSubject.onNext(ResetPasswordViewState.LoadingState(true))
            }
            .doAfterTerminate {
                resetPasswordStateSubject.onNext(ResetPasswordViewState.LoadingState(false))
            }
            .subscribeWithErrorPars({
                Timber.i(it.toString())
                resetPasswordStateSubject.onNext(ResetPasswordViewState.SuccessMessage(it.toString()))
            }, {
                when (it) {
                    is ErrorResult.ErrorMessage -> {
                        resetPasswordStateSubject.onNext(ResetPasswordViewState.ErrorMessage(it.errorMessage))
                    }
                    is ErrorResult.ErrorThrowable -> {
                        Timber.e(it.throwable)
                    }
                }
            }).autoDispose()
    }
}

sealed class ResetPasswordViewState {
    data class ErrorMessage(val errorMessage: String) : ResetPasswordViewState()
    data class SuccessMessage(val successMessage: String) : ResetPasswordViewState()
    data class LoadingState(val isLoading: Boolean) : ResetPasswordViewState()
}