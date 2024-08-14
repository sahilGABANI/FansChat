package base.data.api.file_upload

import base.util.msg
import com.amplifyframework.rx.RxAmplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.result.StorageTransferProgress
import com.amplifyframework.storage.result.StorageUploadFileResult
import io.reactivex.Single
import timber.log.Timber
import java.io.File

class FileUploadDetail {

    private val baseUrl = "https://fanschat1mtn1wamplify164446-dev.s3.eu-central-1.amazonaws.com/public/"

    fun amplifyFileUpload(file: File): Single<String> {
        return Single.create { emitter ->
            val options = StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build()
            val fileName = file.name.replace(" ", "_")
            val upload = RxAmplify.Storage.uploadFile(fileName, file, options)

            upload
                .observeProgress()
                .subscribe { progress: StorageTransferProgress ->
                    Timber.i(
                        progress.fractionCompleted.toString()
                    )
                }
            upload.observeResult()
                .subscribe({ result: StorageUploadFileResult ->
                    Timber.i("Successfully uploaded: https://fanschat1mtn1wamplify164446-dev.s3.eu-central-1.amazonaws.com/public/${fileName}")
                    Timber.i("Successfully uploaded: $result")
                    emitter.onSuccess(baseUrl + fileName)
                }
                ) { error: Throwable? ->
                    Timber.e("Upload failed $error")
                    emitter.onError(Throwable(error?.msg))
                }
        }
    }
}
