package lab06.data

import AppContainer
import AppDataContainer
import android.app.Application

class TodoApplication: Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this.applicationContext)
    }
}