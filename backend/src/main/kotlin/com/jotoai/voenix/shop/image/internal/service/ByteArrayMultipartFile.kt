package com.jotoai.voenix.shop.image.internal.service

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * A MultipartFile implementation that wraps a byte array.
 * This allows us to use stored image data with APIs that expect MultipartFile.
 */
class ByteArrayMultipartFile(
    private val bytes: ByteArray,
    private val filename: String,
    private val contentType: String,
    private val fieldName: String = "file",
) : MultipartFile {
    override fun getName(): String = fieldName

    override fun getOriginalFilename(): String = filename

    override fun getContentType(): String = contentType

    override fun isEmpty(): Boolean = bytes.isEmpty()

    override fun getSize(): Long = bytes.size.toLong()

    override fun getBytes(): ByteArray = bytes

    override fun getInputStream(): InputStream = ByteArrayInputStream(bytes)

    override fun transferTo(dest: File) {
        FileOutputStream(dest).use { output ->
            output.write(bytes)
        }
    }
}
