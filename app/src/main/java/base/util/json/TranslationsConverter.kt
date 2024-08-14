package base.util.json

import base.data.model.other.Translation
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.Converter

class TranslationsConverter : Converter<Map<String, Translation>, ArrayList<Translation>> {

    override fun convert(item: Map<String, Translation>): ArrayList<Translation> {
        val translations = ArrayList<Translation>()
        for ((language, translation) in item) {
            translations.add(translation.apply { this.language = language })
        }
        return translations
    }

    override fun getInputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(Map::class.java)
    }

    override fun getOutputType(typeFactory: TypeFactory): JavaType {
        return typeFactory.constructType(ArrayList::class.java)
    }
}