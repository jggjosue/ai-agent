package com.coding.meet.gaminiaikmp

import app.cash.sqldelight.ColumnAdapter
import domain.model.Role
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.String
import kotlin.collections.List

public data class Message(
  public val messageId: String,
  public val chatId: String,
  public val content: String,
  public val images: List<ByteArray>,
  public val participant: Role,
  public val isPending: Boolean,
) {
  public class Adapter(
    public val imagesAdapter: ColumnAdapter<List<ByteArray>, String>,
    public val participantAdapter: ColumnAdapter<Role, String>,
  )
}
