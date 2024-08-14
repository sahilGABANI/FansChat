package base.util.json

import android.content.Context
import base.util.msg
import com.amplifyframework.rx.RxAmplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.result.StorageTransferProgress
import com.amplifyframework.storage.result.StorageUploadFileResult
import io.reactivex.Single
import timber.log.Timber
import java.io.File


class FileUploader(ctx: Context) {
    private val baseUrl =
        "https://fanschat1mtn1wamplify164446-dev.s3.eu-central-1.amazonaws.com/public/"

//    private val poolId = "eu-west-1:8a0d240e-2ebb-4b62-b66b-2288bc88ce1f"
//    private val baseUrl = "https://s3-eu-west-1.amazonaws.com/sskirbucket/"
//    private val bucket = "sskirbucket"
    /* private val poolId = "eu-central-1:d6747f48-d82c-4d2a-bf41-74c897ca6944"
     private val baseUrl = "https://fanschat-sporting.s3.eu-central-1.amazonaws.com/"
     private val bucket = "fanschat-sporting"
     private var transferUtility: TransferUtility*/

/*
    init {
        val credentials = CognitoCachingCredentialsProvider(ctx, poolId, EU_CENTRAL_1)
        val s3 = AmazonS3Client(credentials)
        transferUtility = TransferUtility.builder().s3Client(s3).context(ctx).build()
    }
*/

/*
    fun upload(file: File): Single<String> {
        return Single.create { emitter ->

            val observer = transferUtility.upload(bucket, file.name, file)

            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (COMPLETED == state) {
                        emitter.onSuccess(baseUrl + file.name)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

                override fun onError(id: Int, ex: Exception) {
                    ex.printStackTrace()
                    emitter.onError(Throwable(ex.msg))
                }
            })
        }
    }
*/

    fun amplifyFileUpload(file: File): Single<String> {
        return Single.create { emitter ->
            val options = StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build()

            val upload = RxAmplify.Storage.uploadFile(file.name, file, options)

            upload
                .observeProgress()
                .subscribe { progress: StorageTransferProgress ->
                    Timber.i(
                        progress.fractionCompleted.toString()
                    )
                }
            upload.observeResult()
                .subscribe({ result: StorageUploadFileResult ->
                    Timber.i("Successfully uploaded: https://fanschat1mtn1wamplify164446-dev.s3.eu-central-1.amazonaws.com/public/${file.name}")
                    Timber.i("Successfully uploaded: $result")
                    emitter.onSuccess(baseUrl + file.name)
                }
                ) { error: Throwable? ->
                    Timber.e("Upload failed $error")
                    emitter.onError(Throwable(error?.msg))
                }
        }
    }
}
