package de.heyimsolace.steamcmdtool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

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
            if (SteamAppContainer.container == null) {
                var json = Configurator.getConfigurator().loadSteamAppContainerJson()
                val type = object: TypeToken<SteamAppContainer>() {}.type
                SteamAppContainer.container = Gson().fromJson<SteamAppContainer>(json, type)
            }
            return SteamAppContainer.container
        }
    }
}