
package usertrust

import play.api.db.DB
import securesocial.core.Token
import java.sql.Timestamp
import play.api.Play.current
import models.Withdrawal
import org.joda.time.DateTime
import anorm._

class UserTrustModel(val db: String = "default") {
  def getTrustedActionRequests = DB.withConnection(db) { implicit c =>
    SQL"""select email, is_signup, language, first_name, last_name, mobile_number from trusted_action_requests"""().map(row =>
      (row[String]("email"), row[Boolean]("is_signup"), row[String]("language"), row[String]("first_name"), row[String]("last_name"), row[String]("mobile_number"))
    ).toList
  }
  def getPendingWithdrawalRequests = DB.withConnection(db) { implicit c =>
    SQL"""select w.*, u.email, u.id as uid, u.pgp, wc.address as destination from withdrawals w
       inner join users u on w.user_id = u.id
       left join withdrawals_crypto wc on w.id = wc.id
       where confirmation_token is null
      """().map(row =>
      (
        Withdrawal(
          row[Long]("id"),
          row[BigDecimal]("amount").bigDecimal.toPlainString,
          row[BigDecimal]("fee").bigDecimal.toPlainString,
          row[DateTime]("created"),
          "",
          row[String]("currency")
        ),
          row[String]("email"),
          row[Long]("uid"),
          row[Option[String]]("pgp"),
          row[Option[String]]("destination")
      )
    ).toList
  }

  def saveWithdrawalToken(id: Long, token: String, expiration: DateTime) = DB.withConnection(db) { implicit c =>
    SQL"""update withdrawals set confirmation_token = $token, token_expiration = ${new Timestamp(expiration.getMillis)} where id = $id""".execute
  }

  def trustedActionFinish(email: String, is_signup: Boolean) = DB.withConnection(db) { implicit c =>
    SQL"""delete from trusted_action_requests where email = $email and is_signup = $is_signup""".execute
  }

  def saveToken(token: Token) = DB.withConnection(db) { implicit c =>
    SQL"""
   insert into tokens (token, email, creation, expiration, is_signup, language, first_name, last_name, mobile_number)
   values (${token.uuid}, ${token.email}, ${new Timestamp(token.creationTime.getMillis)}, ${new Timestamp(token.expirationTime.getMillis)}, ${token.isSignUp}, ${token.language}, ${token.first_name}, ${token.last_name}, ${token.mobile_number})
   """.execute
  }
}
