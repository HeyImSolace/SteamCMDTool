package de.heyimsolace.steamcmdtool.gui.installer

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class ConsoleDisplay {

    private var jmetro: JMetro = JMetro(Style.DARK)
    private var stage: Stage = Stage()
    private var root: VBox = VBox()
    private var scene: Scene = Scene(root)
    private var scrollPane: ScrollPane = ScrollPane()
    private var label: Label = Label("Console:")
    private var textArea: TextArea = TextArea()
    private var doneButton: Button = Button("Done")

    public constructor(parent: Stage) {

        doneButton.setOnAction {
            stage.close()
        }

        //Console window stuff
        root.padding = Insets(10.0)
        root.alignment = Pos.CENTER

        scrollPane.content = textArea
        textArea.isEditable = false
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(parent.scene.window)

        jmetro.scene = scene
        root.children.addAll(label, scrollPane, doneButton)
        root.styleClass.add(JMetroStyleClass.BACKGROUND)
        stage.scene = scene
        stage.show()
    }

    public fun close() {
        stage.close()
    }


    public fun showProcessLog(process: Process) {

        val readerThread = Thread() {
            val consoleReader = InputStreamReader(process.inputStream)
            while (appendText(consoleReader)) {
                ;
            }
        }
        readerThread.start()
    }

    private fun appendText(inputStreamReader: InputStreamReader): Boolean {
        try {
            val buf = CharArray(256)
            val read: Int = inputStreamReader.read(buf)
            if (read < 1) {
                return false
            }
            Platform.runLater { textArea.appendText(String(buf)) }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }




}

