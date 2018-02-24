package com.staszkox.cryptotrayalert.helpers

import com.staszkox.cryptotrayalert.AlertCurrency
import java.io.IOException
import java.net.URL
import java.util.stream.Collectors

class HttpSender
{
    private val fiatCurrencies = "PLN,USD,EUR"

    fun getPricesFromAPI(currenciesAlert: List<AlertCurrency>) : String?
    {
        val currencies = currenciesAlert.stream().map(AlertCurrency::name).collect(Collectors.joining(","))
        return getPricesByCurrenciesCodes(currencies)
    }

    fun getPricesByCurrenciesCodes(currencies : String) : String?
    {
        val requestURL = URL("https://min-api.cryptocompare.com/data/pricemulti?fsyms=$currencies&tsyms=$fiatCurrencies")

        val response = try
        {
            requestURL.openStream().bufferedReader().use { it.readText() }
        }
        catch (e: IOException)
        {
            null
        }

        return response
    }
}

