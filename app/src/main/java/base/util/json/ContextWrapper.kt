package base.util.json

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.LocaleList
import java.util.*

class ContextWrapper(base: Context?) : android.content.ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale?): ContextWrapper {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            configuration.setLocale(newLocale)
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            }
            context = context.createConfigurationContext(configuration)
            return ContextWrapper(context)
        }
    }
}