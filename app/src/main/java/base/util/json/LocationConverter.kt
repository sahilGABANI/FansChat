package base.util.json

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter
import java.util.*

class LocationConverter : Converter<Map<String, String>, String> {

    override fun convert(item: Map<String, String>): String {
        return item["city"] + ", " + item["country"]
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(HashMap::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(String::class.java)
    }
}