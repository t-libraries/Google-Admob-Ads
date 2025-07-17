package com.admobads.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RemoteModel(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("ad_format")
    val ad_format: String = "",
    @SerializedName("ad_type")
    val ad_type: Int = 3,
    @SerializedName("hide")
    val hide: Boolean = false,
    @SerializedName("cta_color")
    val cta_color: String = "#F42727"


) {
    override fun toString(): String {
        return " (id : $id) (ad_format : $ad_format) (ad_type : $ad_type) (cta_color : $cta_color) (hide : $hide) "
    }
}