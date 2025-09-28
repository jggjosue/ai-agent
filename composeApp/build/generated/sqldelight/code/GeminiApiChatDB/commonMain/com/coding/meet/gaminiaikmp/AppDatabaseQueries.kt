package com.coding.meet.gaminiaikmp

import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import domain.model.Role
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.String
import kotlin.collections.List

public class AppDatabaseQueries(
  driver: SqlDriver,
  private val MessageAdapter: Message.Adapter,
) : SuspendingTransacterImpl(driver) {
  public fun <T : Any> getAllGroup(mapper: (
    groupId: String,
    title: String,
    date: String,
    image: String,
  ) -> T): Query<T> = Query(-951_161_889, arrayOf("GroupChat"), driver, "AppDatabase.sq",
      "getAllGroup", """
  |SELECT GroupChat.groupId, GroupChat.title, GroupChat.date, GroupChat.image
  |FROM GroupChat
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!
    )
  }

  public fun getAllGroup(): Query<GroupChat> = getAllGroup { groupId, title, date, image ->
    GroupChat(
      groupId,
      title,
      date,
      image
    )
  }

  public fun <T : Any> getChatByGroupId(chatId: String, mapper: (
    messageId: String,
    chatId: String,
    content: String,
    images: List<ByteArray>,
    participant: Role,
    isPending: Boolean,
  ) -> T): Query<T> = GetChatByGroupIdQuery(chatId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      MessageAdapter.imagesAdapter.decode(cursor.getString(3)!!),
      MessageAdapter.participantAdapter.decode(cursor.getString(4)!!),
      cursor.getBoolean(5)!!
    )
  }

  public fun getChatByGroupId(chatId: String): Query<Message> = getChatByGroupId(chatId) {
      messageId, chatId_, content, images, participant, isPending ->
    Message(
      messageId,
      chatId_,
      content,
      images,
      participant,
      isPending
    )
  }

  public suspend fun updateMessageByMessageId(isPending: Boolean, messageId: String) {
    driver.execute(1_337_921_506, """
        |UPDATE Message
        |SET isPending = ?
        |WHERE messageId = ?
        """.trimMargin(), 2) {
          bindBoolean(0, isPending)
          bindString(1, messageId)
        }.await()
    notifyQueries(1_337_921_506) { emit ->
      emit("Message")
    }
  }

  public suspend fun insertMessage(Message: Message) {
    driver.execute(-181_882_535, """
        |INSERT INTO Message (messageId, chatId, content, images, participant, isPending)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindString(0, Message.messageId)
          bindString(1, Message.chatId)
          bindString(2, Message.content)
          bindString(3, MessageAdapter.imagesAdapter.encode(Message.images))
          bindString(4, MessageAdapter.participantAdapter.encode(Message.participant))
          bindBoolean(5, Message.isPending)
        }.await()
    notifyQueries(-181_882_535) { emit ->
      emit("Message")
    }
  }

  public suspend fun insertGroup(GroupChat: GroupChat) {
    driver.execute(-635_513_775, """
        |INSERT INTO GroupChat (groupId, title, date, image)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, GroupChat.groupId)
          bindString(1, GroupChat.title)
          bindString(2, GroupChat.date)
          bindString(3, GroupChat.image)
        }.await()
    notifyQueries(-635_513_775) { emit ->
      emit("GroupChat")
    }
  }

  public suspend fun deleteAllMessage(chatId: String) {
    driver.execute(-210_802_426, """
        |DELETE FROM Message
        |WHERE chatId = ?
        """.trimMargin(), 1) {
          bindString(0, chatId)
        }.await()
    notifyQueries(-210_802_426) { emit ->
      emit("Message")
    }
  }

  public suspend fun deleteGroupWithMessage(groupId: String) {
    transaction {
      driver.execute(-1_021_811_341, """DELETE FROM Message WHERE chatId = ?""", 1) {
            bindString(0, groupId)
          }.await()
      driver.execute(-1_021_811_340, """DELETE FROM GroupChat  WHERE groupId = ?""", 1) {
            bindString(0, groupId)
          }.await()
    }
    notifyQueries(92_791_362) { emit ->
      emit("GroupChat")
      emit("Message")
    }
  }

  private inner class GetChatByGroupIdQuery<out T : Any>(
    public val chatId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Message", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Message", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_070_670_518, """
    |SELECT Message.messageId, Message.chatId, Message.content, Message.images, Message.participant, Message.isPending
    |FROM Message
    |WHERE chatId = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, chatId)
    }

    override fun toString(): String = "AppDatabase.sq:getChatByGroupId"
  }
}
