package com.coding.meet.gaminiaikmp.composeApp

import app.cash.sqldelight.SuspendingTransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.coding.meet.gaminiaikmp.AppDatabaseQueries
import com.coding.meet.gaminiaikmp.GeminiApiChatDB
import com.coding.meet.gaminiaikmp.Message
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<GeminiApiChatDB>.schema: SqlSchema<QueryResult.AsyncValue<Unit>>
  get() = GeminiApiChatDBImpl.Schema

internal fun KClass<GeminiApiChatDB>.newInstance(driver: SqlDriver,
    MessageAdapter: Message.Adapter): GeminiApiChatDB = GeminiApiChatDBImpl(driver, MessageAdapter)

private class GeminiApiChatDBImpl(
  driver: SqlDriver,
  MessageAdapter: Message.Adapter,
) : SuspendingTransacterImpl(driver), GeminiApiChatDB {
  override val appDatabaseQueries: AppDatabaseQueries = AppDatabaseQueries(driver, MessageAdapter)

  public object Schema : SqlSchema<QueryResult.AsyncValue<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
      driver.execute(null, """
          |CREATE TABLE GroupChat(
          |    groupId TEXT UNIQUE PRIMARY KEY NOT NULL,
          |    title TEXT NOT NULL,
          |    date TEXT NOT NULL,
          |    image TEXT NOT NULL
          |)
          """.trimMargin(), 0).await()
      driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS Message(
          |    messageId TEXT PRIMARY KEY NOT NULL,
          |    chatId TEXT NOT NULL,
          |    content TEXT NOT NULL,
          |    images TEXT NOT NULL,
          |    participant TEXT NOT NULL,
          |    isPending INTEGER NOT NULL DEFAULT 0,
          |    FOREIGN KEY (chatId) REFERENCES GroupChat(groupId)
          |)
          """.trimMargin(), 0).await()
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
    }
  }
}
