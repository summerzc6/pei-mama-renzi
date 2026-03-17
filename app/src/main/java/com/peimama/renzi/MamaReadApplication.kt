package com.peimama.renzi

import android.app.Application
import com.peimama.renzi.di.AppContainer

class MamaReadApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
