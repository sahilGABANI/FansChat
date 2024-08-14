package base.data.cache

import androidx.room.TypeConverter
import base.data.model.User
import base.data.model.chat.Chat
import base.data.model.feed.FeedItem
import base.data.model.feed.FeedItem.Type
import base.data.model.other.Goalscorer
import base.data.model.other.Translation
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.util.*

class MessageDateConverter {

    @TypeConverter
    fun feedItemFromString(value: String): FeedItem? {
        return Gson().fromJson(value, FeedItem::class.java)
    }

    @TypeConverter
    fun feedItemToString(item: FeedItem?):

            String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun typeFromString(key: String):

            Type {
        return Type.valueOf(key)
    }

    @TypeConverter
    fun typeToString(item: Type):

            String {
        return item.toString()
    }

    @TypeConverter
    fun userFromString(value: String): User? {
        return Gson().fromJson(value, User::class.java)
    }

    @TypeConverter
    fun userToString(item: User?):

            String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun dateFromString(value: String): Date? {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create() // your format
        return gson.fromJson(value, Date::class.java)
    }

    //"Sep 8, 2021 7:17:58 PM" || Wed Sep 08 19:17:58 GMT+05:30 2021
    @TypeConverter
    fun dateToString(date: Date?): String {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create() // your format
        return gson.toJson(date)
    }

    fun dateToString() {

    }

    @TypeConverter
    fun chatFromString(value: String): Chat? {
        return Gson().fromJson(value, Chat::class.java)
    }

    @TypeConverter
    fun chatToString(chat: Chat?):

            String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun listOfStringsFromString(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(value, listType)
    }

    @TypeConverter
    fun listOfStringsToString(chat: List<String>?):

            String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun listOfTranslationsFromString(value: String): ArrayList<Translation>? {
        val listType = object : TypeToken<ArrayList<Translation>>() {}.type
        return try {
            Gson().fromJson<ArrayList<Translation>>(value, listType)
        } catch (e: Exception) {
            Timber.tag("Converters").e(e.message + " " + value)
            ArrayList()
        }
    }

    @TypeConverter
    fun listOfTranslationsToString(chat: ArrayList<Translation>?):

            String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun listOfGoalscorersFromString(value: String): List<Goalscorer>? {
        val listType = object : TypeToken<ArrayList<Goalscorer>>() {}.type
        return Gson().fromJson<ArrayList<Goalscorer>>(value, listType)
    }

    @TypeConverter
    fun listOfGoalscorersToString(item: List<Goalscorer>?):

            String {
        return Gson().toJson(item)
    }


}
