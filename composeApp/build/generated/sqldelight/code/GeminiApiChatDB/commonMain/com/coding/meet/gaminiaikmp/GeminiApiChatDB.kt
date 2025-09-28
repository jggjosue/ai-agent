package com.coding.meet.gaminiaikmp

import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.coding.meet.gaminiaikmp.composeApp.newInstance
import com.coding.meet.gaminiaikmp.composeApp.schema
import kotlin.Unit

public interface GeminiApiChatDB : SuspendingTransacter {
  public val appDatabaseQueries: AppDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.AsyncValue<Unit>>
      get() = GeminiApiChatDB::class.schema

    public operator fun invoke(driver: SqlDriver, MessageAdapter: Message.Adapter): GeminiApiChatDB
        = GeminiApiChatDB::class.newInstance(driver, MessageAdapter)
  }
}
