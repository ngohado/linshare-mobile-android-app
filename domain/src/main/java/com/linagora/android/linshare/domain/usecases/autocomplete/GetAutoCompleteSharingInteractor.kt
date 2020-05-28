package com.linagora.android.linshare.domain.usecases.autocomplete

import arrow.core.Either
import com.linagora.android.linshare.domain.model.autocomplete.AutoCompletePattern
import com.linagora.android.linshare.domain.model.autocomplete.AutoCompleteResult
import com.linagora.android.linshare.domain.model.autocomplete.AutoCompleteType
import com.linagora.android.linshare.domain.repository.autocomplete.AutoCompleteRepository
import com.linagora.android.linshare.domain.usecases.utils.Failure
import com.linagora.android.linshare.domain.usecases.utils.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAutoCompleteSharingInteractor @Inject constructor(
    private val autoCompleteRepository: AutoCompleteRepository
) {

    operator fun invoke(autoCompletePattern: AutoCompletePattern): Flow<Either<Failure, Success>> {
        return flow<Either<Failure, Success>> {
            emit(Either.right(Success.Loading))

            val autoCompleteState = Either
                .catch { autoCompleteRepository.getAutoComplete(autoCompletePattern, AutoCompleteType.SHARING) }
                .bimap(::AutoCompleteFailure) { getAutoCompleteState(autoCompletePattern, it) }

            emit(autoCompleteState)
        }
    }

    private fun getAutoCompleteState(pattern: AutoCompletePattern, userAutoCompleteResults: List<AutoCompleteResult>?): Success.ViewState {
        return userAutoCompleteResults
            ?.takeIf { it.isNotEmpty() }
            ?.let(::AutoCompleteViewState)
            ?: AutoCompleteNoResult(pattern)
    }
}
