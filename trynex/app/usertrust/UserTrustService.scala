

package usertrust

import java.io.File

import play.Environment
import play.api.i18n._
import securesocial.core.providers.utils.Mailer
import securesocial.core.{ IdGenerator, Token }
import org.joda.time.DateTime
import play.api.{ Mode, Play }
import akka.actor.{ Actor, Props }
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import service.trynexUserService
import play.api.Play.current

class UserTrustService(val model: UserTrustModel) extends Actor {
  import context.system
  implicit val ec: ExecutionContext = system.dispatcher

  val TokenDurationKey = "securesocial.userpass.tokenDuration"
  val DefaultDuration = 60
  val TokenDuration = Play.current.configuration.getInt(TokenDurationKey).getOrElse(DefaultDuration)

  val trustedActionTimer = system.scheduler.schedule(15.seconds, 15.seconds)(processTrustedActionRequests())

  // Warning: It is not safe to have two user trust services running at the same time
  def processTrustedActionRequests() {

    /*for ((email, is_signup, language, first_name, last_name, mobile_number) <- model.getTrustedActionRequests) {
      // XXX: temporary hack to make languages work in emails
      implicit val messages = new Messages(Lang.get(language).getOrElse(Lang.defaultLang),
        new DefaultMessagesApi(play.api.Environment.simple(new File("."), Mode.Prod),
          play.api.Play.current.configuration,
          new DefaultLangs(play.api.Play.current.configuration)
        )
      )

      // send email to the user
      if (is_signup) {
        // check if there is already an account for this email address

        trynexUserService.userExists(email) match {
          case true => {
            // user signed up already, send an email offering to login/recover password
            Mailer.sendAlreadyRegisteredEmail(email, globals.userModel.userPgpByEmail(email))
          }
          case false => {
            val token = createToken(email, isSignUp = is_signup, language, first_name, last_name, mobile_number)
            Mailer.sendSignUpEmail(email, token)
          }
        }
      } else {
        // create and save token
        val token = createToken(email, isSignUp = is_signup, language, first_name, last_name, mobile_number)
        Mailer.sendPasswordResetEmail(email, token, globals.userModel.userPgpByEmail(email))
      }
      // remove the token from the queue
      model.trustedActionFinish(email, is_signup)
    }*/
    /*for ((withdrawal, email, uid, pgp, destination) <- model.getPendingWithdrawalRequests) {
      // figure out the user's language and create a hacky messages object
      val user = globals.userModel.findUserById(uid).get
      // XXX: temporary hack to make languages work in emails
      implicit val messages = new Messages(Lang.get(user.language).getOrElse(Lang.defaultLang),
        new DefaultMessagesApi(play.api.Environment.simple(new File("."), Mode.Prod),
          play.api.Play.current.configuration,
          new DefaultLangs(play.api.Play.current.configuration)
        )
      )

      // create and save token
      val token = createWithdrawalToken(withdrawal.id)
      // send withdrawal confirmation email
      Mailer.sendWithdrawalConfirmEmail(email, withdrawal.amount, withdrawal.currency, destination.getOrElse("unknown"), withdrawal.id, token, pgp)
    }*/
  }

  /*private def createToken(email: String, isSignUp: Boolean, language: String, first_name: String, last_name: String, mobile_number: String) = {
    val tokenId = IdGenerator.generateEmailToken
    val now = DateTime.now

    val token = Token(
      tokenId, email,
      now,
      now.plusMinutes(TokenDuration),
      isSignUp = isSignUp,
      language, first_name,
      last_name, mobile_number
    )
    model.saveToken(token)

    tokenId
  }
*/
  private def createWithdrawalToken(id: Long) = {
    val token = IdGenerator.generateEmailToken
    val expiration = DateTime.now.plusMinutes(TokenDuration)
    model.saveWithdrawalToken(id, token, expiration)

    token
  }

  def receive = {
    case _ =>
  }
}

object UserTrustService {
  def props(model: UserTrustModel) = {
    Props(classOf[UserTrustService], model)
  }
}