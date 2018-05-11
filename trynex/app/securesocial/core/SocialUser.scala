
package securesocial.core

import play.api.libs.json.{ JsValue, Json, JsObject, Writes }
import controllers.ProviderController

/**
 * An implementation of Identity.  Used by SecureSocial to gather user information when users sign up and/or sign in.
 */
case class SocialUser(id: Long, email: String, first_name: String, last_name: String, mobile_number: String, verification: Int, language: String, onMailingList: Boolean = false, TFAEnabled: Boolean = false, pgp: Option[String] = None, kyc: String = "notApplied")

object SocialUser {
  implicit def writes = new Writes[SocialUser] {
    def writes(u: SocialUser): JsValue = {
      // include everything except the id
      Json.obj("email" -> u.email, "verification" -> u.verification, "onMailingList" -> u.onMailingList,
        "TFAEnabled" -> u.TFAEnabled, "pgp" -> u.pgp, "language" -> u.language, "kyc" -> u.kyc)
    }
  }
}
