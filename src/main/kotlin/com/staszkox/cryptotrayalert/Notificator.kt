package com.staszkox.cryptotrayalert

import com.staszkox.cryptotrayalert.helpers.GuiFactory
import javafx.application.Platform
import javafx.util.Duration
import org.controlsfx.control.Notifications
import java.math.BigDecimal
import java.util.*

class Notificator
{
    companion object
    {
        fun notifyIfRequired(actualData : CurrencyActualData, alertCurrencyToCheck : AlertCurrency, alertCurrencyCode : String) : Boolean
        {
            var notified = false

            val actualPrice = when (alertCurrencyCode)
            {
                "PLN" -> actualData.pln
                "USD" -> actualData.usd
                "EUR" -> actualData.eur
                else -> null
            }

            if (alertCurrencyToCheck.lowerThan != null && actualPrice != null && actualPrice <= alertCurrencyToCheck.lowerThan)
            {
                Platform.runLater({ notifyPriceDecreased(alertCurrencyToCheck, actualPrice, alertCurrencyCode) })
                alertCurrencyToCheck.lowerThan = null
                notified = true
            }

            if (alertCurrencyToCheck.higherThan != null && actualPrice != null && actualPrice >= alertCurrencyToCheck.higherThan)
            {
                Platform.runLater({ notifyPriceIncreased(alertCurrencyToCheck, actualPrice, alertCurrencyCode) })
                alertCurrencyToCheck.higherThan = null
                notified = true
            }

            return notified
        }

        private fun notifyPriceIncreased(alertCurrencyToCheck: AlertCurrency, actualPrice: BigDecimal?, alertCurrencyCode: String)
        {
            val stage = GuiFactory.hiddenStage()
            stage.show()
            Platform.setImplicitExit(false)

            Notifications.create().title("${alertCurrencyToCheck.name} alert (${Date()})")
                    .text("${alertCurrencyToCheck.name} value increases to $actualPrice $alertCurrencyCode")
                    .hideAfter(Duration.INDEFINITE)
                    .onAction({
                        stage.close()
                        Platform.exit()
                    })
                    .showInformation()
        }

        private fun notifyPriceDecreased(alertCurrencyToCheck: AlertCurrency, actualPrice: BigDecimal?, alertCurrencyCode: String)
        {
            val stage = GuiFactory.hiddenStage()
            stage.show()
            Platform.setImplicitExit(false)

            Notifications.create().title("${alertCurrencyToCheck.name} alert (${Date()})")
                    .text("${alertCurrencyToCheck.name} value came down to $actualPrice $alertCurrencyCode")
                    .hideAfter(Duration.INDEFINITE)
                    .onAction({
                        stage.close()
                        Platform.exit()
                    })
                    .showInformation()
        }
    }
}