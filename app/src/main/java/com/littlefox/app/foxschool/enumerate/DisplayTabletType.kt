package com.littlefox.app.foxschool.enumerate

import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import java.lang.Exception

enum class DisplayTabletType
{
    DEFAULT,
    RADIO_4_3;

    companion object
    {
        fun toDisplayTabletType(data : String?) : DisplayTabletType
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