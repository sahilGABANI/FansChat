package base.data.api.file_upload

import io.reactivex.Single
import java.io.File

object UploadFile {
    fun upload(file: File): Single<String> {
        return FileUploadDetail().amplifyFileUpload(file)
    }
}