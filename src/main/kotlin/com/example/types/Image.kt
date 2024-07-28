package com.example.types

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
class Image(
    val id:Int,
    val date:Instant,
    val binary: String,
) {

}