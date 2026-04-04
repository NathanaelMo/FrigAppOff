package com.monnier.frigapp.generate.infrastructure

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.net.URI

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class BrandString

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class IntOrObject

class IntOrObjectAdapter {

    @FromJson
    @IntOrObject
    fun fromJson(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Int>()
                null
            }
            JsonReader.Token.NUMBER -> reader.nextInt()
            JsonReader.Token.BEGIN_OBJECT -> {
                var value: Int? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "value", "days", "count" -> value = if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<Int>()
                            null
                        } else {
                            reader.nextInt()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                value
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @IntOrObject value: Int?) {
        writer.value(value)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class NullableUri

class NullableUriAdapter {

    @FromJson
    @NullableUri
    fun fromJson(reader: JsonReader): URI? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<URI>()
                null
            }
            JsonReader.Token.STRING -> {
                val s = reader.nextString()
                if (s.isNullOrBlank()) null else URI.create(s)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.hasNext()) { reader.nextName(); reader.skipValue() }
                reader.endObject()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @NullableUri value: URI?) {
        writer.value(value?.toString())
    }
}

class BrandAdapter {

    @FromJson
    @BrandString
    fun fromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<String>()
                null
            }
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.BEGIN_OBJECT -> {
                var name: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "name" -> name = if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<String>()
                            null
                        } else {
                            reader.nextString()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                name
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @BrandString value: String?) {
        writer.value(value)
    }
}
