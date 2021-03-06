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

package com.linagora.android.linshare.data.datasource.network

import com.linagora.android.linshare.data.api.LinshareApi
import com.linagora.android.linshare.data.datasource.DocumentDataSource
import com.linagora.android.linshare.data.network.NetworkExecutor
import com.linagora.android.linshare.data.network.handler.CopyNetworkRequestHandler
import com.linagora.android.linshare.data.network.handler.UploadNetworkRequestHandler
import com.linagora.android.linshare.domain.model.copy.CopyRequest
import com.linagora.android.linshare.domain.model.document.Document
import com.linagora.android.linshare.domain.model.document.DocumentId
import com.linagora.android.linshare.domain.model.document.DocumentRequest
import com.linagora.android.linshare.domain.model.document.nameContains
import com.linagora.android.linshare.domain.model.document.DocumentRenameRequest
import com.linagora.android.linshare.domain.model.search.QueryString
import com.linagora.android.linshare.domain.model.share.Share
import com.linagora.android.linshare.domain.model.share.ShareRequest
import com.linagora.android.linshare.domain.model.sharedspace.PartParameter.FILE_PARAMETER_FIELD
import com.linagora.android.linshare.domain.model.upload.OnTransfer
import com.linagora.android.linshare.domain.usecases.remove.RemoveDocumentException
import okhttp3.MultipartBody
import org.slf4j.LoggerFactory
import javax.inject.Inject

class LinShareDocumentDataSource @Inject constructor(
    private val linshareApi: LinshareApi,
    private val networkExecutor: NetworkExecutor,
    private val uploadNetworkRequestHandler: UploadNetworkRequestHandler,
    private val copyNetworkRequestHandler: CopyNetworkRequestHandler
) : DocumentDataSource {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LinShareDocumentDataSource::class.java)
    }

    override suspend fun upload(
        documentRequest: DocumentRequest,
        onTransfer: OnTransfer
    ): Document {
        return networkExecutor.execute(
            networkRequest = { uploadToMySpace(documentRequest, onTransfer) },
            onFailure = { uploadNetworkRequestHandler(it) }
        )
    }

    private suspend fun uploadToMySpace(
        documentRequest: DocumentRequest,
        onTransfer: OnTransfer
    ): Document {
        val fileRequestBody = documentRequest.toMeasureRequestBody(onTransfer)
        return linshareApi.upload(
            file = MultipartBody.Part.createFormData(
                FILE_PARAMETER_FIELD,
                documentRequest.uploadFileName,
                fileRequestBody),
            fileSize = documentRequest.file.length()
        )
    }

    override suspend fun remove(documentId: DocumentId): Document {
        try {
            return linshareApi.removeDocument(documentId.uuid.toString())
        } catch (throwable: Throwable) {
            LOGGER.error("remove() ${throwable.printStackTrace()}")
            throw RemoveDocumentException(throwable)
        }
    }

    override suspend fun getAll(): List<Document> {
        return linshareApi.getAll()
    }

    override suspend fun get(documentId: DocumentId): Document {
        return linshareApi.getDocument(documentId.uuid.toString())
    }

    override suspend fun search(query: QueryString): List<Document> {
        return getAll()
            .filter { document -> document.nameContains(query.value) }
    }

    override suspend fun share(shareRequest: ShareRequest): List<Share> {
        return linshareApi.share(shareRequest)
    }

    override suspend fun copy(copyRequest: CopyRequest): List<Document> {
        return networkExecutor.execute(
            networkRequest = { linshareApi.copyInMySpace(copyRequest) },
            onFailure = { copyNetworkRequestHandler(it) })
    }

    override suspend fun renameDocument(documentId: DocumentId, documentRenameRequest: DocumentRenameRequest): Document {
        return linshareApi.renameDocument(documentId.uuid.toString(), documentRenameRequest)
    }
}
