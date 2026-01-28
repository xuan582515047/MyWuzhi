package com.wuzhi.server.service_tool.pojo.bo

class SortType(
    val name: String,
    val value: String
) {
    companion object{
        const val DEFAULT = "default"
        const val PRICE = "price"
        const val CREATE_TIME = "create_time"
        const val UPDATE_TIME = "update_time"
        const val VIEW_COUNT = "view_count"
        const val USE_COUNT = "use_count"
        const val RATING = "rating"

    }
}