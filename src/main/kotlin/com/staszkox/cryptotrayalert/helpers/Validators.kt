package com.staszkox.cryptotrayalert.helpers

import org.json.JSONObject
import java.math.BigDecimal

class Validators
{
    companion object
    {
        fun validateAlertPrice(text : String?) : Boolean
        {
            var validAlertPrice : Boolean

            if (text == null || text == "")
            {
                validAlertPrice = true
            }
            else
            {
                try
                {
                    BigDecimal(text)
                    validAlertPrice = true
                }
                catch (e : Exception)
                {
                    validAlertPrice = false
                }
            }

            return validAlertPrice
        }

        fun validateCodeCurrency(text : String?) : Boolean
        {
            var validCode : Boolean

            if (text == null || text == "")
            {
                validCode = false
            }
            else
            {
                val response = HttpSender().getPricesByCurrenciesCodes(text)

                try
                {
                    JSONObject(response).getJSONObject(text)
                    validCode = true
                }
                catch (e : Exception)
                {
                    validCode = false
                }
            }

            return validCode
        }
    }
}