package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database.messages.{GetConnectionMessage, IsMemberExistsMessage, IsMemberExistsResponseMessage, QueryOkMessage}
import reactivemongo.bson.BSONDocument
import org.gammf.collabora.database._

import scala.concurrent.ExecutionContext.Implicits.global

class DBWorkerCheckMemberExistenceActor(connectionActor: ActorRef) extends UsersDBWorker(connectionActor) with Stash {

  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: IsMemberExistsMessage =>
      find(
        selector = BSONDocument(USER_ID -> message.username),
        okStrategy = bsonDocumet => QueryOkMessage(IsMemberExistsResponseMessage(message.username, bsonDocumet.isDefined))
      ) pipeTo sender
  }
}
