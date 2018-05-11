
package controllers

import javax.inject.Inject

import models.{ LogEvent, LogType }
import play.Logger
import play.api.Play
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.mvc.{ Action, Controller }
import play.api.i18n.MessagesApi
import securesocial.core._
import service.trynexUserService

/**
 * The Login page controller
 */
class LoginPage @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {
  /**
   * The property that specifies the page the user is redirected to after logging out.
   */
  val onLogoutGoTo = "securesocial.onLogoutGoTo"

  /**
   * Renders the login page
   * @return
   */
  def login = Action { implicit request =>
    val to = ProviderController.landingUrl
    if (SecureSocial.currentUser.isDefined) {
      // if the user is already logged in just redirect to the app
      if (Logger.isDebugEnabled()) {
        Logger.debug("User already logged in, skipping login page. Redirecting to %s".format(to))
      }
      Redirect(to)
    } else {
      Logger.info("Loggin");
      if (SecureSocial.enableRefererAsOriginalUrl) {
        SecureSocial.withRefererAsOriginalUrl(Ok(views.html.auth.login(UsernamePasswordProvider.loginForm)))
      } else {
        Ok(views.html.auth.login(UsernamePasswordProvider.loginForm))

      }
    }
  }

  def tfaTOTP = Action { implicit request =>
    Ok(views.html.auth.TFAGoogle(ProviderController.tfaForm))
  }

  def smsOTP = Action { implicit request =>
    Ok(views.html.auth.smsotp(ProviderController.smsOtpForm))
  }

  /**
   * Logs out the user by clearing the credentials from the session.
   * The browser is redirected either to the login page or to the page specified in the onLogoutGoTo property.
   *
   * @return
   */
  def logout = Action { implicit request =>
    val to = Play.configuration.getString(onLogoutGoTo).getOrElse(controllers.routes.LoginPage.login().absoluteURL(UsernamePasswordProvider.sslEnabled))
    val user = for (
      authenticator <- SecureSocial.authenticatorFromRequest;
      user <- trynexUserService.find(authenticator.uid.get)
    ) yield {
      Authenticator.delete(authenticator.id)
      globals.logModel.logEvent(LogEvent.fromRequest(Some(user.id), Some(user.email), request, LogType.Logout))
      user
    }
    val result = Redirect(to).discardingCookies(Authenticator.discardingCookie)
    user match {
      case Some(u) => result.withSession(request2session)
      case None => result
    }
  }
}
