package com.staszkox.cryptotrayalert.helpers

import com.staszkox.cryptotrayalert.AlertCurrency
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class FilesHelper
{
    private val currenciesFileName = "currencies.txt"
    private val settingsFileName = "settings.txt"

    fun saveCurrencies(alertCurrencies: List<AlertCurrency>)
    {
        val csvDefinitions = alertCurrencies.stream().map(AlertCurrency::toCSV).collect(Collectors.joining("\n"))
        Files.write(Paths.get(currenciesFileName), csvDefinitions.toByteArray())
    }

    fun readCurrencies() : ObservableList<AlertCurrency>
    {
        val csvCurrencies = try {Files.readAllLines(Paths.get(currenciesFileName))} catch (e : Exception) {null}

        return when (csvCurrencies)
        {
            null -> FXCollections.observableArrayList<AlertCurrency>()
            else -> csvCurrencies.stream().map(AlertCurrency.Companion::fromCSV)
                            .collect(Collectors.toCollection { FXCollections.observableArrayList<AlertCurrency>() })
        }
    }

    fun saveSettings(selectedCurrency : String)
    {
        Files.write(Paths.get(settingsFileName), selectedCurrency.toByteArray())
    }

    fun readSettings(defaultCurrency : String) : String
    {
        return try {Files.readAllLines(Paths.get(settingsFileName))[0]} catch (e : Exception) {defaultCurrency}
    }
}