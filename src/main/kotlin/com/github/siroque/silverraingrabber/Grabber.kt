package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.BroadcastSnatcher.fetch
import com.github.siroque.silverraingrabber.utils.FileUtilities.makeSureDirectoryExists
import com.github.siroque.silverraingrabber.models.BroadcastCategory
import com.github.siroque.silverraingrabber.parsers.WebPageExtractor.getBroadcastCategoryListItems
import java.io.File
import java.lang.System.getProperty
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.ceil

object Grabber {
    @JvmStatic
    fun main(args: Array<String>) {
        val rootOutputPath = "D:\\Multimedia\\SilverRain"//getProperty("user.dir")+"\\output"
        fetch(rootOutputPath)
        ceil(0.1)
    }
}
