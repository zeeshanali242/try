package service

import java.sql.{ PreparedStatement, Timestamp }
import securesocial.core._
import play.api.{ Play, Logger, Application }
import scala.Some
import play.libs.{ F, Akka }
import akka.actor.Cancellable
import org.postgresql.util.PSQLException
import securesocial.core.{ IdGenerator, Token }
import org.joda.time.DateTime
import java.security.SecureRandom

object trynexUserService {

  implicit val implapplication = play.api.Play.current
  val TokenDurationKey = "securesocial.userpass.tokenDuration"
  val DefaultDuration = 60
  val TokenDuration = Play.current.configuration.getInt(TokenDurationKey).getOrElse(DefaultDuration)

  def find(id: Long): Option[SocialUser] = {
    globals.userModel.findUserById(id)
  }

  def userExists(email: String, mobile: String): Boolean = {
    globals.userModel.userExists(email, mobile)
  }

  def userExistsEmail(email: String): Boolean = {
    globals.userModel.userExistsByEmail(email)
  }

  def userExistsByMobile(mobile: String): Boolean = {
    globals.userModel.userExistsByMobile(mobile)
  }

  def userSmsOtpCheck(id: Long, otp: Integer): Boolean = {
    globals.userModel.userSmsOtpCheck(id, otp)
  }

  def generateOTP(): String = {
    val random = new SecureRandom
    (random.nextInt(9) + 1).toString concat "%05d".format(random.nextInt(100000));
  }
  def create(user: SocialUser, password: String, token: String, pgp: String): SocialUser = {

    Logger.info("====in create of trynexUserService======")
    val pgp_key = PGP.parsePublicKey(pgp).map(_._2)
    val user_id = globals.userModel.create(user.email, user.first_name, user.last_name, user.mobile_number, password, user.onMailingList, pgp_key, token)

    user_id match {
      case Some(id) => {

        user.copy(id = id, pgp = pgp_key)
      }
      case None => throw new Exception(" Duplicate Email ")
    }
  }

  def save(user: SocialUser): SocialUser = {
    globals.userModel.saveUser(user.id, user.email, user.onMailingList)
    user
  }

  // this function requires higher database privileges
  def resetPass(email: String, token: String, password: String) {
    globals.userModel.userResetPassComplete(email, token, password)
  }

  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Option[Token] = {
    globals.userModel.findToken(token)
  }

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String) {
    globals.userModel.deleteToken(uuid)
  }

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() {
    globals.userModel.deleteExpiredTokens()
    globals.userModel.deleteExpiredTOTPBlacklistTokens()
  }

  var cancellable: Option[Cancellable] = None
  val DefaultInterval = 5
  val DeleteIntervalKey = "securesocial.userpass.tokenDeleteInterval"

  def onStart() {
    import scala.concurrent.duration._
    import play.api.libs.concurrent.Execution.Implicits._
    val i = play.api.Play.current.configuration.getInt(DeleteIntervalKey).getOrElse(DefaultInterval)

    cancellable = if (UsernamePasswordProvider.enableTokenJob) {
      Some(
        Akka.system.scheduler.schedule(1.seconds, i.minutes) {
          if (Logger.isDebugEnabled) {
            Logger.debug("[securesocial] calling deleteExpiredTokens()")
          }
          try {
            deleteExpiredTokens()
          } catch {
            case e: PSQLException => // Ignore failures (they can be caused by a connection that just closed and hopefully next time we'll get a valid connection
          }
        }
      )
    } else None

    Logger.info("[securesocial] loaded user service: %s".format(this.getClass))
  }

  def onStop() {
    cancellable.map(_.cancel())
  }
  def createToken(email: String, isSignUp: Boolean, language: String, first_name: String, last_name: String, mobile_number: String, password: String) = {
    val tokenId = IdGenerator.generateEmailToken
    val now = DateTime.now

    val token = Token(
      tokenId, email,
      now,
      now.plusMinutes(TokenDuration),
      isSignUp = isSignUp,
      language, first_name, last_name, mobile_number, password
    )

    globals.userModel.saveToken(token)

    tokenId
  }

  def createWithdrawalToken(id: Long) = {
    val token = IdGenerator.generateEmailToken
    val expiration = DateTime.now.plusMinutes(TokenDuration)
    globals.userModel.saveWithdrawalToken(id, token, expiration)

    token
  }
}