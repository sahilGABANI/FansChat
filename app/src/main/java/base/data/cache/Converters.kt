package base.data.cache

import androidx.room.TypeConverter
import base.data.api.users.model.FansChatUserDetails
import base.data.model.Sender
import base.data.model.User
import base.data.model.chat.Chat
import base.data.model.feed.FeedItem
import base.data.model.feed.FeedItem.Type
import base.data.model.other.Goalscorer
import base.data.model.other.Translation
import base.data.model.wall.FeedSetupItem
import base.data.model.wall.TickerData
import base.socket.model.Owner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.util.*

class Converters {

    @TypeConverter
    fun feedItemFromString(value: String): FeedItem? {
        return Gson().fromJson(value, FeedItem::class.java)
    }

    @TypeConverter
    fun feedItemToString(item: FeedItem?): String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun typeFromString(key: String): Type {
        return Type.valueOf(key)
    }

    @TypeConverter
    fun typeToString(item: Type): String {
        return item.toString()
    }

    @TypeConverter
    fun userFromString(value: String): User? {
        return Gson().fromJson(value, User::class.java)
    }

    @TypeConverter
    fun userToString(item: User?): String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun fansUserFromString(value: String): FansChatUserDetails? {
        return Gson().fromJson(value, FansChatUserDetails::class.java)
    }

    @TypeConverter
    fun fansUserToString(item: FansChatUserDetails?): String {
        return Gson().toJson(item)
    }


    @TypeConverter
    fun dateFromString(value: String): Date? {
        return Gson().fromJson(value, Date::class.java)
    }

    @TypeConverter
    fun dateToString(date: Date?): String {
        return Gson().toJson(date)
    }

    @TypeConverter
    fun chatFromString(value: String): Chat? {
        return Gson().fromJson(value, Chat::class.java)
    }

    @TypeConverter
    fun chatToString(chat: Chat?): String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun listOfStringsFromString(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(value, listType)
    }

    @TypeConverter
    fun listOfStringsToString(chat: List<String>?): String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun arraylistOfStringsFromString(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson<ArrayList<String>>(value, listType)
    }

    @TypeConverter
    fun arraylistOfStringsToString(chat: ArrayList<String>?): String {
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
    fun listOfTranslationsToString(chat: ArrayList<Translation>?): String {
        return Gson().toJson(chat)
    }

    @TypeConverter
    fun listOfOwnerFromString(value: String): ArrayList<Owner>? {
        val listType = object : TypeToken<ArrayList<Owner>>() {}.type
        return Gson().fromJson<ArrayList<Owner>>(value, listType)
    }

    @TypeConverter
    fun listOfOwnerToString(owner: ArrayList<Owner>?): String {
        return Gson().toJson(owner)
    }

    @TypeConverter
    fun listOfGoalscorersFromString(value: String): List<Goalscorer>? {
        val listType = object : TypeToken<ArrayList<Goalscorer>>() {}.type
        return Gson().fromJson<ArrayList<Goalscorer>>(value, listType)
    }

    @TypeConverter
    fun listOfGoalscorersToString(item: List<Goalscorer>?): String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun listOfFeedSetupItemFromString(value: String): List<FeedSetupItem>? {
        val listType = object : TypeToken<ArrayList<FeedSetupItem>>() {}.type
        return Gson().fromJson<ArrayList<FeedSetupItem>>(value, listType)
    }

    @TypeConverter
    fun listOfFeedSetupItemToString(item: List<FeedSetupItem>?): String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun listOfTickerFromString(value: String): List<TickerData>? {
        val listType = object : TypeToken<ArrayList<TickerData>>() {}.type
        return Gson().fromJson<ArrayList<TickerData>>(value, listType)
    }

    @TypeConverter
    fun listOfTickerToString(item: List<TickerData>?): String {
        return Gson().toJson(item)
    }

    @TypeConverter
    fun ownerFromString(value: String): Owner? {
        return Gson().fromJson(value, Owner::class.java)
    }

    @TypeConverter
    fun ownerToString(item: Owner?): String {
        return Gson().toJson(item)
    }


    @TypeConverter
    fun listOfAnyFromString(value: String): ArrayList<Any> {
        val listType = object : TypeToken<ArrayList<Any>>() {}.type
        return Gson().fromJson<ArrayList<Any>>(value, listType)
    }

    @TypeConverter
    fun listOfAnyToString(value: ArrayList<Any>): ArrayList<String>? {
        var item = arrayListOf<String>()

        value?.forEach { item.add(it.toString()) }

        return item
    }

    @TypeConverter
    fun listOfUserFromString(value: String): ArrayList<User> {
        val listType = object : TypeToken<ArrayList<User>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun listOfUserToString(value: ArrayList<User>): ArrayList<String>? {
        var item = arrayListOf<String>()

        value?.forEach { item.add(it.toString()) }

        return item
    }

//    @TypeConverter
//    fun listOfAnyToString(anyString: ArrayList<Any>): String {
//        return Gson().toJson(anyString)
//    }
    @TypeConverter
    fun senderFromString(value: String): Sender? {
        return Gson().fromJson(value, Sender::class.java)
    }

    @TypeConverter
    fun senderToString(item: Sender?): String {
        return Gson().toJson(item)
    }

}


class OtherTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun objectFromString(value: ArrayList<Any>): ArrayList<String> {
        var item = arrayListOf<String>()

        value.forEach { item.add(it.toString()) }

        return item
    }

    @TypeConverter
    fun stringToList(data: String?): List<String> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<String>>() {

        }.type

        return gson.fromJson<List<String>>(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: List<String>): String {
        return gson.toJson(someObjects)
    }
}
