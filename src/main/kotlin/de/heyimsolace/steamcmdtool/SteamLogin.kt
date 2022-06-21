package de.heyimsolace.steamcmdtool

data class SteamLogin(var name: String, var pass: String) {

    override fun toString(): String {
        return "$name"
    }
}