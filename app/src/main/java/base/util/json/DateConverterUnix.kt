package base.util.json

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter
import java.util.*

class DateConverterUnix : Converter<Double, Date> {

    override fun convert(item: Double): Date {
        return if (item.toLong().toString().length < 12) {
            Date(item.toLong() * 1000L)
        } else {
            Date(item.toLong())
        }
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Double::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Date::class.java)
    }
}