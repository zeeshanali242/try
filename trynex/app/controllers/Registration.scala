
package controllers

import _root_.java.util.UUID
import javax.inject.Inject
import play.api.mvc.{ Result, Action, Controller, _ }
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.{ Play, Logger }
import play.api.i18n.{ Lang, MessagesApi, I18nSupport, Messages }
import securesocial.core._
import Play.current
import securesocial.core.providers.utils._
import org.joda.time.DateTime
import scala.language.reflectiveCalls
import securesocial.core.Token
import scala.Some
import securesocial.core.SocialUser
import service.{ PGP, trynexUserService }
import models.{ LogType, LogEvent }
import java.security.SecureRandom
import securesocial.core.providers.utils.Mailer
import usertrust.UserTrustService
import org.bouncycastle.openssl._;
import org.bouncycastle.openssl.jcajce._;
import play.api.libs.json._
import java.security._
import java.io._
import java.util.Formatter

import play.api.i18n.MessagesApi
/**
 * A controller to handle user registration.
 *
 */
class Registration @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {
  import controllers.Registration._
  import PasswordChange._

  lazy val registrationEnabled = current.configuration.getBoolean(RegistrationEnabled).getOrElse(true)

  val form = Form[RegistrationInfo](
    mapping(
      AcceptTos -> boolean.verifying(acceptTos => acceptTos),
      MailingList -> boolean,
      Password ->
        tuple(
          Password1 -> nonEmptyText.verifying(Messages(passwordErrorStr, passwordMinLen), passwordErrorFunc _),
          Password2 -> nonEmptyText
        ).verifying(Messages(PasswordsDoNotMatch), passwords => passwords._1 == passwords._2),
      Pgp -> text.verifying(Messages(PgpKeyInvalid), pgp => pgp == "" || PGP.parsePublicKey(pgp).isDefined)
    ) // binding
    ((_, list, password, pgp) => RegistrationInfo(list, password._1, pgp)) // unbinding
    (info => Some(true, info.mailingList, ("", ""), info.pgp))
  )

  /* val loginForm = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )
  )*/

  def generateHashForMessage(message: String): String =
    try {
      /*TODO - Kindly store the key in a secure area  */

      println("TEST1")
      var privKey: String = ""
      privKey += "-----BEGIN RSA PRIVATE KEY-----\n"
      privKey += "MIIEowIBAAKCAQEAzcwX4G00jaets0mJknH/acMq++0AhIxkb8rrx2kUPQLD6mL8\n"
      privKey += "SMLLWq+FhEiyCjfbVL78xOMSRkbsuydLBp3oUaJvd30lrXXJOlfjAgE38VDn60SQ\n"
      privKey += "jajxBHDYtfNPXXM9arJlH2XoBt+KfwEVESd2xwGlb0t2HV/LQMHJxxRl9kC5ff3l\n"
      privKey += "9NqgGVm8aQRaI7AJc6ZBdROjZGiGiIrsprzzdRJkEJnom3klCZceo7lILRdivXku\n"
      privKey += "oW5HtJIQNlKcvtJwOjyxg56VbkJxaVxtFMYqIyzHZYf0b9KAmnXdnsWHRgdgk96v\n"
      privKey += "zRh7Q2Eipy1NcQSJr0vdpzzB1Zju4GrqMSkkHwIDAQABAoIBAQCjNQaCf1i8Nox0\n"
      privKey += "sQ8fSrTqJVODc1ODyuskFWOjQ1w/fl/tFA9LjOBEzQov/I7lt6KDtOs1IXeusDSx\n"
      privKey += "v9mqJ7TEePO5aVBmHhE16dkoD9tTz3v9guS405BAm1XiBlGcpPXCFjRIEENQoBtv\n"
      privKey += "2WXhstBpxo5ykv/bD8tbUdQ5w52RChtowDRnhOh+6LmAoRVcD/OVBDoIh79n5onU\n"
      privKey += "XX5OSqzcOKa1Xpac50zrYSsf2Yvu6vaBpBInvz5hOIXQjtbnF1LTlcVN0DDgS9EF\n"
      privKey += "iIcgZVoU3mOp7e2sl+DJyMHkbLuAkJT9C7K4oZkn7BHIKSEbXhEM4RvU7gtQlR78\n"
      privKey += "yOlx9okBAoGBAOrXldEUgklDvQAeuAPaecIAeVffkpGgRd5Qj35iWzm6ELOSZdFO\n"
      privKey += "Rdz4eyH1tTs2Q7aj04UX8vslevl1rt33Mxtxj9jhQjqz5XrJhlz071gFm36MBJIk\n"
      privKey += "ApHXa0RZsqSOkEdmQSLax14cz30fQa/Ecmwc6y7lL+8oaNARgif2Z2dfAoGBAOBW\n"
      privKey += "nV7FSZycSuuWpCPIoRiabtSoFZqyqOs2H6ZcN23c6BT2y0QJrnNPSfWkHLgEGggR\n"
      privKey += "H4Lh3lTSW9gkwL/dHcexN4Fxwg21uElHMsm20wQRuWVCM+C7b+C5yyE6hSsYk6Yj\n"
      privKey += "nPr4vPlxXzrw7asl3wVVXoCG1fsVImjmD0uFCztBAoGANHtVWdJRg3oF5N74lLPg\n"
      privKey += "fgCJHaAzKyQ8OQCb8MyeQnpYfSj8ZBgv+L/3FJHKnJ715v0Zqia+AG5R2yn3mFdE\n"
      privKey += "Lp/kW72LhX7qi9Q5mNCMJImsRE2aP+aYRGt152J8T9YkXDB34ggugdPCct3nWhZ2\n"
      privKey += "075quKIzYikPs2AWTEP+u9UCgYBF/P+vt2EVyPTetuqSd18668Mz+RR0ZNSqPQJ2\n"
      privKey += "xkJMtiR5ld0oZtTUCKKMThzfk/gDGER6crkIQXCB6EVyFivaRwGIEtN1r4HE6r9/\n"
      privKey += "itgeZuEuJA9HR3LJ62zh+v3cyhgWNvocmklqkOIi41Nil7gSU+XdtzM+2AMaMtwG\n"
      privKey += "tYUhgQKBgByYCXu3+j0+QYHg2Wwu8sNUXC8h+7hcDGk02Qh3J67L89RR21iyODeZ\n"
      privKey += "QndPEQc8soBLSKH0FxrMaROsMMgxpZzxwWV50LFFiJvnmEOG5dDuZgLpz1VPVVpe\n"
      privKey += "j/MpwqAanoW4wMst+G+fVyfdoMXHSu98m1Wx8npqHD1OBY/LKePK\n"
      privKey += "-----END RSA PRIVATE KEY-----\n"
      println("TEST2")
      java.security.Security.addProvider(
        new org.bouncycastle.jce.provider.BouncyCastleProvider())
      println("Key [" + privKey + "]")
      val parser: PEMParser = new PEMParser(new StringReader(privKey))
      val pemPair: PEMKeyPair = parser.readObject().asInstanceOf[PEMKeyPair]
      val keyPair: KeyPair =
        new JcaPEMKeyConverter().setProvider("BC").getKeyPair(pemPair)
      val privateKey: PrivateKey = keyPair.getPrivate
      val sign: Signature = Signature.getInstance("SHA512withRSA")
      sign.initSign(privateKey)
      println("TEST3")
      sign.update(message.getBytes)
      val hashBytes: Array[Byte] = sign.sign()
      bytes2hex(hashBytes)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        ""
      }

    }

  /*  private def bytesToHexString(bytes: Array[Byte]): String = {
    val sb: StringBuilder = new StringBuilder(bytes.length * 2)
    var st = new String();
    for (b <- bytes) {
      var st1 = String.format("%02X", b);
      st = st + String.format("%02x", b);
    }

    st
  }*/

  def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
    // bytes.foreach(println)
  }

  def paymentGatewayProcess = Action(parse.json) { implicit request =>
    var res = "user"
    try {
      val body = request.body
      (for (
        amount <- (body \ "amount").validate[String]

      ) yield {
        Logger.info(">>>>>>>>>>>>>> amount is " + amount);
        val invoiceId = "INV00" + trynexUserService.generateOTP();

        Logger.info(">>>>>>>>>>>>>> invoiceId is " + invoiceId);
        val api_key = "08Z1782051U62BY9OUGW4XM67GF2004";
        val id = "FPTEST";
        val merchant_id = "FPTEST";
        val invoice_amt = amount;
        val merchant_display = "fonePaisa TEST MERCHANT";
        var hash = generateHashForMessage(api_key + "#" + id + "#" + merchant_id + "#" + invoiceId + "#" + invoice_amt + "#");
        Logger.info(">>>>>>>>>>>>>> hash is " + hash);

        val anotherJson: JsValue = Json.toJson(hash + "," + invoiceId)
        Ok(anotherJson)
      }).getOrElse(

        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
      )

    } catch {

      case _: Throwable =>
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoplacebid")))
    }
  }

  /*val emailForm = Form[StartRegistrationInfo](
    mapping(
      Email -> email.verifying(nonEmpty),
      FirstName -> text.verifying(nonEmpty),
      LastName -> text,
      MobileNumber -> text,
      Password -> text,
      Otp -> text

    ) // binding
    ((email, first_name, last_name, mobile_number, password, send_otp) => StartRegistrationInfo(email, first_name, last_name, mobile_number, password, send_otp)) //unbinding
    (info => Some(info.email, info.first_name, info.last_name, info.mobile_number, info.password, send_otp))
  )*/

  val resetEmailForm = Form[PasswordResetInfo](
    mapping(
      Email -> email.verifying(nonEmpty)
    ) // binding
    ((email) => PasswordResetInfo(email)) //unbinding
    (info => Some(info.email))
  )

  val startForm = Form[StartRegistrationInfo](
    mapping(
      Email -> email.verifying(nonEmpty),
      FirstName -> text,
      LastName -> text,
      MobileNumber -> text,
      Password -> text,
      Otp -> text
    ) // binding
    ((email, first_name, last_name, mobile_number, password, otp) => StartRegistrationInfo(email, first_name, last_name, mobile_number, password, otp)) //unbinding
    (info => Some(info.email, info.first_name, info.last_name, info.mobile_number, info.password, info.otp)) /*((email, first_name, last_name, mobile_number, password) => StartRegistrationInfo(email, first_name, last_name, mobile_number, password)) //unbinding
   (info => Some(info.email, info.first_name, info.last_name, info.mobile_number, info.password))
*/

  )
  val SupportTicketForm = Form[SupportTicketInfo](
    mapping(
      Name -> text,
      Email -> email.verifying(),
      Phone -> text,
      Category -> text,
      Description -> text
    ) // binding
    ((name, email, phone, category, description) => SupportTicketInfo(name, email, phone, category, description)) //unbinding
    (info => Some(info.name, info.email, info.phone, info.category, info.description))

  )

  val changePasswordForm = Form(
    Password ->
      tuple(
        Password1 -> nonEmptyText.verifying(Messages(passwordErrorStr, passwordMinLen), passwordErrorFunc _),
        Password2 -> nonEmptyText
      ).verifying(Messages(PasswordsDoNotMatch), passwords => passwords._1 == passwords._2)
  )

  def support = Action { implicit request =>
    SupportTicketForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.exchange.support_ticket(errors))
      },
      email => {

        val s_name = email.name
        val s_email = email.email
        val s_phone = email.phone
        val s_category = email.category
        val s_description = email.description
        /*Logger.info("value---------------------------" + e)
        */
        //fetch user email id
        Mailer.sendSupportMail("adithihdgowda@gmail.com", s_name, s_email, s_phone, s_category, s_description)
        Ok(views.html.exchange.support_ticket(SupportTicketForm))

      }
    )
  }

  def support_ticket = Action { implicit request =>
    Ok(views.html.exchange.support_ticket(SupportTicketForm))
  }

  /**
   * Starts the sign up process
   */
  def startSignUp = Action { implicit request =>
    if (registrationEnabled) {
      if (SecureSocial.enableRefererAsOriginalUrl) {
        SecureSocial.withRefererAsOriginalUrl(Ok(views.html.auth.Registration.startSignUp(startForm)))
      } else {
        Ok(views.html.auth.Registration.startSignUp(startForm))
      }
    } else NotFound
  }

  def sendOtp(mobile: String) = Action { implicit request =>
    Logger.info("-----inside sendOtp")
    if (registrationEnabled) {
      Logger.info("-----inside sendOtp")
      val otp = trynexUserService.generateOTP().toInt;
      Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>  OTP is " + otp + "mobile number" + mobile);

      trynexUserService.userExistsByMobile(mobile) match {
        case true => {
          Logger.info("=============user exists=================")
        }
        case false => {
          // The user wasn't registered. Oh, well.
        }
      }

      if (SecureSocial.enableRefererAsOriginalUrl) {
        SecureSocial.withRefererAsOriginalUrl(Ok(views.html.auth.Registration.startSignUp(startForm)))
      } else {
        Ok(views.html.auth.Registration.startSignUp(startForm))
      }
    } else NotFound

  }

  private def badRequest[A](f: Form[controllers.Registration.StartRegistrationInfo], request: Request[A], msg: Option[String] = None): Result = {
    implicit val r = request
    Results.BadRequest(views.html.auth.Registration.startSignUp(f, msg))
  }

  val random = new SecureRandom()

  def handleStartSignUp = Action { implicit request =>

    if (registrationEnabled) {
      var checkOtp = false
      var token = ""
      startForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.auth.Registration.startSignUp(errors))
        },
        form => {
          // there seems to be no good way to get the language, so we do it manually
          val lang = request.cookies.get(messagesApi.langCookieName).map(cookie => Lang.get(cookie.value).getOrElse(Lang.defaultLang)).getOrElse(Lang.defaultLang)
          Logger.info("=====value in otp=====" + form.otp)
          val res = globals.userModel.trustedActionStart(form.email, isSignup = true, lang.code, form.first_name, form.last_name, form.mobile_number, form.password
          )
          val url = "http://" + request.host;
          Logger.info("=====URL is ====" + url)
          trynexUserService.userExists(form.email, form.mobile_number) match {
            case true => {
              Logger.info("========user already exists======")
              // user signed up already, send an email offering to login/recover password

              Mailer.sendAlreadyRegisteredEmail(url + "/reset", form.email, globals.userModel.userPgpByEmail(form.email))
            }
            case false => {
              Logger.info("========new user======")

              //check for otp
              checkOtp = trynexUserService.userSmsOtpCheck((form.mobile_number).toLong, (form.otp).toInt)
              Logger.info("============otp check=====" + checkOtp)

            }
          }
          if (checkOtp == false) {
            Logger.info(">>>>>>>>>>>>>res is  false" + checkOtp)
            /*val to = controllers.routes.Registration.startSignUp()
              Logger.info("to value====" + to)
              */ // Redirect(to).flashing(Error -> Messages(InvalidOtp))
            //badRequest(startForm, form, request, Some("invalidOTP"));
            // BadRequest(views.html.auth.Registration.startSignUp(startForm.fill(form))).flashing("error" -> "invalid")
            Results.BadRequest(views.html.auth.Registration.startSignUp(startForm.fill(form), Some("invalidOTP")));
          } else {
            token = trynexUserService.createToken(form.email, isSignUp = true, lang.code, form.first_name, form.last_name, form.mobile_number, form.password)

            Mailer.sendSignUpEmail(url + "/signup", form.email, token)

            Logger.info("============call handleSignUp===========" + token)

            Redirect(onHandleStartSignUpGoTo).flashing(Success -> Messages(ThankYouCheckEmail), Email -> form.email)
          }

        }
      )
    } else NotFound

  }

  /**
   * Renders the sign up page
   * @return
   */
  def signUp(token: String) = Action { implicit request =>
    if (registrationEnabled) {
      if (Logger.isDebugEnabled) {
        Logger.debug("[securesocial] trying sign up with token %s".format(token))
      }
      //userTokenDetails.get
      //get user details from token table
      // Logger.info("==============userTokenDetails======" + userTokenDetails.get.mobile_number + "==" + userTokenDetails.get.first_name)

      //val userTokenDetailss = trynexUserService.findToken(token)
      //Logger.info("==============userTokenDetails======" + userTokenDetailss.get.mobile_number + "==" + userTokenDetailss.get.first_name)

      // trynexUserService.deleteToken(t.uuid)
      executeForToken(token, true, { _ =>
        val userTokenDetails = trynexUserService.findToken(token)
        val user = trynexUserService.create(SocialUser(
          -1, // this is a placeholder
          userTokenDetails.get.email,
          userTokenDetails.get.first_name,
          userTokenDetails.get.last_name,
          userTokenDetails.get.mobile_number,
          0,
          "enableRefererAsOriginalUrl"
        ), userTokenDetails.get.password, token, "")
        Logger.info("======before execute======")
        Ok(views.html.auth.login(UsernamePasswordProvider.loginForm))
      })
    } else NotFound
  }

  private def executeForToken(token: String, isSignUp: Boolean, f: Token => Result): Result = {
    trynexUserService.findToken(token) match {
      case Some(t) if !t.isExpired && t.isSignUp == isSignUp => {
        f(t)
      }
      case _ => {
        val to = if (isSignUp) controllers.routes.Registration.startSignUp() else controllers.routes.Registration.startResetPassword()
        Redirect(to).flashing(Error -> Messages(InvalidLink))
      }
    }
  }

  // XXX: copied from ProviderController TODO: fix duplication
  def completePasswordAuth[A](id: Long, email: String)(implicit request: play.api.mvc.Request[A]) = {
    import controllers.ProviderController._
    val authenticator = Authenticator.create(Some(id), None, email)
    Redirect(toUrl(request2session)).withSession(request2session - SecureSocial.OriginalUrlKey).withCookies(authenticator.toCookie)
  }

  /**
   * Handles posts from the sign up page
   */
  def handleSignUp(token: String) = Action {
    implicit request =>
      Logger.info("==============registrationEnabled=======" + registrationEnabled)

      if (registrationEnabled) {
        executeForToken(token, true, { t =>
          form.bindFromRequest.fold(
            errors => {
              if (Logger.isDebugEnabled) {
                Logger.debug("[securesocial] errors " + errors)
              }
              BadRequest(views.html.auth.Registration.signUp(errors, t.uuid))
            },
            info => {
              Logger.info("========inside info part-===================")
              val user = trynexUserService.create(SocialUser(
                -1, // this is a placeholder
                t.email,
                t.first_name,
                t.last_name,
                t.mobile_number,
                0,
                "enableRefererAsOriginalUrl"
              ), info.password, token, info.pgp)
              //trynexUserService.deleteToken(t.uuid)
              if (UsernamePasswordProvider.sendWelcomeEmail) {
                Mailer.sendWelcomeEmail(user)
              }
              globals.logModel.logEvent(LogEvent.fromRequest(Some(user.id), Some(user.email), request, LogType.SignupSuccess))
              if (UsernamePasswordProvider.signupSkipLogin) {
                completePasswordAuth(user.id, user.email)
              } else {
                Redirect(onHandleSignUpGoTo).flashing(Success -> Messages(SignUpDone)).withSession(request2session)
              }
            }
          )
        })
      } else NotFound
  }

  def startResetPassword = Action { implicit request =>
    Ok(views.html.auth.Registration.startResetPassword(resetEmailForm))
  }

  def handleStartResetPassword = Action { implicit request =>
    resetEmailForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.auth.Registration.startResetPassword(errors))
      },
      email => {
        Logger.info("-======check for user info=====" + email.email)
        trynexUserService.userExistsEmail(email.email) match {
          case true => {
            Logger.info("=============user exists=================")
            //send mail to user for password reset

            val token = trynexUserService.createToken(email.email, isSignUp = true, "", "", "", "", "")
            val url = "http://" + request.host;
            Mailer.sendPasswordResetEmail(email.email, url, token, globals.userModel.userPgpByEmail(email.toString()))

            //globals.userModel.trustedActionStart(Email, isSignup = false, "", email.first_name, email.last_name, email.mobile_number, "")
          }
          case false => {
            // The user wasn't registered. Oh, well.
          }
        }
        Redirect(onHandleStartResetPasswordGoTo).flashing(Success -> Messages(ThankYouCheckEmail))
      }
    )
  }

  def resetPassword(token: String) = Action { implicit request =>
    executeForToken(token, true, { t =>
      Ok(views.html.auth.Registration.resetPasswordPage(changePasswordForm, token))
    })
  }

  def handleResetPassword(token: String) = Action { implicit request =>
    executeForToken(token, true, { t =>
      changePasswordForm.bindFromRequest.fold(errors => {
        BadRequest(views.html.auth.Registration.resetPasswordPage(errors, token))
      },
        p => {
          val toFlash = trynexUserService.userExists(t.email, t.mobile_number) match {
            case true => {
              // this should never actually fail because we checked the token already
              val res = trynexUserService.resetPass(t.email, token, p._1)
              Logger.info("=========reset pwd==========" + res)
              trynexUserService.deleteToken(token)
              Mailer.sendPasswordChangedNotice(t.email, globals.userModel.userPgpByEmail(t.email))
              Success -> Messages(PasswordUpdated)
            }
            case false => {
              Logger.error("[securesocial] could not find user with email %s during password reset".format(t.email))
              Error -> Messages(ErrorUpdatingPassword)
            }
          }
          Redirect(onHandleResetPasswordGoTo).flashing(toFlash)
        })
    })
  }
}

object Registration {

  val PasswordsDoNotMatch = "auth.signup.passwordsDoNotMatch"
  val PgpKeyInvalid = "auth.signup.pgpKeyInvalid"
  val ThankYouCheckEmail = "auth.signup.thankYouCheckEmail"
  val InvalidLink = "auth.signup.invalidLink"
  val SignUpDone = "auth.signup.signUpDone"
  val PasswordUpdated = "auth.password.passwordUpdated"
  val ErrorUpdatingPassword = "auth.password.error"
  val Name = "name"
  val Phone = "phone"
  val Category = "category"
  val Description = "description"
  val AcceptTos = "accepttos"
  val MailingList = "mailinglist"
  val Password = "password"
  val Otp = "otp"
  val Password1 = "password1"
  val Password2 = "password2"
  val Email = "email"
  val FirstName = "first_name"
  val LastName = "last_name"
  val MobileNumber = "mobile_number"
  val Success = "success"
  val Error = "error"
  val Pgp = "pgp"
  val Language = "language"

  val RegistrationEnabled = "securesocial.registrationEnabled"

  /** The redirect target of the handleStartSignUp action. */
  val onHandleStartSignUpGoTo = stringConfig("securesocial.onStartSignUpGoTo", controllers.routes.LoginPage.login().url)
  /** The redirect target of the handleSignUp action. */
  val onHandleSignUpGoTo = stringConfig("securesocial.onSignUpGoTo", controllers.routes.LoginPage.login().url)
  /** The redirect target of the handleStartResetPassword action. */
  val onHandleStartResetPasswordGoTo = stringConfig("securesocial.onStartResetPasswordGoTo", controllers.routes.LoginPage.login().url)
  /** The redirect target of the handleResetPassword action. */
  val onHandleResetPasswordGoTo = stringConfig("securesocial.onResetPasswordGoTo", controllers.routes.LoginPage.login().url)

  private def stringConfig(key: String, default: => String) = {
    Play.current.configuration.getString(key).getOrElse(default)
  }

  case class RegistrationInfo(mailingList: Boolean, password: String, pgp: String)
  //case class StartRegistrationInfo(email: String, first_name: String, last_name: String, mobile_number: String, password: String)
  case class StartRegistrationInfo(email: String, first_name: String, last_name: String, mobile_number: String, password: String, otp: String)

  case class PasswordResetInfo(email: String)
  case class SupportTicketInfo(name: String, email: String, phone: String, category: String, description: String)

}