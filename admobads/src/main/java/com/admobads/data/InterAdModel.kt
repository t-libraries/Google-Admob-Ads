package com.admobads.data

import androidx.annotation.Keep

@Keep
data class InterAdModel(
    val inter_type: String = "timer",
    val inter_counter_start: Int = 0,
    val inter_counter_gap: Int = 0,
    val inter_start_after_seconds: Long = 0,
    val inter_start_load_before_seconds: Long = 0,
    val inter_gap_after_seconds: Long = 0,
    val inter_gap_load_before_seconds: Long = 0
)
