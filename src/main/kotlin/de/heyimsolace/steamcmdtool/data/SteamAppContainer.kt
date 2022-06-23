package de.heyimsolace.steamcmdtool.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SteamAppContainer (var applist: Applist) {
    data class Applist (var apps: Array<App>) {
        data class App(var appid: Integer, var name: String) {
        }
    }

    companion object {
        @JvmStatic
        var container: SteamAppContainer? = null

        @JvmStatic
        fun buildContainer(): SteamAppContainer? {
            if (container == null) {
                var json = Configurator.getConfigurator().loadSteamAppContainerJson()
                val type = object: TypeToken<SteamAppContainer>() {}.type
                container = Gson().fromJson<SteamAppContainer>(json, type)
            }
            return container
        }
    }
}