package com.linagora.android.linshare.domain

import com.linagora.android.linshare.domain.model.document.DocumentRequest
import okhttp3.MediaType
import java.io.File

object DomainFixtures {

    private const val TEST_FILE_NAME = "test.txt"

    private const val BIG_SIZE_TEST_FILE_NAME = "image_test.jpeg"

    private val FILE = File(ClassLoader.getSystemResource(TEST_FILE_NAME).file)

    private val BIG_SIZE_FILE = File(ClassLoader.getSystemResource(BIG_SIZE_TEST_FILE_NAME).file)

    val DOCUMENT_REQUEST = DocumentRequest(
        file = FILE,
        uploadFileName = "document.txt",
        mediaType = MediaType.get("text/plain")
    )

    val DOCUMENT_REQUEST_BIG_SIZE = DocumentRequest(
        file = BIG_SIZE_FILE,
        uploadFileName = "big_size.jpeg",
        mediaType = MediaType.get("image/jpeg")
    )

    val FILE_SIZE = FILE.length()

    val BIG_FILE_SIZE = BIG_SIZE_FILE.length()
}
