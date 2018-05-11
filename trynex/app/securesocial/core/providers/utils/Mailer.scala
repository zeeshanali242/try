
package securesocial.core.providers.utils

import securesocial.core.SocialUser
import org.joda.time.DateTime
import play.api.{ Play, Logger }
import Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc.RequestHeader
import play.api.i18n.Messages
import play.twirl.api.{ Html, Txt }
import org.apache.commons.mail.{ DefaultAuthenticator, SimpleEmail, MultiPartEmail, EmailAttachment }
import java.io.File
import javax.mail.internet.InternetAddress
import service.PGP
import org.apache.commons.mail.HtmlEmail
/**
 * A helper class to send email notifications
 */
object Mailer {
  val fromAddress = current.configuration.getString("smtp.from").get
  val WithdrawalConfirmSubject = "mails.sendWithdrawalConfirmEmail.subject"
  val AlreadyRegisteredSubject = "mails.sendAlreadyRegisteredEmail.subject"
  val SignUpEmailSubject = "mails.sendSignUpEmail.subject"
  val WelcomeEmailSubject = "mails.welcomeEmail.subject"
  val PasswordResetSubject = "mails.passwordResetEmail.subject"
  val PasswordResetOkSubject = "mails.passwordResetOk.subject"
  val withdrawAdminEmail = "mails.passwordResetOk.admin_email"
  val userLoginOTP = "mails.login.otp"
  val userSignUpOTP = "mails.signup.otp"
  val userLoginInfo = "mails.userLoginInfo.subject"
  val supportMailInfo = "mails.supportMailInfo.subject"
  val orderExportSubject = "mails.orderEmailAttachment.subject"
  val walletExportSubject = "mails.walletEmailAttachment.subject"

  def sendExportData(email: String, attach: EmailAttachment, typeExport: String)(implicit request: RequestHeader, messages: Messages) {
    var txtAndHtml = views.txt.auth.mails.dataExportAttachmentEmail(email, typeExport)
    //sendEmail(Messages(PasswordResetOkSubject), email, txtAndHtml, pgp)
    if (typeExport == "Order") {
      sendEmailWithFile(Messages(orderExportSubject), email, txtAndHtml.toString(), attach)
    } else {
      sendEmailWithFile(Messages(walletExportSubject), email, txtAndHtml.toString(), attach)
    }
  }

  def sendSupportMail(address: String, s_name: String, s_email: String, s_phone: String, s_category: String, s_description: String)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.adminEmail(address, s_name, s_email, s_phone, s_category, s_description)), None)
    sendEmail(Messages(supportMailInfo), address, txtAndHtml, None)
  }

  def sendWithdrawalAdminEmail(email: String, amount: String, currency: String, userEmail: String, address: String, first_name: String, last_name: String)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.withdrawEmail(email, amount, currency, userEmail, address, first_name, last_name)), None)
    sendEmail(Messages(withdrawAdminEmail), email, txtAndHtml, None)
  }

  def sendUserOTP(first_name: String, email: String, otp: String)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.userLoginOTP(first_name, email, otp)), None)
    sendEmail(Messages(userLoginOTP), email, txtAndHtml, None)

  }
  def sendUserSignUpOTP(email: String, otp: String)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.userSignUpInfo(otp)), None)
    sendEmail(Messages(userSignUpOTP), email, txtAndHtml, None)
  }

  def sendUserInfoEmail(first_name: String, email: String, creationDate: String, ip: String, os: String, browser: String)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.userInfoEmail(first_name, email, creationDate, ip, os, browser)), None)
    //Logger.info("================= sending email to ================== " + email)

    sendEmail(Messages(userLoginInfo), email, txtAndHtml)
  }
  def sendWithdrawalConfirmEmail(email: String, amount: String, currency: String, destination: String, id: Long, token: String, pgp: Option[String])(implicit messages: Messages) {
    val url_confirm = "%s/%s/%s".format(current.configuration.getString("url.withdrawal_confirm").getOrElse("http://localhost:9000/withdrawal_confirm"), id, token)
    val url_reject = "%s/%s/%s".format(current.configuration.getString("url.withdrawal_reject").getOrElse("http://localhost:9000/withdrawal_reject"), id, token)
    val txtAndHtml = (Some(views.txt.auth.mails.withdrawalConfirmEmail(email, amount, currency, destination, id, token, url_confirm, url_reject)), None)
    sendEmail(Messages(WithdrawalConfirmSubject), email, txtAndHtml, pgp)
  }

  def sendRefillWalletEmail(email: String, currency: String, nodeId: Int, balance: BigDecimal, balanceTarget: BigDecimal)(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.refillWalletEmail(email, currency, nodeId, balance, balanceTarget)), None)
    sendEmail(s"Refill $currency wallet $nodeId", email, txtAndHtml)
  }

  def sendAlreadyRegisteredEmail(url: String, email: String, pgp: Option[String])(implicit messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.alreadyRegisteredEmail(email, url)), None)
    sendEmail(Messages(AlreadyRegisteredSubject), email, txtAndHtml, pgp)
  }

  def sendSignUpEmail(urlTo: String, to: String, token: String)(implicit messages: Messages) {
    val url = urlTo + "/" + token;
    val txtAndHtml = (Some(views.txt.auth.mails.signUpEmail(token, url)), None)
    sendEmail(Messages(SignUpEmailSubject), to, txtAndHtml)
  }

  def sendWelcomeEmail(user: SocialUser)(implicit request: RequestHeader, messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.welcomeEmail(user)), None)
    sendEmail(Messages(WelcomeEmailSubject), user.email, txtAndHtml, user.pgp)
  }

  def sendPasswordResetEmail(email: String, url: String, token: String, pgp: Option[String])(implicit messages: Messages) {
    var resetUrl = url + "/reset/" + token
    val txtAndHtml = (Some(views.txt.auth.mails.passwordResetEmail(email, resetUrl)), None)
    sendEmail(Messages(PasswordResetSubject), email, txtAndHtml, pgp)
  }

  def sendPasswordChangedNotice(email: String, pgp: Option[String])(implicit request: RequestHeader, messages: Messages) {
    val txtAndHtml = (Some(views.txt.auth.mails.passwordChangedNotice(email)), None)
    sendEmail(Messages(PasswordResetOkSubject), email, txtAndHtml, pgp)
  }

  private def sendEmail(subject: String, recipient: String, body: (Option[Txt], Option[Html]), pgp: Option[String] = None) {
    import scala.concurrent.duration._
    import play.api.libs.concurrent.Execution.Implicits._

    if (Logger.isDebugEnabled) {
      Logger.debug("[securesocial] sending email to %s".format(recipient))
    }
    val strBody = pgp match {
      case Some(pgp_key) => PGP.simple_encrypt(pgp_key, body._1.get.toString())
      case None => body._1.get.toString()
    }

    Akka.system.scheduler.scheduleOnce(1.seconds) {

      val smtpHost = Play.current.configuration.getString("smtp.host").getOrElse(throw new RuntimeException("smtp.host needs to be set in application.conf in order to use this plugin (or set smtp.mock to true)"))
      val smtpPort = Play.current.configuration.getInt("smtp.port").getOrElse(25)
      val smtpSsl = Play.current.configuration.getBoolean("smtp.ssl").getOrElse(false)
      val smtpUser = Play.current.configuration.getString("smtp.user").get
      val smtpPassword = Play.current.configuration.getString("smtp.password").get
      val smtpLocalhost = current.configuration.getString("smtp.localhost").get
      val email = new SimpleEmail()

      email.setMsg(strBody)
      email.setContent(strBody, "text/html")
      email.setHostName(smtpLocalhost)

      //TODO: move this somewhere better
      System.setProperty("mail.smtp.localhost", current.configuration.getString("smtp.localhost").get)
      email.setCharset("utf-8")
      email.setSubject(subject)
      setAddress(fromAddress) { (address, name) => email.setFrom(address, name) }
      email.addTo(recipient, null)
      email.setHostName(smtpHost)
      email.setSmtpPort(smtpPort)
      email.setSSLOnConnect(smtpSsl)
      email.setAuthentication(smtpUser, smtpPassword)
      try {
        email.send
      } catch {
        case ex: Throwable => {
          // important: Print the bodies of emails in logs only if dealing with fake money
          if (Play.current.configuration.getBoolean("fakeexchange").get) {
            Logger.debug("Failed to send email to %s, subject: %s, email body:\n%s".format(recipient, subject, strBody))
          }
          throw ex
        }
      }
    }
  }

  /**
   * Extracts an email address from the given string and passes to the enclosed method.
   * https://github.com/typesafehub/play-plugins/blob/master/mailer/src/main/scala/com/typesafe/plugin/MailerPlugin.scala
   *
   * @param emailAddress
   * @param setter
   */
  private def setAddress(emailAddress: String)(setter: (String, String) => Unit) = {

    if (emailAddress != null) {
      try {
        val iAddress = new InternetAddress(emailAddress)
        val address = iAddress.getAddress
        val name = iAddress.getPersonal

        setter(address, name)
      } catch {
        case e: Exception =>
          setter(emailAddress, null)
      }
    }
  }

  // XXX: currently not used
  def sendEmailWithFile(subject: String, recipient: String, body: String, attachment: EmailAttachment) {
    import scala.concurrent.duration._
    import play.api.libs.concurrent.Execution.Implicits._

    if (Logger.isDebugEnabled) {
      Logger.debug("[securesocial] sending email to %s".format(recipient))
      Logger.debug("[securesocial] mail = [%s]".format(body))
    }

    Akka.system.scheduler.scheduleOnce(1.seconds) {
      // we can't use the plugin easily with multipart emails
      val email = new MultiPartEmail
      email.setHostName(current.configuration.getString("smtp.host").get)
      //TODO: move this somewhere better
      System.setProperty("mail.smtp.localhost", current.configuration.getString("smtp.localhost").get)
      email.attach(attachment)
      email.setSubject(subject)
      email.addTo(recipient)
      email.setBoolHasAttachments(true)
      email.setSmtpPort(current.configuration.getInt("smtp.port").getOrElse(25))
      email.setSSLOnConnect(current.configuration.getBoolean("smtp.ssl").get)
      email.setAuthentication(current.configuration.getString("smtp.user").get, current.configuration.getString("smtp.password").get)
      setAddress(fromAddress) { (address, name) => email.setFrom(address, name) }
      email.setMsg(body)
      email.send()
      new File(attachment.getPath).delete()
    }
  }
}
