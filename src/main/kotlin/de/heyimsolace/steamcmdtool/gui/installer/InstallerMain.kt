package de.heyimsolace.steamcmdtool

import de.heyimsolace.steamcmdtool.data.Configurator
import de.heyimsolace.steamcmdtool.data.SteamAppContainer
import de.heyimsolace.steamcmdtool.data.SteamLogin
import de.heyimsolace.steamcmdtool.gui.installer.ConsoleDisplay
import de.heyimsolace.steamcmdtool.gui.installer.TFAModal
import de.heyimsolace.steamcmdtool.gui.util.AutocompletionlTextField
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import org.controlsfx.control.textfield.TextFields

class InstallerMain : Application() {

    private var root: VBox = VBox()
    private var scene: Scene = Scene(root)
    private var jMetro: JMetro = JMetro(Style.DARK)
    private var stage: Stage = Stage(); //only temporary, is getting set by the start method

    private var loginL: Label = Label("Login")
    private var appidL: Label = Label("AppID")
    private var pathL: Label = Label("Path")
    private var presetL: Label = Label("Preset")
    private var header: Label = Label("SteamCMD Tool")

    private var loginPresetCB: ComboBox<SteamLogin> = ComboBox()

    private var loginTf: TextField = TextField()
    private var passwordTf: TextField = PasswordField()
    private var appidTf: AutocompletionlTextField =
        AutocompletionlTextField()

    private var targetPath: TextField = TextFields.createClearableTextField()
    private var pathButton: Button = Button("...")
    private var pathChooser: DirectoryChooser = DirectoryChooser()
    private var startButton: Button = Button("Install")

    private var addUserButton: Button = Button("+")

    private var grid: GridPane = GridPane()



    override fun start(stage: Stage) {
        this.stage = stage
        stage.title = "SteamCMD Tool"
        stage.scene = buildScene()
        stage.show()
    }

    private fun buildScene(): Scene {

        loginPresetCB.items.addAll(Configurator.getConfigurator().LOGINS)

        val appidlist = SteamAppContainer.buildContainer()?.applist?.apps?.toList()?.map { "" + it.appid + " - " + it.name }
        if (appidlist != null) {
            appidTf.entries.addAll(appidlist)
        } else
            appidTf.entries.add("No apps found")



        pathButton.setOnAction {
            pathButtonAction()
        }

        startButton.setOnAction {
            startButtonAction(it)
        }

        loginPresetCB.setOnAction {
            presetCBAction()
        }


        addUserButton.setOnAction {
            addUserButtonAction()
        }

        grid.addRow(0, presetL, loginPresetCB, addUserButton)
        grid.addRow(1, loginL, loginTf, passwordTf)
        grid.addRow(2, appidL)
        grid.add(appidTf, 1, 2, 2, 1)
        grid.addRow(3, pathL)
        grid.add(targetPath, 1, 3, 2, 1)
        grid.addRow(3, pathButton)

        grid.padding = Insets(10.0)
        grid.hgap = 10.0
        grid.vgap = 10.0

        header.font = Font.font(20.0)
        root.children.addAll(header, grid, startButton)
        root.padding = Insets(10.0)
        root.alignment = Pos.CENTER


        jMetro.scene = scene
        root.styleClass.add(JMetroStyleClass.BACKGROUND)
        return scene
    }


    private fun runSteamCmd(login: String, password: String, auth: String, appid: String, path: String) {

        val display = ConsoleDisplay(this.stage)
        val finalCheck = Alert(Alert.AlertType.CONFIRMATION, "u sure?").showAndWait()
        if (finalCheck.get() == ButtonType.OK) {

        val config = Configurator.getConfigurator()
        //for testing
        val processbuilder = if (config.isTest){
                ProcessBuilder("cmd.exe", "/c", "ping -n 15 google.com")
            } else {
                ProcessBuilder(config.steamcmdPath + "steamcmd.exe", "+force_install_dir $path", "+login $login $password $auth", "+app_update $appid + validate", "+quit")
            }
        processbuilder.redirectErrorStream(true)
        val process = processbuilder.start()
        display.showProcessLog(process)
        process.waitFor()
        process.destroy()
        } else {
            display.close()
        }
    }

    private fun startButtonAction(action: ActionEvent) {
        val auth = TFAModal(this.stage).showAndReturnAuth().trim()

        val login = loginTf.text
        val password = passwordTf.text
        val appid = appidTf.text.substringBefore(" - ")
        val path = targetPath.text
        if (login.isEmpty() || (password.isEmpty() && !login.equals("anonymous")) || (auth.isEmpty() && !login.equals("anonymous")) || appid.isEmpty() || path.isEmpty()) {
            Alert(Alert.AlertType.ERROR, "You gotta fill in all the fields!").show()
            return
        } else {
            runSteamCmd(login, password, auth, appid, path)
        }
    }

    private fun pathButtonAction() {
        val path = pathChooser.showDialog(null)
        if (path != null) {
            targetPath.text = path.absolutePath
        }
    }

    private fun presetCBAction() {
        val login: SteamLogin = loginPresetCB.selectionModel.selectedItem
        if (login != null) {
            loginTf.text = login.name
            passwordTf.text = login.pass
        }
    }

    private fun addUserButtonAction() {
        if (loginTf.text.isNotBlank() && passwordTf.text.isNotBlank()) {

            val login = SteamLogin(loginTf.text, passwordTf.text)
            Configurator.getConfigurator().addPreset(login)
            loginPresetCB.items.add(login)
        } else {
            Alert(Alert.AlertType.ERROR, "You gotta fill in the login fields!").show()
        }
    }
}
