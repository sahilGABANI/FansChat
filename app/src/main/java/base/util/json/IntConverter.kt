package base.util.json

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class IntConverter : Converter<Double, Int> {

    override fun convert(item: Double): Int {
        return item.toInt()
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Double::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Int::class.java)
    }
}