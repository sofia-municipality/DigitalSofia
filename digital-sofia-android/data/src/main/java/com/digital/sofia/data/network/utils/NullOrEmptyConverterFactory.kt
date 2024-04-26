package com.digital.sofia.data.network.utils

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NullOrEmptyConverterFactory: Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ) = object : Converter<ResponseBody, Any?> {
        val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(this@NullOrEmptyConverterFactory, type, annotations)

        override fun convert(value: ResponseBody): Any? {
            return if (value.contentLength() != 0L) {
                nextResponseBodyConverter.convert(value)
            } else null
        }
    }
}