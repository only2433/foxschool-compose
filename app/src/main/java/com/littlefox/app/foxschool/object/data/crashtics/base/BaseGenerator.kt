package com.littlefox.app.foxschool.`object`.data.crashtics.base

import java.util.*

open class BaseGenerator
{
    var crashlyticsData = HashMap<String, String>()
        protected set
    var exception : Exception? = null
        protected set
}