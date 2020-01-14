package com.github.siroque.silverraingrabber.models

data class BroadcastListItem (
    val title: String,
    val url: String,
    val date: String,
    val description: String,
    val imgUrl: String?
)

data class Broadcast (
    val title: String,
    val hostNames: List<String>,
    val category: String,
    val categoryUrl: String,
    val date: String,
    val description: String,
    val url: String,
    val streamUrl: String,
    val imgUrl: String?,
    val filePath: String?
) {
    override fun toString(): String{
        return "${this.title}, ${this.category}, ${this.categoryUrl}, ${this.date}, ${this.description}, ${this.url}, ${ this.streamUrl}, ${this.imgUrl}, ${this.filePath}"
    }
}

data class BroadcastCategory (
    val title: String,
    val url: String,
    val hostNames: List<String>
) {
    override fun toString(): String{
        return "${this.title}, ${this.url}, ${this.hostNames.toTypedArray().contentToString()}"
    }
}