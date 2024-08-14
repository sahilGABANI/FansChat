package base.util.json

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class DateConverterString : Converter<String, Date> {

    override fun convert(item: String): Date {
        var formatter: SimpleDateFormat
        return try {
            formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", getDefault())
            formatter.parse(item)
        } catch (e: ParseException) {
            formatter = SimpleDateFormat("yyyy-MM-dd", getDefault())
            formatter.parse(item)
        }
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(String::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Date::class.java)
    }
}