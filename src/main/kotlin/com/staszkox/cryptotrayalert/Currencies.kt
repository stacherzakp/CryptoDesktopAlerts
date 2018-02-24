package com.staszkox.cryptotrayalert

import java.math.BigDecimal

data class AlertCurrency(val name : String, var higherThan : BigDecimal?, var lowerThan : BigDecimal?)
{
    fun toCSV() : String = "$name;$higherThan;$lowerThan;"

    companion object
    {
        fun fromCSV(csv : String) : AlertCurrency
        {
            val csvParts = csv.split(";")

            val name = csvParts[0]
            val higherThan = if (csvParts[1] != "null") BigDecimal(csvParts[1]) else null
            val lowerThan = if (csvParts[2] != "null") BigDecimal(csvParts[2]) else null

            return AlertCurrency(name, higherThan, lowerThan)
        }
    }
}

data class CurrencyActualData(val name : String, val pln : BigDecimal, val usd : BigDecimal, val eur : BigDecimal)
