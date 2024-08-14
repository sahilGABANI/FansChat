package base.data.network

import base.data.network.model.FansChatError
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.io.IOException
import kotlin.reflect.KClass

open class NetworkException(
    val error: Any? = null,
    message: String? = null,
    val exception: Throwable
) : RuntimeException(message, exception) {

    companion object {

        private fun parseError(
            retrofit: Retrofit,
            httpException: HttpException,
            kClass: KClass<*>
        ): Any? {
            if (httpException.response()?.isSuccessful == true) {
                return null
            }
            val errorBody = httpException.response()?.errorBody() ?: return null
            val converter: Converter<ResponseBody, Any> =
                retrofit.responseBodyConverter(kClass.java, arrayOf())
            return converter.convert(errorBody)
        }

//        fun asRetrofitException(
//            annotations: Array<Annotation>,
//            retrofit: Retrofit,
//            throwable: Throwable
//        ): Throwable {
//            val errorType: ErrorType? = annotations.find { it is ErrorType } as? ErrorType
//            return when (throwable) {
//                is HttpException -> {
//                    return if (errorType != null && throwable.response()?.code() != 401) {
//                        val error = parseError(retrofit, throwable, errorType.type)
//                        HttpCallFailureException(error, throwable.message(), throwable)
//                    } else {
//                        throwable
//                    }
//                }
//                is IOException -> {
//                    NetworkError(
//                        message = throwable.localizedMessage,
//                        exception = throwable
//                    )
//                }
//                else -> throwable
//            }
//        }

        fun asRetrofitException(
            annotations: Array<Annotation>,
            retrofit: Retrofit,
            throwable: Throwable
        ): Throwable {
            val errorType: ErrorType? = annotations.find { it is ErrorType } as? ErrorType

            return if (errorType != null && throwable is HttpException) {
                val error = parseError(retrofit, throwable, errorType.type)
                NetworkException(error, throwable.message(), throwable)
            } else throwable
        }
    }
}

fun Throwable.parseRetrofitException(): FansChatError? {
    if (this is NetworkException && this.error is FansChatError) {
        return this.error
    }
    Timber.e(this)
    return null
}

inline fun <reified T> Throwable.parseRetrofitException(): T? {
    if (this is NetworkException && this.error is T) {
        return this.error
    }
    Timber.e(this)
    return null
}