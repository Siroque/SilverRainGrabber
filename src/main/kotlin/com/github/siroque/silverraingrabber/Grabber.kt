package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.BroadcastSnatcher.fetch
import kotlin.math.ceil

object Grabber {
    @JvmStatic
    fun main(args: Array<String>) {
        val rootOutputPath = "D:\\Multimedia\\SilverRain"//getProperty("user.dir")+"\\output"
        fetch(rootOutputPath)
        ceil(0.1)
    }
}
