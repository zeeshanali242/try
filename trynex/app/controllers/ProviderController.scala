
package controllers

import javax.inject.Inject

import models.{ LogEvent, LogType }
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import org.joda.time.DateTime
import securesocial.core.providers.utils.Mailer
import play.api.i18n.{ Lang, I18nSupport, Messages, MessagesApi }
import play.api.mvc.{ Result, _ }
import play.api.{ Logger, Play }
import securesocial.core.{ AccessDeniedException, SocialUser, _ }
import service.{ TOTPAuthenticator, TOTPSecret }
import service.trynexUserService
import securesocial.core.providers.utils.SendSms;
import securesocial.core.SocialUser
/**
 * A controller to provide the authentication entry point
 */
class ProviderController @Inject() (val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {
  import controllers.ProviderController._

  /**
   * Renders a not authorized page if the Authorization object passed to the action does not allow
   * execution.
   *
   * @see Authorization
   */
  def notAuthorized() = Action { implicit request =>
    Forbidden(views.html.auth.notAuthorized())
  }

  private def badRequestTOTP[A](f: Form[String], request: Request[A], msg: Option[String] = None): Result = {
    implicit val r = request
    BadRequest(views.html.auth.TFAGoogle(f, msg))
  }

  private def badRequestSMS[A](f: Form[String], request: Request[A], msg: Option[String] = None): Result = {
    implicit val r = request
    BadRequest(views.html.auth.smsotp(f, msg))
  }

  //TODO: turn into ajax call
  def tfaPost() = Action { implicit request =>
    val form = tfaForm.bindFromRequest()(request)
    form.fold(
      errors => {
        Results.BadRequest("Unknown form error.") //TODO: take the user to an actual error page
      },
      tfaToken => {
        val authenticator = SecureSocial.authenticatorFromRequest(request)
        var userOs: String = "";
        var userBrowser: String = "";
        var userInfo = models.LogModel.UserSysDetFromRequest(request)

        userOs = userInfo.os
        userBrowser = userInfo.browser
        if (authenticator.isDefined) {
          if (globals.userModel.userHasTotp(authenticator.get.email)) {
            val user = globals.userModel.totpLoginStep2(authenticator.get.email, authenticator.get.totpSecret.get, tfaToken, models.LogModel.headersFromRequest(request), models.LogModel.ipFromRequest(request), userOs, userBrowser)

            if (user.isDefined) {
              ProviderController.firstname = user.get.first_name
              ProviderController.lastname = user.get.last_name
              Authenticator.save(authenticator.get.complete2fa(user.get.id))
              var userIp = models.LogModel.ipFromRequest(request)
              val email = authenticator.get.email
              val currentTime = DateTime.now()
              val format = "yyyy-MM-dd mm:ss"
              var userLoginTime: String = currentTime.toString(format)
              // Logger.info("================= email in provider controller================== " + email)
              Mailer.sendUserInfoEmail(user.get.first_name, email, userLoginTime.toString(), userIp, userOs, userBrowser)
              Redirect(toUrl(request2session)).withSession(request2session - SecureSocial.OriginalUrlKey)
            } else {
              // form error
              badRequestTOTP(tfaForm, request, Some("Invalid token."))
            }
          } else {
            Results.BadRequest("Two factor auth not configured.")
          }
        } else {
          Results.BadRequest("Please log in first.")
        }
      }
    )
  }

  def smsPost() = Action { implicit request =>
    val form = smsOtpForm.bindFromRequest()(request)

    form.fold(
      errors => {
        Results.BadRequest("Unknown form error.") //TODO: take the user to an actual error page
      },
      smsToken => {
        val authenticator = SecureSocial.authenticatorFromRequest(request)
        var userOs: String = "";
        var userBrowser: String = "";
        var userInfo = models.LogModel.UserSysDetFromRequest(request)
        userOs = userInfo.os
        userBrowser = userInfo.browser
        if (authenticator.isDefined) {
          if (!globals.userModel.userHasTotp(authenticator.get.email)) {
            val user = globals.userModel.totpLoginSMS(authenticator.get.email, smsToken.toInt, models.LogModel.headersFromRequest(request), models.LogModel.ipFromRequest(request), userOs, userBrowser)
            Logger.info("------------------------user--------" + user)
            if (user.isDefined) {
              Authenticator.save(authenticator.get.complete2fa(user.get.id))
              var userIp = models.LogModel.ipFromRequest(request)
              val email = authenticator.get.email
              val currentTime = DateTime.now()
              val format = "yyyy-MM-dd mm:ss"
              var userLoginTime: String = currentTime.toString(format)
              /*Logger.info("email of user is L:115 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + user.get.email)
              Logger.info("================= email in provider controller L:116================== " + email)*/

              Mailer.sendUserInfoEmail(user.get.first_name, user.get.email, userLoginTime.toString(), userIp, userOs, userBrowser)
              Redirect(toUrl(request2session)).withSession(request2session - SecureSocial.OriginalUrlKey)
            } else {
              // form error

              badRequestSMS(smsOtpForm, request, Some("Please enter correct OTP"))
            }
          } else {
            Results.BadRequest("Two factor auth is configured")
          }
        } else {
          Results.BadRequest("Please log in first.")
        }
      }
    )
  }

  private def badRequest[A](f: Form[(String, String)], request: Request[A], msg: Option[String] = None): Result = {
    implicit val r = request
    Results.BadRequest(views.html.auth.login(f, msg))
  }

  def loginPost() = Action { implicit request =>
    try {
      val form = UsernamePasswordProvider.loginForm.bindFromRequest()
      form.fold(
        errors => badRequest(errors, request),
        credentials => {
          val email = credentials._1.trim
          var user: Option[SocialUser] = None
          var totp_hash: Option[String] = None
          var userOs: String = "";
          var userBrowser: String = "";
          var userInfo = models.LogModel.UserSysDetFromRequest(request)
          userOs = userInfo.os
          userBrowser = userInfo.browser
          // check for 2FA
          if (globals.userModel.userHasTotp(email)) {
            totp_hash = globals.userModel.totpLoginStep1(email, credentials._2, models.LogModel.headersFromRequest(request), models.LogModel.ipFromRequest(request), userOs, userBrowser)
          } else {
            user = globals.userModel.findUserByEmailAndPassword(email, credentials._2, models.LogModel.headersFromRequest(request), models.LogModel.ipFromRequest(request), userOs, userBrowser)
          }
          Logger.info(">>>>>>>>>>>. username is " + email + ">>>>>> password is " + credentials._2);
          Logger.info(">>>>>>>>>>>>>>>> User is >>>>>>>>>>>>>>>>>>>>> " + user);

          if (totp_hash.isDefined) {
            // create session
            val authenticator = Authenticator.create(None, totp_hash, email)
            Redirect(controllers.routes.LoginPage.tfaTOTP()).withSession(request2session).withCookies(authenticator.toCookie)
          } else if (user.isDefined) {
            // create session

            val authenticator = Authenticator.create(None, None, email)

            val otp = trynexUserService.generateOTP().toInt;
            ProviderController.firstname = user.get.first_name
            ProviderController.lastname = user.get.last_name
            Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>  OTP is " + otp + " and userid is " + user.get.id + " >>>>>>>>>>>>> and mobile_number is " + user.get.mobile_number + " and username is " + ProviderController.firstname + " and session name is " + user.get.first_name);
            globals.userModel.saveOTP(user.get.id, otp);
            var msg = "You need an OTP to login. The OTP is " + otp + ". This OTP will be valid for 5 minutes.NEVER SHARE IT WITH ANYONE.";
            SendSms.sendSms(user.get.mobile_number, msg);
            Mailer.sendUserOTP(user.get.first_name, user.get.email, otp.toString());
            Redirect(controllers.routes.LoginPage.smsOTP()).withSession(request2session).withCookies(authenticator.toCookie)

          } else {
            badRequest(UsernamePasswordProvider.loginForm, request, Some(ProviderController.InvalidCredentials))
          }
        }
      )
    } catch {
      case ex: AccessDeniedException => {
        Redirect(controllers.routes.LoginPage.login()).flashing("error" -> Messages("auth.login.accessDenied"))
      }

      case other: Throwable => {
        Logger.error("Unable to log user in. An exception was thrown", other)
        Redirect(controllers.routes.LoginPage.login()).flashing("error" -> Messages("auth.login.errorLoggingIn"))
      }
    }
  }
}

object ProviderController {
  /**
   * The property that specifies the page the user is redirected to if there is no original URL saved in
   * the session.
   */
  val onLoginGoTo = "securesocial.onLoginGoTo"

  /**
   * The root path
   */
  val Root = "/"

  /**
   * The application context
   */
  val ApplicationContext = "application.context"

  val InvalidCredentials = "auth.login.invalidCredentials"

  /**
   * Returns the url that the user should be redirected to after login
   *
   * @param session
   * @return
   */
  var firstname = ""
  var lastname = ""
  def toUrl(session: Session) = session.get(SecureSocial.OriginalUrlKey).getOrElse(landingUrl)

  /**
   * The url where the user needs to be redirected after succesful authentication.
   *
   * @return
   */
  def landingUrl = Play.configuration.getString(onLoginGoTo).getOrElse(
    Play.configuration.getString(ApplicationContext).getOrElse(Root)
  )

  val tfaForm = Form(
    single("token" -> text)
  )

  val smsOtpForm = Form(
    single("otp" -> text)
  )
}
