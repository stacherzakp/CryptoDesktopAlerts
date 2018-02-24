package com.staszkox.cryptotrayalert

import com.staszkox.cryptotrayalert.helpers.*
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import javafx.util.Duration
import org.json.JSONObject
import java.awt.*
import java.awt.MenuItem
import java.math.BigDecimal
import java.math.RoundingMode
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

class App : Application()
{
    private var primaryStage : Stage? = null
    private var alertsTable : TableView<AlertCurrency>? = null

    private val alertCurrencies = FilesHelper().readCurrencies()
    private val currenciesActualData = FXCollections.observableArrayList<CurrencyActualData>()!!
    private var alertCurrencyCode = FilesHelper().readSettings(defaultCurrency = "PLN")

    private val updateIntervalDuration = Duration.seconds(10.0)
    private val sceneWidth = 800.0
    private val sceneHeight = 600.0

    override fun start(primaryStage: Stage)
    {
        this.primaryStage = primaryStage

        primaryStage.title = "Crypto notifications"
        primaryStage.scene = buildGui()
        primaryStage.isResizable = false
        primaryStage.isAlwaysOnTop = false
        primaryStage.initStyle(StageStyle.UTILITY)

        SwingUtilities.invokeLater { addTray() }
        Platform.setImplicitExit(false)

        showStage()

        createCurrenciesUpdater()
        createNotificator()
    }

    private fun showStage()
    {
        primaryStage?.show()
        primaryStage?.toFront()
    }

    private fun addTray()
    {
        if (SystemTray.isSupported())
        {
            Toolkit.getDefaultToolkit()

            val display = MenuItem("Show")
            display.addActionListener {
                Platform.runLater(this::showStage)
            }

            val exit = MenuItem("Exit")

            val menu = PopupMenu("Crypto alert")
            menu.add(display)
            menu.addSeparator()
            menu.add(exit)

            val image = ImageIO.read(App::javaClass::class.java.getResource("/coin-of-dollar.png"))

            val trayIcon = TrayIcon(image, "Crypto alert", menu)
            trayIcon.isImageAutoSize = true
            trayIcon.addActionListener {
                Platform.runLater(this::showStage)
            }

            val tray = SystemTray.getSystemTray()
            tray.add(trayIcon)

            exit.addActionListener {
                try
                {
                    FilesHelper().saveCurrencies(alertCurrencies)
                    FilesHelper().saveSettings(alertCurrencyCode)
                    tray.remove(trayIcon)
                    Platform.exit()
                }
                finally
                {
                    System.exit(0)
                }
            }
        }
    }

    private fun buildGui() : Scene
    {
        val actualPricesVBox = buildActualPricesVBox()
        val currenciesToCheckVBox = buildAlertCurrenciesVBox()
        val settingsVBox = buildSettings()

        val group = Group(VBox(HBox(actualPricesVBox, currenciesToCheckVBox), settingsVBox))

        val scene = Scene(group, sceneWidth, sceneHeight)
        return scene
    }

    private fun createNotificator()
    {
        val startFrame = KeyFrame(Duration.seconds(0.0), EventHandler {

            for (currency in alertCurrencies)
            {
                val currencyActualData = currenciesActualData.firstOrNull { currencyActualData -> currencyActualData.name == currency.name }

                if (currencyActualData != null)
                {
                    val notified = Notificator.notifyIfRequired(currencyActualData, currency, alertCurrencyCode)
                    if (notified) alertsTable?.refresh()
                }
            }
        })

        val endFrame = KeyFrame(updateIntervalDuration)

        val notifier = Timeline(startFrame, endFrame)
        notifier.cycleCount = Animation.INDEFINITE
        notifier.play()
    }

    private fun createCurrenciesUpdater()
    {
        val startFrame = KeyFrame(Duration.seconds(0.0), EventHandler {

            HttpSender().getPricesFromAPI(alertCurrencies)?.let {

                currenciesActualData.clear()

                val jsonResponse = JSONObject(it)

                for (currency in alertCurrencies)
                {
                    val currencyResponse = jsonResponse.getJSONObject(currency.name)
                    val pln = BigDecimal(currencyResponse.getDouble("PLN")).setScale(4, RoundingMode.HALF_DOWN)
                    val usd = BigDecimal(currencyResponse.getDouble("USD")).setScale(4, RoundingMode.HALF_DOWN)
                    val eur = BigDecimal(currencyResponse.getDouble("EUR")).setScale(4, RoundingMode.HALF_DOWN)
                    val actualData = CurrencyActualData(currency.name, pln, usd, eur)

                    currenciesActualData.add(actualData)
                }
            }
        })

        val endFrame = KeyFrame(updateIntervalDuration)

        val updater = Timeline(startFrame, endFrame)
        updater.cycleCount = Animation.INDEFINITE
        updater.play()
    }

    private fun buildActualPricesVBox() : VBox
    {
        val plnColumn = GuiFactory.tableColumn<CurrencyActualData>(title = "PLN", objectProperty = "pln")
        val usdColumn = GuiFactory.tableColumn<CurrencyActualData>(title = "USD", objectProperty = "usd")
        val eurColumn = GuiFactory.tableColumn<CurrencyActualData>(title = "EUR", objectProperty = "eur")

        val fiatColumn = GuiFactory.tableColumn(title = "Fiat", subColumns = *arrayOf(plnColumn, usdColumn, eurColumn))
        val nameColumn = GuiFactory.tableColumn<CurrencyActualData>(title = "Currency", objectProperty = "name")

        val table = GuiFactory.table(height = 350.0, width = sceneWidth.half() - 20.0,
                                     items = currenciesActualData, columns = *arrayOf(nameColumn, fiatColumn))

        val label = GuiFactory.label(text = "Actual prices", size = 14.0, bold = true)

        val vBox = GuiFactory.vbox(height = 400.0, width = sceneWidth.half(), spacing = 5.0,
                                   padding = 10.0, elements = *arrayOf(label, table))

        return vBox
    }

    private fun buildAlertCurrenciesVBox() : VBox
    {
        val lowerThanColumn = GuiFactory.tableColumn<AlertCurrency>(title = "Lower than", objectProperty = "lowerThan")
        val higherThanColumn = GuiFactory.tableColumn<AlertCurrency>(title = "Higher than", objectProperty = "higherThan")

        val alertPriceColumn = GuiFactory.tableColumn(title = "Alert on price", subColumns = *arrayOf(lowerThanColumn, higherThanColumn))
        val nameColumn = GuiFactory.tableColumn<AlertCurrency>(title = "Currency", objectProperty = "name")

        val table = GuiFactory.table(height = 350.0, width = sceneWidth.half() - 20.0,
                                     items = alertCurrencies, columns = *arrayOf(nameColumn, alertPriceColumn))

        table.rowFactory = Callback {

            val row = TableRow<AlertCurrency>()

            val removeOption = javafx.scene.control.MenuItem("Remove")
            removeOption.onAction = EventHandler {
                table.items.remove(row.item)
                table.refresh()
            }

            val removeMenu = ContextMenu()
            removeMenu.items.add(removeOption)

            val noContextMenu : ContextMenu? = null

            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty()).then(noContextMenu).otherwise(removeMenu)
            )

            row
        }

        alertsTable = table

        val label = GuiFactory.label(text = "Currencies alerts", size = 14.0, bold = true)

        val vBox = GuiFactory.vbox(height = 400.0, width = sceneWidth.half(), spacing = 5.0,
                                   padding = 10.0, elements = *arrayOf(label, table))

        return vBox
    }

    private fun buildSettings() : VBox
    {
        val radioGroup = GuiFactory.radioGroup()
        val plnRadio = GuiFactory.radioButton(group = radioGroup, text = "PLN", userData = "PLN", selected = alertCurrencyCode == "PLN")
        val usdRadio = GuiFactory.radioButton(group = radioGroup, text = "USD", userData = "USD", selected = alertCurrencyCode == "USD")
        val eurRadio = GuiFactory.radioButton(group = radioGroup, text = "EUR", userData = "EUR", selected = alertCurrencyCode == "EUR")

        val radios = GuiFactory.hbox(spacing = 5.0, elements = *arrayOf(plnRadio, usdRadio, eurRadio))

        radioGroup.selectedToggleProperty().addListener({ _ : ObservableValue<out Toggle>?, _ : Toggle, newValue: Toggle ->
            alertCurrencyCode = (newValue as RadioButton).text
        })

        val messageText = GuiFactory.messageText()

        val nameTextField = GuiFactory.textField(prompt = "Code")
        val lowerThanTextField = GuiFactory.textField(prompt = "Lower than")
        val higherThanTextField = GuiFactory.textField(prompt = "Higher than")

        val addNewAlertEvent = EventHandler<ActionEvent> {

            val validName = Validators.validateCodeCurrency(nameTextField.text)
            val validLowerThan = Validators.validateAlertPrice(lowerThanTextField.text)
            val validHigherThan = Validators.validateAlertPrice(higherThanTextField.text)

            if (validName && validLowerThan && validHigherThan)
            {
                val lowerThanAlert = if (lowerThanTextField.text != null && "" != lowerThanTextField.text) BigDecimal(lowerThanTextField.text) else null
                val higherThanAlert = if (higherThanTextField.text != null && "" != higherThanTextField.text) BigDecimal(higherThanTextField.text) else null
                val newAlertCurrency = AlertCurrency(nameTextField.text, higherThanAlert, lowerThanAlert)

                val oldAlertCurrency = alertCurrencies.firstOrNull{ alertCurrency -> alertCurrency.name == nameTextField.text }

                if (oldAlertCurrency == null)
                {
                    alertCurrencies.add(newAlertCurrency)

                    messageText.text = "Currency successfully added!"
                    messageText.fill = Color.GREEN
                }
                else
                {
                    oldAlertCurrency.higherThan = higherThanAlert
                    oldAlertCurrency.lowerThan = lowerThanAlert

                    messageText.text = "Currency successfully updated!"
                    messageText.fill = Color.GREEN
                }
            }
            else
            {
                messageText.text = "Cannot save a currency!"
                messageText.fill = Color.RED
            }

            nameTextField.clear()
            lowerThanTextField.clear()
            higherThanTextField.clear()
            alertsTable?.refresh()
        }

        val addAlertCurrency = GuiFactory.button(text = "Update", eventHandler = addNewAlertEvent)

        val addNewAlertLabel = GuiFactory.label(text = "Add/update currency alert", size = 12.0)
        val hbox = GuiFactory.hbox(spacing = 1.0,
                elements = *arrayOf(nameTextField, lowerThanTextField, higherThanTextField, addAlertCurrency))

        val addAlertBox = GuiFactory.vbox(spacing = 5.0, elements = *arrayOf(addNewAlertLabel, hbox))

        val currencySelectLabel = GuiFactory.label(text = "Choose currency code for notifications", size = 12.0)
        val currencySelectBox = GuiFactory.vbox(spacing = 5.0, elements = *arrayOf(currencySelectLabel, radios))

        val label = GuiFactory.label(text = "Settings", size = 14.0, bold = true)

        val vBox = GuiFactory.vbox(height = 150.0, width = sceneWidth, spacing = 15.0,
                                   padding = 10.0,
                                   elements = *arrayOf(label, currencySelectBox, addAlertBox, messageText))

        return vBox
    }
}

fun main(args: Array<String>)
{
    Application.launch(App::class.java, *args)
}
