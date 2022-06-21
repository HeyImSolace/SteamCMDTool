package de.heyimsolace.steamcmdtool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javafx.scene.control.Alert
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.collections.ArrayList

class Configurator {

    companion object {
        @JvmStatic
        var instance: Configurator? = null

        @JvmStatic
        fun getConfigurator(): Configurator {
            if (instance == null) {
                instance = Configurator();
            }
            return instance as Configurator;
        }
    }
    val configpath = loadConfigpath()
    val configprops = loadConfig()
    val steamcmdPath = loadConfig().getProperty("steamcmdpath")
    val LOGINS = loadLogins();

    private fun loadConfig(): Properties {
        val props = Properties()
        val configFile = File("$configpath/config.properties")
        if (configFile.exists()) {
            println("Config file found")
            props.load(configFile.inputStream())
            println(props["steamcmdpath"])
        } else {
            //create default config
            configFile.createNewFile()
            props.setProperty("steamcmdpath", "PUT YOUR STEAMCMD PATH HERE")
            props.store(configFile.outputStream(), "");
            Alert(Alert.AlertType.INFORMATION).apply {
                title = "SteamCMD Tool"
                headerText = "Config file created"
                contentText = "Please set the steamcmd path in the config file"
            }.showAndWait()
        }
        return props
    }

    private fun loadLogins(): ArrayList<SteamLogin> {
        val configFile = File("$configpath/logins.json")
        val gson = Gson()
        if (configFile.exists()) {
            val type = object : TypeToken<ArrayList<SteamLogin>>() {}.type
            return gson.fromJson(configFile.reader(), type)
        } else {
            //create default config
            configFile.createNewFile()
            val logins = ArrayList<SteamLogin>()
            logins.add(SteamLogin("template", "password"))
            logins.add(SteamLogin("anonymous", ""))
            configFile.writeText(gson.toJson(logins))
            return logins
        }
    }

    private fun loadConfigpath(): String {
        var path = Paths.get("").toAbsolutePath().toString() + "/toolfiles"
        return if (Files.exists(Paths.get(path))) {
            path
        } else {
            Files.createDirectory(Paths.get(path))
            path;
        }

    }


    //Lädt alle SteamApp IDs aus einer JSON oder von der Steam API
    fun loadSteamAppContainerJson(): String {
        val configFile = File("$configpath/steamappcontainer.json")

        val fileExists = Files.exists(Paths.get("$configpath/steamappcontainer.json"))

        if (fileExists){
            var attr = Files.readAttributes(configFile.toPath(), BasicFileAttributes::class.java)
            var filetime = attr.lastModifiedTime()
            val fileOld = filetime.toMillis() < System.currentTimeMillis() - (1000 * 60 * 60 * 24)

            if (fileOld) {
                return rereadJsonFromAPI()
            }
            println("AppIDs aus Datei gelesen!")
            return configFile.readText()
        }
        return rereadJsonFromAPI()

    }

    private fun rereadJsonFromAPI(): String {
        val configFile = File("$configpath/steamappcontainer.json")
        val path: String = "https://api.steampowered.com/ISteamApps/GetAppList/v0002/?key=STEAMKEY&format=json"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(path)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val json = response.body()
        configFile.createNewFile()
        configFile.writeText(json);
        println("AppIDs aus API gelesen!")
        return json;
    }

    public fun addPreset(preset: SteamLogin) {
        val configFile = File("$configpath/logins.json")
        val gson = Gson()
        val logins = loadLogins()

        logins.add(preset)
        configFile.writeText(gson.toJson(logins))
    }

}