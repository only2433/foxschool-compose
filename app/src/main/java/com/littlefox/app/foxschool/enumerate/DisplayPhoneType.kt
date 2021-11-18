package com.littlefox.app.foxschool.enumerate

import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import java.lang.Exception

enum class DisplayPhoneType
{
    DEFAULT,
    RADIO_20_9,
    RADIO_FLIP;

    companion object
    {
        fun toDisplayPhoneType(data : String?) : DisplayPhoneType
        {
            try
            {
                return valueOf(data!!)
            }
            catch(e : Exception)
            {
                return DEFAULT
            }
        }
    }
}