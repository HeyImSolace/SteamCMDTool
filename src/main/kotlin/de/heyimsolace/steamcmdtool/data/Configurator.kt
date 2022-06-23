package de.heyimsolace.steamcmdtool.data

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


class Configurator private constructor() {

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

    //Config stuff thats used throughout the program
    val configpath = loadConfigpath()
    val configprops = loadConfig()
    val steamcmdPath = loadConfig().getProperty("steamcmdpath")
    val isTest = loadConfig().getProperty("istest").toBoolean()
    val LOGINS = loadLogins();

    /**
     * Loads the config file from the config.properties file
     */
    private fun loadConfig(): Properties {
        val props = Properties()
        val configFile = File("$configpath/config.properties")
        if (configFile.exists()) {
            props.load(configFile.inputStream())
        } else {
            //create default config
            configFile.createNewFile()
            props.setProperty("steamcmdpath", "PUT YOUR STEAMCMD PATH HERE")
            props.setProperty("istest", "true")
            props.store(configFile.outputStream(), "steamcmdpath must be with forwardslashes (/)\n if istest is true, the actual process will be a ping to google.com");
            Alert(Alert.AlertType.INFORMATION).apply {
                title = "SteamCMD Tool"
                headerText = "Config file created"
                contentText = "You MUST set the steamcmd path in the config file"
            }.showAndWait()
        }
        return props
    }

    /**
     * Loads the Login presets from the logins.json file
     */
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

    /**
     * Tries to load the configpath
     * The configpath is the working directory of the program and then /toolfiles
     *
     * if the configpath is not found, the program will create it
     */
    private fun loadConfigpath(): String {
        var path = Paths.get("").toAbsolutePath().toString() + "/toolfiles"
        return if (Files.exists(Paths.get(path))) {
            path
        } else {
            Files.createDirectory(Paths.get(path))
            path;
        }

    }


    /**
     * Tries to load all SteamApp ids from either the file or the API
     * if the file is either not found or older than 24 hours, the API will be used to create the file
     */
    public fun loadSteamAppContainerJson(): String {
        val appidfile = File("$configpath/steamappcontainer.json")

        val fileExists = Files.exists(Paths.get("$configpath/steamappcontainer.json"))

        if (fileExists){
            var attr = Files.readAttributes(appidfile.toPath(), BasicFileAttributes::class.java)
            var filetime = attr.lastModifiedTime()
            val fileOld = filetime.toMillis() < System.currentTimeMillis() - (1000 * 60 * 60 * 24)

            if (fileOld) {
                return rereadJsonFromAPI()
            }
            println("AppIDs aus Datei gelesen!")
            return appidfile.readText()
        }
        return rereadJsonFromAPI()

    }

    /**
     * Tries to load all SteamApp ids from the API
     */
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

    /**
     * Adds an entry to the logins.json file
     */
    public fun addPreset(preset: SteamLogin) {
        val configFile = File("$configpath/logins.json")
        val gson = Gson()
        val logins = loadLogins()

        logins.add(preset)
        configFile.writeText(gson.toJson(logins))
    }

}