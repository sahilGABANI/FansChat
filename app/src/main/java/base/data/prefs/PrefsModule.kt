package base.data.prefs

import android.content.Context
import base.data.prefs.LocalPrefs
import dagger.Module
import dagger.Provides

@Module
class PrefsModule {

    @Provides
    fun provideLocalPrefs(context: Context): LocalPrefs {
        return LocalPrefs(context)
    }
}