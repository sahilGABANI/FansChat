package base.util.json

import base.data.model.feed.FeedItem.Type
import base.util.fixTypeNamingInconsistency
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class ItemTypeConverter : Converter<Any, Type> {

    override fun convert(item: Any): Type {
        return try {
            val typeIndex = when (item) {
                is Int -> item
                is Long -> item.toInt()
                is Double -> item.toInt()
                else -> item as Long
            }

            Type.values()[typeIndex.toInt() - 1]

        } catch (e: ClassCastException) {

            val typeKey = (item as String).fixTypeNamingInconsistency()

            Type.valueOf(typeKey)
        }
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Any::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Type::class.java)
    }
}