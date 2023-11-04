package com.dulziz.submission_storyapp.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

private const val CUSTOM_FILENAME_FORMAT = "dd-MMM-yyyy"

val customTimestamp: String = SimpleDateFormat(
    CUSTOM_FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File {
    val customStorageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(customTimestamp, ".jpg", customStorageDir)
}

fun convertUriToFile(selectedImageUri: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val outputFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImageUri) as InputStream
    val outputStream: OutputStream = FileOutputStream(outputFile)
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } > 0) outputStream.write(buffer, 0, bytesRead)
    outputStream.close()
    inputStream.close()

    return outputFile
}

fun compressCustomImage(inputFile: File): File {
    val bitmap = BitmapFactory.decodeFile(inputFile.path)

    var compressionQuality = 90
    var byteArrayLength: Int

    do {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)
        val compressedByteArray = outputStream.toByteArray()
        byteArrayLength = compressedByteArray.size
        compressionQuality -= 2
    } while (byteArrayLength > 1000000)

    bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, FileOutputStream(inputFile))

    return inputFile
}