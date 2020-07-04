package com.github.siroque.silverraingrabber.parsers

import com.github.siroque.silverraingrabber.models.Broadcast
import com.github.siroque.silverraingrabber.models.BroadcastCategory
import com.github.siroque.silverraingrabber.models.BroadcastListItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.logging.Logger

object WebPageExtractor {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.parsers.WebPageExtractor")

    fun getBroadcastCategoryListItems(baseUrl: String, broadcastCategoryListPath: String): List<BroadcastCategory> {
        val broadcastCategories = mutableListOf<BroadcastCategory>()
        logger.info("Getting category list items from page ${baseUrl+broadcastCategoryListPath}")
        Jsoup.connect(baseUrl+broadcastCategoryListPath).get().run {
            select("div.program-list table tbody tr ").forEach { element ->
                val categoryNameAnchor = element.select("td h3 a")
                val categoryTitle = categoryNameAnchor.text()
                var categoryUrl = categoryNameAnchor.attr("href")
                verifyCategoryListExceptionalLinks(categoryUrl)?.let {
                    categoryUrl = it
                }
                val shouldSkip = shouldSkip(categoryTitle)
                val categoryHosts = mutableListOf<String>()
                element.select("td ul li h4 a").forEach { categoryHosts.add(it.text()) }
                if(!shouldSkip) {
                    when (categoryTitle) {
                        "Завтрак включен" -> {
                            broadcastCategories.add(BroadcastCategory("Коллекция историй",
                                "https://www.silver.ru/programms/zavtrak_vkluchen/kollekciay-istoriy/",
                                arrayListOf("Евгений Жаринов")
                            ))
                        }
                        "Отцы и дети" -> {
                            broadcastCategories.add(BroadcastCategory("Мишанина",
                                "https://www.silver.ru/programms/ottsy_i_deti/archive-of-the-mishanina/",
                                arrayListOf("Михаил Козырев", "Фекла Толстая")
                            ))
                        }
                        else -> {
                            broadcastCategories.add(BroadcastCategory(categoryTitle, baseUrl+categoryUrl, categoryHosts))
                        }
                    }

                }
            }
        }
        return broadcastCategories
    }

    private fun verifyCategoryListExceptionalLinks(categoryPath: String): String? {
        if (categoryPath == "/programms/LookBook/")
            return "/programms/LookBook/vipusky_programmy_lookebook/"
        if (categoryPath == "/programms/mozcow_dizcow_hi_fi_edition/")
            return "/programms/mozcow_dizcow_hi_fi_edition/vipyski-prigrammi/"
        if (categoryPath == "/programms/Soul_kitchen/")
            return "/programms/Soul_kitchen/vipusky_programmy_soul_kitchen/"
        if (categoryPath == "/programms/avtorskienovosty/")
            return "/programms/avtorskienovosty/vipusky_avtorskie_novosti/"
        if (categoryPath == "/programms/Kinoprobi/")
            return "/programms/Kinoprobi/2019/"
        if (categoryPath == "/programms/Minutioiskusstve/")
            return "/programms/Minutioiskusstve/VypuskiprogrammyMinutyobiskusstve/"
        if (categoryPath == "/programms/Plastilin/")
            return "/programms/Plastilin/vipyski_programmi_plastilin/"
        if (categoryPath == "/programms/edim_sport/")
            return "/programms/edim_sport/vipuski_programmy_edim_sport/"
        if (categoryPath == "/programms/Istoria_sd/")
            return "/programms/Istoria_sd/vipyski-programmi_istoriasd/"
        if (categoryPath == "/programms/kak_to_tak/")
            return "/programms/kak_to_tak/vipuski_programmy_kak-to_tak/"
        if (categoryPath == "/programms/ot_avtora/")
            return "/programms/ot_avtora/vipusky_programmy_ot_avtora/"
        if (categoryPath == "/programms/ottsy_i_deti/")
            return "/programms/ottsy_i_deti/+editions-of-the-program/"
        if (categoryPath == "/programms/francyzskiy-za-minuty/")
            return "/programms/francyzskiy-za-minuty/vipyski-programmi/"
        if (categoryPath == "/programms/xolodnaya_voyna/")
            return "/programms/xolodnaya_voyna/vipuski_programmy_holodnaya_voina/"
        if (categoryPath == "/programms/Cikllekciyobevolucii/")
            return "/programms/Cikllekciyobevolucii/vipyski-programmi/"
        if (categoryPath == "/programms/shpionskie_igri/")
            return "/programms/shpionskie_igri/vipuski_programmy_shpionskie_igri/"
        return null
    }

    fun getBroadcastArchiveListItems(baseUrl: String, broadcastListUrl: String): List<BroadcastListItem> {
        val broadcasts = mutableListOf<BroadcastListItem>()
        logger.info("Getting broadcast archive list items from page $broadcastListUrl")
        Jsoup.connect(broadcastListUrl).get().run {
            select("div dl dd table tbody tr td").forEach { element ->
                element.elementSiblingIndex()
                val headerAnchor = element.select("td h4 a")
                val title = headerAnchor.text()
                val href = headerAnchor.attr("href")

                val imgAnchor = element.select("td a img")
                var imgSrc: String? = imgAnchor.attr("src")
                if (imgSrc!!.isBlank()) imgSrc = null
                var imgUrl: String? = null
                imgSrc?.let {
                    imgUrl = baseUrl + it
                }

                val dateAnchor = element.select("td div span")
                val date = dateAnchor.text()

                val descriptionAnchor = element.select("td p")
                val description = descriptionAnchor.text()
                if (title.isNotEmpty() && date.isNotEmpty() && href.isNotEmpty()){
                    var desc = ""
                    description?.let { desc = it }
                    broadcasts.add(
                        BroadcastListItem(
                            title,
                            baseUrl + href,
                            date,
                            desc,
                            imgUrl
                        )
                    )
                }
            }
        }
        logger.info("Extracted ${broadcasts.size} broadcast list items")
        return broadcasts
    }

    fun getBroadcastListItems(baseUrl: String, broadcastListUrl: String): List<BroadcastListItem> {
        val broadcasts = mutableListOf<BroadcastListItem>()
        logger.info("Getting broadcast list items from page $broadcastListUrl")
        Jsoup.connect(broadcastListUrl).get().run {
            select("tr ").forEach { element ->
                val headerAnchor = element.select("td h4 a")
                val title = headerAnchor.text()
                val href = headerAnchor.attr("href")

                val imgAnchor = element.select("td a img")
                var imgSrc: String? = imgAnchor.attr("src")
                if (imgSrc!!.isBlank()) imgSrc = null
                var imgUrl: String? = null
                imgSrc?.let {
                    imgUrl = baseUrl + it
                }

                val dateAnchor = element.select("td div span")
                val date = dateAnchor.text()

                val descriptionAnchor = element.select("td p")
                val description = descriptionAnchor.text()
                if (title.isNotEmpty() && date.isNotEmpty() && href.isNotEmpty()){
                    var desc = ""
                    description?.let { desc = it }
                    broadcasts.add(
                        BroadcastListItem(
                            title,
                            baseUrl + href,
                            date,
                            desc,
                            imgUrl
                        )
                    )
                }
            }
        }
        logger.info("Extracted ${broadcasts.size} broadcast list items")
        return broadcasts
    }

    fun getBroadcast(baseUrl: String, item: BroadcastListItem, category: BroadcastCategory): Broadcast? {
        try {
            var cast: Broadcast? = null
            if (category.title.contains("Музыкальный секонд-хэнд")){
                Jsoup.connect(item.url).get().run {
                    select("a.red").forEach { element ->
                        val streamUrl = element.attr("data-audio")
                        cast = broadcast(baseUrl, item, category, streamUrl.replace("/import_mssql/content", ""), element)
                    }
                }
            }
            cast?.let {
                return it
            }
            Jsoup.connect(item.url).get().run {
                select("audio").forEach { element ->
                    val streamUrl = element.attr("src")
                    return broadcast(baseUrl, item, category, streamUrl, element)
                }
            }
        } catch (e: Exception) {
            logger.severe("Failed to fetch broadcast from URL: ${item.url}. Error message: ${e.localizedMessage}")
        }
        return null
    }

    private fun broadcast(baseUrl: String, item: BroadcastListItem, category: BroadcastCategory, streamUrl: String, element: Element): Broadcast?{
        if (streamUrl.isNotEmpty() && (streamUrl.contains("medialibrary")
                    || streamUrl.contains("LoadedImages")
                    || streamUrl.contains("images")
                    || streamUrl.contains("LoadedFiles"))){
            val categoryUrl = if (category.url.contains("silver.ru")) category.url else baseUrl + category.url
            val resultStreamUrl = if (streamUrl.contains("silver.ru")) streamUrl else baseUrl + streamUrl
            return Broadcast(
                item.title,
                category.hostNames,
                category.title,
                categoryUrl.replace("\\s".toRegex(), ""),
                item.date,
                item.description,
                item.url,
                resultStreamUrl,
                item.imgUrl,
                null
            )
        } else {
            logger.warning("ERROR: Malformed src attribut (streamUrl) of audio player: $streamUrl")
            logger.warning("`audio` css selector resolved to element with content: $element")
            logger.warning(item.url)
        }
        return null
    }

    fun shouldSkip(categoryTitle: String): Boolean{
        val corruptCategories = arrayListOf(
            "mixtape",
            "бизнес новости",
            "вам слово",
            "ваше право",
            "вечерний рбк",
            "Выходные с Мари Армас",
            "Классика",
            "Культур-мультур weekend",
            "Музыка",
            "Новости",
            "Вчера. Сегодня. Завтра",
            "Блог Даши Касьяновой",
            "Архив лекций",
            "Ну да, Москва",
            "Наноновости",
            "Добрый день")
        val validCategoriesToSkip = arrayListOf(
            "Discover vibes",
            "Flammable Beats",
            "In-Beat-Ween Music Show",
            "Intelligent Beats",
            "LookBook",
            "Melody P.M.",
            "Mixtape",
            "Mozcow Dizcow Hi-Fi Edition",
            "Prime Traveller",
            "Soul Kitchen",
            "Авторские новости от НСН",
            "Барная карта",
            "Большой Ежедневник",
            "Взрослым о взрослых",
            "Вне зависимости",
            "Вокруг света за 8000 песен",
            "Волшебные бобы",
            "Дети. Инструкция по применению",
            "Доброе Утро, Вьетнам",
            "ЕДим спорт",
            "Есть или не есть?",
            "Завтрак включен",
            "История в лицах",
            "Йога для мозгов",
            "Кино и музыка",
            "Кинопробы",
            "Культур-мультур",
            "Минуты об искусстве",
            "Мужские игры",
            "Музыкальный секонд-хэнд",
            "На час раньше",
            "Пиратское радио",
            "Пластилин",
            "Потапенко будит!",
            "С приветом, Набутов!",
            "Тема дня за 60 секунд",
            "Тест-драйв онлайн",
            "Хождение по звукам",
            "Что-то хорошее",
            "Электроспектива")
//        corruptCategories.addAll(validCategoriesToSkip)
//        println(categoryTitle.toLowerCase())
//        println(corruptCategories.any { it.toLowerCase() == categoryTitle.toLowerCase() })
        //Архив
        //Музыкальный секонд-хэнд
        println("Should skip category $categoryTitle: ${corruptCategories.any { it.toLowerCase() == categoryTitle.toLowerCase() }}")
        return corruptCategories.any { it.toLowerCase() == categoryTitle.toLowerCase() }
    }
}