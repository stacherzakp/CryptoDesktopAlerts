package com.staszkox.cryptotrayalert.helpers

import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle

class GuiFactory
{
    companion object
    {
        fun textField(prompt : String?) : TextField
        {
            val textField = TextField()
            textField.promptText = prompt

            return textField
        }

        fun messageText() : Text
        {
            val text = Text()
            text.font = Font.font("Arial", FontWeight.BOLD, 12.0)
            return text
        }

        fun button(text : String, eventHandler: EventHandler<ActionEvent>) : Button
        {
            val button = Button(text)
            button.onAction = eventHandler

            return button
        }

        fun <T> tableColumn(title: String, objectProperty: String = "", vararg subColumns : TableColumn<T, String>?) : TableColumn<T, String>
        {
            val newColumn = TableColumn<T, String>(title)
            newColumn.cellValueFactory = if (objectProperty != "") PropertyValueFactory<T, String>(objectProperty) else null
            newColumn.columns.addAll(subColumns)

            return newColumn
        }

        fun <T> table(height: Double, width: Double, items : ObservableList<T>, vararg columns : TableColumn<T, String>) : TableView<T>
        {
            val table = TableView<T>()
            table.minHeight = height
            table.maxHeight = height
            table.minWidth = width
            table.maxWidth = width

            table.isEditable = true

            table.items = items
            table.columns.addAll(columns)

            table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

            return table
        }

        fun vbox(height : Double, width : Double, spacing : Double = 0.0, padding : Double = 0.0, vararg elements : Node) : VBox
        {
            val vbox = VBox()
            vbox.minWidth = width
            vbox.maxWidth = width
            vbox.minHeight = height
            vbox.maxHeight = height

            vbox.spacing = spacing
            vbox.padding = Insets(padding)

            vbox.children.addAll(elements)

            return vbox
        }

        fun vbox(spacing: Double, vararg elements: Node) : VBox
        {
            val vbox = VBox()
            vbox.children.addAll(elements)
            vbox.spacing = spacing

            return vbox
        }

        fun hbox(spacing: Double = 0.0, vararg elements : Node) : HBox
        {
            val hbox = HBox()
            hbox.spacing = spacing
            hbox.children.addAll(elements)

            return hbox
        }

        fun label(text : String, size : Double, bold : Boolean = false) : Label
        {
            val label = Label(text)
            label.font = Font.font("arial", if (bold) FontWeight.BOLD else FontWeight.NORMAL, size)

            return label
        }

        fun radioButton(group : ToggleGroup, text : String, userData : String, selected : Boolean = false) : RadioButton
        {
            val radio = RadioButton(text)
            radio.isSelected = selected
            radio.toggleGroup = group
            radio.userData = userData

            return radio
        }

        fun radioGroup() : ToggleGroup
        {
            val group = ToggleGroup()
            return group
        }

        fun hiddenStage() : Stage
        {
            val root = StackPane()
            root.style = "-fx-background-color: TRANSPARENT"

            val scene = Scene(root, 1.0, 1.0)

            val stage = Stage(StageStyle.TRANSPARENT)
            stage.scene = scene
            stage.maxWidth = 1.0
            stage.maxHeight = 1.0
            stage.x = 10000.0
            stage.y = 10000.0
            stage.isAlwaysOnTop = false
            stage.initStyle(StageStyle.UTILITY)

            return stage
        }
    }
}
