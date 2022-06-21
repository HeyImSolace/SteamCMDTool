package de.heyimsolace.steamcmdtool

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
import javafx.stage.Modality
import javafx.stage.Stage
import org.controlsfx.control.textfield.TextFields
import java.io.BufferedReader

class MainApp : Application() {

    private var root: VBox = VBox()
    private var scene: Scene = Scene(root)

    private var loginL: Label = Label("Login")
    private var appidL: Label = Label("AppID")
    private var pathL: Label = Label("Path")
    private var presetL: Label = Label("Preset")
    private var authL: Label = Label("2FA Code:")
    private var header: Label = Label("SteamCMD Tool")

    private var loginPresetCB: ComboBox<SteamLogin> = ComboBox()

    private var loginTf: TextField = TextFields.createClearableTextField()
    private var passwordTf: TextField = TextFields.createClearablePasswordField()
    private var appidTf: AutocompletionlTextField = AutocompletionlTextField()
    private var authTF: TextField = TextFields.createClearableTextField()

    private var targetPath: TextField = TextFields.createClearableTextField()
    private var pathButton: Button = Button("...")
    private var pathChooser: DirectoryChooser = DirectoryChooser()
    private var startButton: Button = Button("Install")
    private var confirmButton: Button = Button("Confirm")
    private var addUserButton: Button = Button("+")

    private var grid: GridPane = GridPane()
    private var authStage: Stage = Stage()
    private var authRoot: VBox = VBox()
    private var authScene: Scene = Scene(authRoot)

    private var consoleStage: Stage = Stage()
    private var consoleRoot: VBox = VBox()
    private var consoleScene: Scene = Scene(consoleRoot)
    private var consoleScrollPane: ScrollPane = ScrollPane()
    private var consoleTextArea: TextArea = TextArea()
    private var consoleButton: Button = Button("Done")


    override fun start(stage: Stage) {

        stage.title = "SteamCMD Tool"
        stage.scene = buildScene()
        stage.show()
    }

    fun main(args: Array<String>) {
        launch(MainApp::class.java)
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

        confirmButton.setOnAction {
            authStage.close()
        }

        consoleButton.setOnAction {
            consoleStage.close()
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

        //Auth window stuff
        authRoot.children.addAll(authL, authTF, confirmButton)
        authRoot.alignment = Pos.CENTER
        authRoot.spacing = 10.0
        authRoot.padding = Insets(10.0)
        authStage.scene = authScene
        authStage.initModality(Modality.WINDOW_MODAL)
        authStage.initOwner(scene.window)

        //Console window stuff
        consoleRoot.padding = Insets(10.0)
        consoleRoot.alignment = Pos.CENTER

        consoleScrollPane.content = consoleTextArea
        consoleTextArea.isEditable = false
        consoleStage.initModality(Modality.WINDOW_MODAL)
        consoleStage.initOwner(scene.window)

        consoleRoot.children.addAll(Label("Console:"), consoleScrollPane, consoleButton)
        consoleStage.scene = consoleScene
        return scene
    }


    private fun runSteamCmd(login: String, password: String, auth: String, appid: String, path: String) {
        consoleButton.isDisable = true
        consoleTextArea.text = ""
        consoleStage.show()

        val config = Configurator.getConfigurator()
        var processbuilder = ProcessBuilder(config.steamcmdPath + "steamcmd.exe", "+force_install_dir $path", "+login $login $password $auth", "+app_update $appid + validate", "+quit")

        //for testing:
        //var processbuilder = ProcessBuilder("cmd.exe", "/c", "dir")
        processbuilder.redirectErrorStream(true)
        val process = processbuilder.start()
        val consoleReader = BufferedReader(process.inputStream.bufferedReader())

        val readerThread = Thread() {
            var out = consoleReader.readLine();
            while (out != null) {
                consoleTextArea.appendText(out + "\n")
                println()
                out = consoleReader.readLine();
            }
        }
        readerThread.start()

        while (readerThread.isAlive) {
            Thread.sleep(100)
        }
        consoleButton.isDisable = false

    }

    private fun startButtonAction(action: ActionEvent) {
        authStage.showAndWait()

        val login = loginTf.text
        val password = passwordTf.text
        val auth = authTF.text
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

    private fun consoleButtonAction() {
        consoleStage.close()
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
