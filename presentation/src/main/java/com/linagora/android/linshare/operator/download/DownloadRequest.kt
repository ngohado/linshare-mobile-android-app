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

package com.linagora.android.linshare.operator.download

import com.linagora.android.linshare.domain.model.document.Document
import com.linagora.android.linshare.domain.model.download.DownloadType
import com.linagora.android.linshare.domain.model.share.Share
import com.linagora.android.linshare.domain.model.sharedspace.WorkGroupDocument
import com.linagora.android.linshare.domain.network.Endpoint
import com.linagora.android.linshare.domain.network.ServicePath
import okhttp3.MediaType
import java.util.UUID

data class DownloadRequest(
    val downloadName: String,
    val downloadSize: Long,
    val downloadMediaType: MediaType,
    val downloadDataId: UUID,
    val downloadType: DownloadType,
    val sharedSpaceId: UUID? = null
)

fun Document.toDownloadRequest(): DownloadRequest {
    return DownloadRequest(
        downloadName = name,
        downloadSize = size,
        downloadMediaType = type,
        downloadDataId = documentId.uuid,
        downloadType = DownloadType.DOCUMENT
    )
}

fun Share.toDownloadRequest(): DownloadRequest {
    return DownloadRequest(
        downloadName = name,
        downloadSize = size,
        downloadMediaType = type,
        downloadDataId = shareId.uuid,
        downloadType = DownloadType.SHARE
    )
}

fun WorkGroupDocument.toDownloadRequest(): DownloadRequest {
    return DownloadRequest(
        downloadName = name,
        downloadSize = size,
        downloadMediaType = mimeType,
        downloadDataId = workGroupNodeId.uuid,
        downloadType = DownloadType.SHARED_SPACE_DOCUMENT,
        sharedSpaceId = sharedSpaceId.uuid
    )
}

fun DownloadRequest.toServicePath(): ServicePath {
    val path = when (downloadType) {
        DownloadType.DOCUMENT -> Endpoint.DOCUMENT_PATH
        DownloadType.SHARE -> Endpoint.RECEIVED_SHARES_PATH
        DownloadType.SHARED_SPACE_DOCUMENT -> {
            require(sharedSpaceId != null) { "Can not download shared space document without sharedSpaceId" }
            "${Endpoint.SHARED_SPACE_PATH}/$sharedSpaceId/${Endpoint.NODES}"
        }
    }
    return ServicePath("$path/$downloadDataId/${Endpoint.DOWNLOAD}")
}
