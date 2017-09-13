package org.gammf.collabora.database.actors.worker

import akka.actor.{ActorRef, Props, Stash}
import akka.pattern.pipe
import org.gammf.collabora.database._
import org.gammf.collabora.database.messages._
import org.gammf.collabora.util.Note
import org.gammf.collabora.yellowpages.ActorService.ActorService
import org.gammf.collabora.yellowpages.messages.RegistrationResponseMessage
import reactivemongo.bson.{BSON, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import org.gammf.collabora.yellowpages.util.Topic
import org.gammf.collabora.yellowpages.TopicElement._
import org.gammf.collabora.yellowpages.ActorService._
import org.gammf.collabora.yellowpages.util.Topic.ActorTopic

/**
  * A worker that performs query on notes.
  */
class DBWorkerNotesActor(override val yellowPages: ActorRef,
                         override val name: String,
                         override val topic: ActorTopic,
                         override val service: ActorService = DefaultWorker)
  extends CollaborationsDBWorker[DBWorkerMessage] with Stash with DefaultDBWorker {

  override def receive: Receive = ({
    //TODO consider: these three methods in super class?
    case message: RegistrationResponseMessage => getActorOrElse(Topic() :+ Database, ConnectionHandler, message)
      .foreach(_ ! AskConnectionMessage())

    case message: GetConnectionMessage =>
      connection = Some(message.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: InsertNoteMessage =>
      val bsonNote: BSONDocument = BSON.write(message.note) // necessary conversion, sets the noteID
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$push" -> BSONDocument(COLLABORATION_NOTES -> bsonNote)),
        okMessage = QueryOkMessage(InsertNoteMessage(bsonNote.as[Note], message.collaborationID, message.userID)),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: UpdateNoteMessage =>
      update(
        selector = BSONDocument(
          COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get,
          COLLABORATION_NOTES + "." + NOTE_ID -> BSONObjectID.parse(message.note.id.get).get
        ),
        query = BSONDocument("$set" -> BSONDocument(COLLABORATION_NOTES + ".$" -> message.note)),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

    case message: DeleteNoteMessage =>
      update(
        selector = BSONDocument(COLLABORATION_ID -> BSONObjectID.parse(message.collaborationID).get),
        query = BSONDocument("$pull" -> BSONDocument(COLLABORATION_NOTES ->
          BSONDocument(NOTE_ID -> BSONObjectID.parse(message.note.id.get).get))),
        okMessage = QueryOkMessage(message),
        failStrategy = defaultDBWorkerFailStrategy(message.userID)
      ) pipeTo sender

  }: Receive) orElse super[CollaborationsDBWorker].receive
}

object DBWorkerNotesActor {
  /**
    * Factory methods that return a [[Props]] to create a database worker notes registered actor
    * @param yellowPages the reference to the yellow pages root actor.
    * @param topic the topic to which this actor is going to be registered.
    * @return the [[Props]] to use to create a database worker notes actor.
    */

  def printerProps(yellowPages: ActorRef, topic: ActorTopic, name: String = "DBWorkerNotes") : Props =
    Props(new DBWorkerNotesActor(yellowPages = yellowPages, name = name, topic = topic))
}