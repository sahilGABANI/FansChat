package base.util.json

import base.data.model.feed.FeedItem.Type
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class CountConverter : Converter<Any, Int> {

    override fun convert(item: Any): Int {
        return when (item) {
            is Int -> item
            is Long -> item.toInt()
            is Double -> item.toInt()
            is String -> item.substring(0, 1).toInt() // workaround for malformed types like "0facebook"
            else -> 0
        }
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Any::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Type::class.java)
    }
}