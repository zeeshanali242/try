
package controllers

import javax.inject.Inject

import play.api.Play
import play.api.data.{ FormError, Form }
import play.api.data.Forms._
import play.api.data.validation.{ Invalid, Valid, Constraint }
import play.api.i18n.Messages
import play.api.mvc.{ AnyContent, Controller, Result }
import play.api.i18n.MessagesApi
import securesocial.core.{ SecuredRequest, _ }
import securesocial.core.providers.utils.Mailer
import service.trynexUserService
import play.api.i18n.I18nSupport

/**
 * A controller to provide password change functionality
 */
class PasswordChange @Inject() (val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {
  import PasswordChange._
  val CurrentPassword = "currentPassword"
  val InvalidPasswordMessage = "auth.passwordChange.invalidPassword"
  val Password = "password"
  val Password1 = "password1"
  val Password2 = "password2"
  val Success = "success"
  val OkMessage = "auth.passwordChange.ok"

  /**
   * The property that specifies the page the user is redirected to after changing the password.
   */
  val onPasswordChangeGoTo = "securesocial.onPasswordChangeGoTo"

  /** The redirect target of the handlePasswordChange action. */
  def onHandlePasswordChangeGoTo = Play.current.configuration.getString(onPasswordChangeGoTo).getOrElse(
    controllers.routes.PasswordChange.page().url
  )

  private def execute[A](f: (SecuredRequest[A], Form[ChangeInfo]) => Result)(implicit request: SecuredRequest[A]): Result = {
    val form = Form[ChangeInfo](
      mapping(
        CurrentPassword -> nonEmptyText.verifying(),
        Password ->
          tuple(
            Password1 -> nonEmptyText.verifying(Messages(passwordErrorStr, passwordMinLen), passwordErrorFunc _),
            Password2 -> nonEmptyText
          ).verifying(Messages(Registration.PasswordsDoNotMatch), passwords => passwords._1 == passwords._2)

      )((currentPassword, password) => ChangeInfo(currentPassword, password._1))((changeInfo: ChangeInfo) => Some("", ("", "")))
    )

    f(request, form)
  }

  def page = SecuredAction { implicit request =>
    implicit val r = request.request
    execute { (request: SecuredRequest[AnyContent], form: Form[ChangeInfo]) =>
      Ok(views.html.auth.passwordChange(form, request.user))
    }
  }

  def handlePasswordChange = SecuredAction { implicit request =>
    implicit val r = request.request
    execute { (request: SecuredRequest[AnyContent], form: Form[ChangeInfo]) =>
      form.bindFromRequest()(request).fold(
        errors => BadRequest(views.html.auth.passwordChange(errors, request.user)),
        info => {
          import scala.language.reflectiveCalls
          // This never actually fails because we already checked that the password is valid in the validators
          if (globals.userModel.userChangePass(request.user.id, info.currentPassword, info.password)) {
            Mailer.sendPasswordChangedNotice(request.user.email, globals.userModel.userPgpByEmail(request.user.email))
            Redirect(onHandlePasswordChangeGoTo).flashing(Success -> Messages(OkMessage))
          } else {
            BadRequest(views.html.auth.passwordChange(form.withError("currentPassword", Messages(InvalidPasswordMessage)), request.user))
          }
        }
      )
    }
  }
}

object PasswordChange {
  val passwordMinLen = 8

  val passwordErrorStr = "auth.signup.invalidPassword"
  def passwordErrorFunc(passwords: String) = { passwords.length >= passwordMinLen }
  case class ChangeInfo(currentPassword: String, password: String)
}
