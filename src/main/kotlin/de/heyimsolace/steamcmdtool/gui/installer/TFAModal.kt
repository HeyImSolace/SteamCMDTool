package de.heyimsolace.steamcmdtool.gui.installer

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style

class TFAModal {
    private var jmetro: JMetro = JMetro(Style.DARK)
    private var stage: Stage = Stage()
    private var root: VBox = VBox()
    private var scene: Scene = Scene(root)

    private var label: Label = Label("2FA Code:")
    private var tf: TextField = TextField()
    private var confirmButton: Button = Button("Confirm")

    public constructor(parent: Stage) {

        confirmButton.setOnAction { confirmButtonAction() }

        root.alignment = Pos.CENTER
        root.spacing = 10.0
        root.padding = Insets(10.0)
        stage.scene = scene
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(parent.scene.window)

        jmetro.scene = scene
        root.styleClass.add(JMetroStyleClass.BACKGROUND)

        root.children.addAll(label, tf, confirmButton)
        stage.scene = scene

    }

    private fun confirmButtonAction() {
        stage.close()
    }

    public fun showAndReturnAuth(): String {
        stage.showAndWait()
        return tf.text
    }

}