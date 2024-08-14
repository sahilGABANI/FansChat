package base.data.model

import java.io.Serializable

data class NotificationSettings(
    val ims: Boolean = true,
    val news: Boolean = true,
    val rumor: Boolean = true,
    val social: Boolean = true,
    val video: Boolean = true,
    val wall: Boolean = true
) : Serializable