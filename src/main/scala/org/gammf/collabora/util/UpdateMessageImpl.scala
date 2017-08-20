package org.gammf.collabora.util

import org.gammf.collabora.util.UpdateMessageTarget.UpdateMessageTarget
import org.gammf.collabora.util.UpdateMessageType.UpdateMessageType
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

/**
  * Simple implementation of an update message
  * @param target the target of the message (note, module, collaboration, member)
  * @param messageType the type of the message (creation, update, deletion)
  * @param user the user that creates the update
  * @param note the note affected
  * @param module the module affected
  * @param collaboration the collaboration affected, used for containing the collaboration id
  * @param member the collaboration member affected
  */
case class UpdateMessageImpl(target: UpdateMessageTarget, messageType: UpdateMessageType, user: String, note: Option[SimpleNote] = None,
                             module: Option[SimpleModule] = None, collaboration: Option[SimpleCollaboration] = None,
                             member: Option[CollaborationUser] = None, collaborationId: Option[String]) extends UpdateMessage {

}
object UpdateMessageImpl {
  implicit val updateMessageReads: Reads[UpdateMessageImpl] = (
    (JsPath \ "target").read[UpdateMessageTarget] and
      (JsPath \ "messageType").read[UpdateMessageType] and
      (JsPath \ "user").read[String] and
      (JsPath \ "note").readNullable[SimpleNote] and
      (JsPath \ "module").readNullable[SimpleModule] and
      (JsPath \ "collaboration").readNullable[SimpleCollaboration] and
      (JsPath \ "member").readNullable[CollaborationUser] and
      (JsPath \ "collaborationId").readNullable[String]
    )(UpdateMessageImpl.apply _)

  implicit val updateMessageWrites: Writes[UpdateMessageImpl] = (
    (JsPath \ "target").write[UpdateMessageTarget] and
      (JsPath \ "messageType").write[UpdateMessageType] and
      (JsPath \ "user").write[String] and
      (JsPath \ "note").writeNullable[SimpleNote] and
      (JsPath \ "module").writeNullable[SimpleModule] and
      (JsPath \ "collaboration").writeNullable[SimpleCollaboration] and
      (JsPath \ "member").writeNullable[CollaborationUser] and
      (JsPath \ "collaborationId").writeNullable[String]
    ) (unlift(UpdateMessageImpl.unapply))
}