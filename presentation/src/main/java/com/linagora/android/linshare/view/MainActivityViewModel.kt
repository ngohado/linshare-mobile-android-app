package com.linagora.android.linshare.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linagora.android.linshare.domain.model.Credential
import com.linagora.android.linshare.domain.model.properties.RecentUserPermissionAction
import com.linagora.android.linshare.domain.model.properties.RecentUserPermissionAction.DENIED
import com.linagora.android.linshare.domain.network.manager.AuthorizationManager
import com.linagora.android.linshare.domain.repository.PropertiesRepository
import com.linagora.android.linshare.domain.usecases.auth.AuthenticationViewState
import com.linagora.android.linshare.domain.usecases.auth.GetAuthenticatedInfoInteractor
import com.linagora.android.linshare.domain.usecases.utils.Failure
import com.linagora.android.linshare.domain.usecases.utils.Success
import com.linagora.android.linshare.model.properties.RuntimePermissionRequest
import com.linagora.android.linshare.model.properties.RuntimePermissionRequest.InitialReadStorage
import com.linagora.android.linshare.model.properties.RuntimePermissionRequest.ShouldNotShowReadStorage
import com.linagora.android.linshare.model.properties.RuntimePermissionRequest.ShouldShowReadStorage
import com.linagora.android.linshare.network.DynamicBaseUrlInterceptor
import com.linagora.android.linshare.util.CoroutinesDispatcherProvider
import com.linagora.android.linshare.view.MainActivityViewModel.AuthenticationState.AUTHENTICATED
import com.linagora.android.linshare.view.MainActivityViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.linagora.android.linshare.view.MainActivityViewModel.AuthenticationState.UNAUTHENTICATED
import com.linagora.android.linshare.view.base.BaseViewModel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val getAuthenticatedInfo: GetAuthenticatedInfoInteractor,
    private val dispatcherProvider: CoroutinesDispatcherProvider,
    private val dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
    private val authorizationManager: AuthorizationManager,
    private val propertiesRepository: PropertiesRepository
) : BaseViewModel(dispatcherProvider) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainActivityViewModel::class.java)
    }

    enum class AuthenticationState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        INVALID_AUTHENTICATION
    }

    private val mutableCurrentCredential = MutableLiveData(Credential.InvalidCredential)
    val currentCredential: LiveData<Credential> = mutableCurrentCredential

    private val shouldShowPermissionRequest = MutableLiveData<RuntimePermissionRequest>(InitialReadStorage)
    val shouldShowPermissionRequestState: LiveData<RuntimePermissionRequest> = shouldShowPermissionRequest

    val authenticationState = MutableLiveData<AuthenticationState>()

    init {
        authenticationState.value = UNAUTHENTICATED
    }

    fun checkSignedIn() {
        LOGGER.info("checkSignedIn()")
        dispatchState(INITIAL_STATE)
        viewModelScope.launch(dispatcherProvider.io) {
            consumeStates(getAuthenticatedInfo())
        }
    }

    fun shouldShowReadStoragePermissionRequest(systemRuntimePermissionRequest: RuntimePermissionRequest) {
        shouldShowPermissionRequest.value = InitialReadStorage
        viewModelScope.launch(dispatcherProvider.io) {
            val userStoragePermissionRequest = propertiesRepository.getRecentActionForReadStoragePermission()
            shouldShowPermissionRequest.postValue(
                combineReadStoragePermission(userStoragePermissionRequest, systemRuntimePermissionRequest)
            )
        }
    }

    fun setActionForReadStoragePermissionRequest(recentUserPermissionAction: RecentUserPermissionAction) {
        viewModelScope.launch(dispatcherProvider.io) {
            propertiesRepository.storeRecentActionForReadStoragePermission(recentUserPermissionAction)
        }
    }

    private fun combineReadStoragePermission(
        recentUserPermissionAction: RecentUserPermissionAction,
        systemRuntimePermissionRequest: RuntimePermissionRequest
    ): RuntimePermissionRequest {
        if (recentUserPermissionAction != DENIED) {
            return ShouldShowReadStorage
        }

        if (systemRuntimePermissionRequest == ShouldShowReadStorage) {
            return ShouldShowReadStorage
        }

        return ShouldNotShowReadStorage
    }

    fun setUpAuthenticated(authenticationViewState: AuthenticationViewState) {
        authenticationState.value = AUTHENTICATED
        mutableCurrentCredential.value = authenticationViewState.credential
        setUpInterceptors(authenticationViewState)
    }

    override fun onSuccessDispatched(success: Success) {
        success.takeIf { it is AuthenticationViewState }
            ?.let { setUpAuthenticated(it as AuthenticationViewState) }
    }

    override fun onFailureDispatched(failure: Failure) {
        authenticationState.value = INVALID_AUTHENTICATION
        mutableCurrentCredential.value = Credential.InvalidCredential
    }

    private fun setUpInterceptors(authenticationViewState: AuthenticationViewState) {
        LOGGER.info("setUpInterceptors()")
        dynamicBaseUrlInterceptor.changeBaseUrl(authenticationViewState.credential.serverUrl)
        authorizationManager.updateToken(authenticationViewState.token)
    }
}
