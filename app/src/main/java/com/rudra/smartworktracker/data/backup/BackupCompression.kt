package com.rudra.smartworktracker.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object BackupCompression {
    private const val BUFFER_SIZE = 8192

    fun compress(input: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(input.size)
        GZIPOutputStream(bos, BUFFER_SIZE).use { gzip ->
            gzip.write(input)
        }
        return bos.toByteArray()
    }

    fun decompress(input: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(input.size * 2)
        GZIPInputStream(ByteArrayInputStream(input), BUFFER_SIZE).use { gzip ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (gzip.read(buffer).also { bytesRead = it } != -1) {
                bos.write(buffer, 0, bytesRead)
            }
        }
        return bos.toByteArray()
    }

    fun compressStream(input: InputStream, output: OutputStream) {
        GZIPOutputStream(output, BUFFER_SIZE).use { gzip ->
            input.copyTo(gzip, BUFFER_SIZE)
        }
    }

    fun decompressStream(input: InputStream, output: OutputStream) {
        GZIPInputStream(input, BUFFER_SIZE).use { gzip ->
            gzip.copyTo(output, BUFFER_SIZE)
        }
    }
}
