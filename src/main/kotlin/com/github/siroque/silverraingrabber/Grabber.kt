package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.BroadcastSnatcher.fetch
import com.github.siroque.silverraingrabber.utils.FileUtilities.makeSureDirectoryExists
import com.github.siroque.silverraingrabber.models.BroadcastCategory
import com.github.siroque.silverraingrabber.parsers.WebPageExtractor.getBroadcastCategoryListItems
import java.lang.System.getProperty

object Grabber {
    @JvmStatic
    fun main(args: Array<String>) {
        val rootOutputPath = "C:\\Users\\isukh\\Desktop\\SilverRain"//getProperty("user.dir")+"\\output"
        fetch(rootOutputPath)
    }
}