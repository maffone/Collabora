package org.gammf.collabora.database.messages

import org.gammf.collabora.util.LoginUser

/**
  * A simple trait representing a message created from a db worker
  */
trait DBWorkerMessage

/**
  * A simple case class representing a message used to tell that a certain operation on the db is succeeded
  */
case class QueryOkMessage(queryGoneWell: QueryMessage) extends DBWorkerMessage

/**
  * A simple case class representing a message used to tell that a certain operation on the DB failed.
  * @param error the error.
  */
case class QueryFailMessage(error: Exception) extends DBWorkerMessage

/**
  * Simple class representing a message used to report login info (username and password) of a user. If no user is
  * found the loginInfo is None.
  * @param loginInfo username and password if the user is registered. None otherwise.
  */
case class AuthenticationMessage(loginInfo: Option[LoginUser]) extends DBWorkerMessage