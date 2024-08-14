package base.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import timber.log.Timber
import java.io.*


object FileUtils {

    fun getPath(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) return Environment.getExternalStorageDirectory()
                    .toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                /* val id = DocumentsContract.getDocumentId(uri)
                 val contentUri = ContentUris.withAppendedId(
                     Uri.parse("content://downloads/public_downloads"),
                     java.lang.Long.valueOf(id)
                 )
                 return getDataColumn(context, contentUri, null, null)*/

                val fileNameFile: String? = getFileName(context, uri)
                if (fileNameFile != null) {
                    val nFile = File(Environment.getExternalStorageDirectory().toString() + "/Download/" + fileNameFile)
                    if (nFile.exists()) {
                        return nFile.absolutePath
                    }
                }
                val id = DocumentsContract.getDocumentId(uri)
                var returnPath: String? = null
                if (id != null) {
                    if (id.startsWith("raw:")) {
                        returnPath = id.substring(4)
                    } else if (id.contains("/")) {
                        returnPath = if (id.startsWith("/")) {
                            id
                        } else {
                            id.substring(id.indexOf("/"))
                        }
                    } else {
                        val contentUriPrefixesToTry = arrayOf("content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads")
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            val contentUri =
                                ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))
                            try {
                                val path: String? = getDataColumn(context, contentUri, null, null)
                                if (path != null) {
                                    returnPath = path
                                    break
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (returnPath == null) {
                        // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                        val fileName: String? = getFileName(context, uri)
                        val cacheDir: File? = getDocumentCacheDir(context)
                        val file: File? = generateFileName(fileName, cacheDir)
                        if (file != null) {
                            returnPath = file.absolutePath
                            saveFileFromUri(context, uri, returnPath)
                        }
                    }
                }
                return returnPath
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    "document" -> contentUri = MediaStore.Files.getContentUri("external")
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            `is`!!.read(buf)
            do {
                bos.write(buf)
            } while (`is`.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun generateFileName(name: String?, directory: File?): File? {
        var name = name ?: return null
        var file = File(directory, name)
        if (file.exists()) {
            var fileName = name
            var extension = ""
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex)
                extension = name.substring(dotIndex)
            }
            var index = 0
            while (file.exists()) {
                index++
                name = "$fileName($index)$extension"
                file = File(directory, name)
            }
        }
        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            Timber.tag(">").w(e)
            return null
        }
        return file
    }

    private fun getFileName(context: Context, fileUri: Uri): String? {
        var fileName: String? = null
        try {
            val returnCursor = context.contentResolver.query(fileUri, null, null, null, null)!!
            returnCursor.moveToFirst()
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            fileName = returnCursor.getString(nameIndex)
            Timber.tag("Name").d(returnCursor.getString(nameIndex))
            Timber.tag("size").d(returnCursor.getLong(sizeIndex).toString())
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        return fileName
    }

    const val DOCUMENTS_DIR = "documents"

    private fun getDocumentCacheDir(context: Context): File? {
        val dir = File(context.cacheDir, DOCUMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}