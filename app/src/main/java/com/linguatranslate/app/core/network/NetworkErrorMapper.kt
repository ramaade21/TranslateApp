package com.linguatranslate.app.core.network

import com.linguatranslate.app.core.common.AppError
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Central place that turns network/IO failures into user-friendly
 * [AppError]s. Nothing above this layer should ever see a raw
 * exception or stack trace (requirement #21).
 */
object NetworkErrorMapper {

    fun fromThrowable(t: Throwable): AppError = when (t) {
        is UnknownHostException -> AppError.NoInternet
        is SocketTimeoutException -> AppError.Timeout
        is IOException -> AppError.NoInternet
        else -> AppError.Unknown(t.message ?: "An unexpected error occurred.")
    }

    fun <T> fromHttpResponse(response: Response<T>): AppError {
        return when (response.code()) {
            400, 422 -> AppError.Http(response.code(), extractMessage(response) ?: "Invalid request.")
            429 -> AppError.Http(response.code(), "Too many requests. Please slow down.")
            in 500..599 -> AppError.ProviderUnavailable
            else -> AppError.Http(response.code(), extractMessage(response) ?: "Something went wrong.")
        }
    }

    private fun <T> extractMessage(response: Response<T>): String? =
        response.errorBody()?.string()?.let { body ->
            Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        }
}
