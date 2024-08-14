package base.util.json

import base.data.model.feed.FeedItem.Type
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class ItemTypeSerializer : Converter<Type, Any> {

    override fun convert(item: Type): Any {
        return item.ordinal + 1
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Type::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Any::class.java)
    }
}