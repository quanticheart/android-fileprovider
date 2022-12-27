@file:Suppress("unused")

package com.quanticheart.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*


class ResourceFileProvider private constructor(
    private val activity: Activity,
    private val directory: String,
    private val fileName: String,
    private val fileExtension: String,
    private val fileType: String
) {
    /**
     * Given the [Builder] parameters, retrieves the file. Automatically starts a "share" Intent.
     *
     * @throws FileNotFoundException if can't find the file.
     */
    @Throws(FileNotFoundException::class)
    fun shareFile() {
        val resId = activity.resources.getIdentifier(fileName, directory, activity.packageName)
        var inputStream: InputStream? = null
        if (isValidResId(resId)) {
            when (directory) {
                FOLDER_RAW -> inputStream = activity.resources.openRawResource(resId)
                FOLDER_MIPMAP, FOLDER_DRAWABLE -> {
                    val fileUri: Uri? = try {
                        Uri.parse(String.format(ANDROID_RES_URI, activity.packageName, resId))
                    } catch (e: IllegalFormatException) {
                        throw FileNotFoundException()
                    }
                    if (fileUri != null) {
                        inputStream = activity.contentResolver.openInputStream(fileUri)
                    }
                }
                else -> inputStream = activity.resources.openRawResource(resId)
            }
            prepareAndShare(inputStream)
        } else {
            if (directory == FOLDER_ASSETS) {
                val assetManager = activity.assets
                inputStream = try {
                    assetManager.open("$fileName.$fileExtension")
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw FileNotFoundException()
                }
                prepareAndShare(inputStream)
            } else {
                throw FileNotFoundException()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    private fun prepareAndShare(inputStream: InputStream?) {
        try {
            val buff = ByteArray(1024)
            var len: Int
            val outputStream =
                activity.openFileOutput("$fileName.$fileExtension", Context.MODE_PRIVATE)
            if (inputStream != null) {
                while (inputStream.read(buff).also { len = it } > 0) {
                    outputStream.write(buff, 0, len)
                }
                inputStream.close()
            }
            outputStream.close()
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage ?: "Error")
        }
        val uri = FileProvider.getUriForFile(
            activity, activity.resources.getString(R.string.file_provider_authorities), File(
                activity.filesDir, "$fileName.$fileExtension"
            )
        )

        val intent = ShareCompat.IntentBuilder(activity).intent
        intent.action = Intent.ACTION_SEND
        intent.type = fileType
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    private fun isValidResId(resId: Int): Boolean {
        return resId != 0
    }

    /**
     * Builder class: the entry point of this tool.
     */
    class Builder private constructor(private val activity: Activity) {
        private var directory: String? = null
        private var fileName: String? = null
        private var fileExtension: String? = null
        private var fileType: String? = null

        /**
         * Set the directory where the file is stored.
         *
         * @param directory a String representing the directory. Consider using one of the following
         * [ResourceFileProvider.FOLDER_ASSETS]
         * [ResourceFileProvider.FOLDER_RAW]
         * [ResourceFileProvider.FOLDER_DRAWABLE]
         * @return the Builder itself.
         */
        fun setDirectory(directory: String): Builder {
            this.directory = directory
            return this
        }

        /**
         * Set the file name.
         *
         * @param fileName the filename as a String, WITHOUT any extension, see [Builder.setFileExtension].
         * @return the Builder itself.
         */
        fun setFileName(fileName: String): Builder {
            this.fileName = fileName
            return this
        }

        /**
         * Set the file extension.
         *
         * @param fileExtension the extension as a String, WITHOUT the point.
         * @return the Builder itself.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun setFileExtension(fileExtension: String): Builder {
            this.fileExtension = fileExtension
            return this
        }

        /**
         * Set the file type. Used for the "share" intent.
         *
         * @param fileType the file type as a String (MIME type). Consider using one of the following
         * [ResourceFileProvider.TYPE_AUDIO]
         * [ResourceFileProvider.TYPE_VIDEO]
         * [ResourceFileProvider.TYPE_IMAGE]
         * [ResourceFileProvider.TYPE_PDF]
         * @return the Builder itself.
         */
        fun setFileType(fileType: String): Builder {
            this.fileType = fileType
            return this
        }

        /**
         * Builds the ResourceFileProvider.
         *
         * @return the ResourceFileProvider.
         */
        fun build(): ResourceFileProvider {
            return ResourceFileProvider(
                activity,
                directory!!,
                fileName!!,
                fileExtension!!,
                fileType!!
            )
        }

        companion object {
            /**
             * @param activity a valid Activity used as Context and to start the "share" Intent
             * @return the Builder itself.
             */
            fun from(activity: Activity): Builder {
                return Builder(activity)
            }
        }
    }

    companion object {
        private const val TAG = "ResourceFileProvider"
        private const val ANDROID_RES_URI = "android.resource://%1\$s/%2\$s"
        const val FOLDER_RAW = "raw"
        const val FOLDER_DRAWABLE = "drawable"
        const val FOLDER_MIPMAP = "mipmap"
        const val FOLDER_ASSETS = "assets"
        const val TYPE_AUDIO = "audio/*"
        const val TYPE_IMAGE = "image/*"
        const val TYPE_VIDEO = "video/*"
        const val TYPE_PDF = "application/pdf"
    }
}

fun Activity.testShare() {
    try {
        ResourceFileProvider.Builder
            .from(this)
            .setDirectory(ResourceFileProvider.FOLDER_RAW)
            .setFileName("my_sound")
            .setFileExtension("mp3")
            .setFileType(ResourceFileProvider.TYPE_AUDIO)
            .build()
            .shareFile()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
}