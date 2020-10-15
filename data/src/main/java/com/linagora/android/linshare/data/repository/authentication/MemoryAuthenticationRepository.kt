/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 *
 * Copyright (C) 2020 LINAGORA
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version,
 * provided you comply with the Additional Terms applicable for LinShare software by
 * Linagora pursuant to Section 7 of the GNU Affero General Public License,
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain the
 * display in the interface of the “LinShare™” trademark/logo, the "Libre & Free" mention,
 * the words “You are using the Free and Open Source version of LinShare™, powered by
 * Linagora © 2009–2020. Contribute to Linshare R&D by subscribing to an Enterprise
 * offer!”. You must also retain the latter notice in all asynchronous messages such as
 * e-mails sent with the Program, (ii) retain all hypertext links between LinShare and
 * http://www.linshare.org, between linagora.com and Linagora, and (iii) refrain from
 * infringing Linagora intellectual property rights over its trademarks and commercial
 * brands. Other Additional Terms apply, see
 * <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf>
 * for more details.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 * You should have received a copy of the GNU Affero General Public License and its
 * applicable Additional Terms for LinShare along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General Public License version
 *  3 and <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf> for
 *  the Additional Terms applicable to LinShare software.
 */

package com.linagora.android.linshare.data.repository.authentication

import com.linagora.android.linshare.domain.model.Credential
import com.linagora.android.linshare.domain.model.Password
import com.linagora.android.linshare.domain.model.Token
import com.linagora.android.linshare.domain.model.Username
import com.linagora.android.linshare.domain.network.SupportVersion
import com.linagora.android.linshare.domain.repository.authentication.AuthenticationRepository
import com.linagora.android.linshare.domain.usecases.auth.AuthenticationException
import com.linagora.android.linshare.domain.usecases.auth.AuthenticationException.Companion.WRONG_CREDENTIAL
import com.linagora.android.linshare.domain.usecases.auth.AuthenticationException.Companion.WRONG_PASSWORD
import com.linagora.android.linshare.domain.usecases.auth.BadCredentials
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

class MemoryAuthenticationRepository(
    predefinedCredential: Credential,
    predefinedPassword: Password,
    token: Token
) : AuthenticationRepository {

    private val authenticationStore: AtomicReference<Triple<Credential, Password, Token>> =
        AtomicReference(Triple(predefinedCredential, predefinedPassword, token))

    override suspend fun retrievePermanentToken(baseUrl: URL, supportVersion: SupportVersion, username: Username, password: Password): Token {
        return validateCredential(Credential(baseUrl, supportVersion, username)) { authenticationStore.get().first == it }
            .run { validatePassword(password) { authenticationStore.get().second == it } }
            .let { authenticationStore.get().third }
    }

    @Throws(AuthenticationException::class)
    private fun validateCredential(credential: Credential, predicate: (Credential) -> Boolean): Credential {
        return credential.takeIf(predicate) ?: throw BadCredentials(WRONG_CREDENTIAL)
    }

    @Throws(AuthenticationException::class)
    private fun validatePassword(password: Password, predicate: (Password) -> Boolean): Password {
        return password.takeIf(predicate) ?: throw BadCredentials(WRONG_PASSWORD)
    }

    override suspend fun deletePermanentToken(token: Token): Boolean {
        return authenticationStore.get().takeIf { it.third == token }
            ?.let {
                authenticationStore.set(null)
                true
            }
            ?: true
    }
}
