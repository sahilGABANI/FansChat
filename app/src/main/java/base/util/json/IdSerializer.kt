package base.util.json

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class IdSerializer : Converter<String, Map<String, String>?> {

    override fun convert(item: String): Map<String, String>? {
        return if (item.isNotEmpty()) {
            mapOf("\$oid" to item)
        } else null
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(String::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Map::class.java)
    }
}