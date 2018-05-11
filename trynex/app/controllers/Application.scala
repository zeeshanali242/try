package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.i18n.{ I18nSupport, Lang }
import play.api.Play.current
import play.api.i18n.MessagesApi
import play.i18n.Langs
import scala.language.postfixOps
import play.api.Logger
import jsmessages.JsMessagesFactory
import securesocial.core.{ SecuredRequest, _ }
import controllers.KycUserController.UserKycAuthController
import controllers.KycUserController.GetUserKycController
import securesocial.core._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.libs.EventSource
import play.api.libs.json.JsValue
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws._

class Application @Inject() (jsMessagesFactory: JsMessagesFactory, val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {

  /*val chatChannel = Concurrent.braodcast(String)
  val chatOut = Concurrent.braodcast(String)*/

  val (out, channel) = Concurrent.broadcast[String]
  def index = UserAwareAction { implicit request =>
    val to = ProviderController.landingUrl
    if (SecureSocial.currentUser.isDefined) {
      Redirect(to)
    } else {
      Ok(views.html.content.index(request.user))
    }

  }

  /* def chatFeed = Action { request =>
    Logger.info("Hey connnect");
    Ok.chunked(chatOut
      &> EventSource()).as("text/eventstream")
  }

  def postMessage = Action(parse.json) { request =>
    chatChannel.push(request.body);
    Ok

  }*/

  /*def dashboard = SecuredAction { implicit request =>
    Ok(views.html.content.delete(route.Application.chatFeed(), route.Application.postMessage()))
  }*/
  def connect = WebSocket.using[String] { request =>

    val in = Iteratee.foreach[String] { msg =>
      Logger.info("Mesg rec " + msg)
      channel.push(msg)
    }.map { _ => println("closed") } //when connection close

    (in, out)

  }
  def homePage = UserAwareAction { implicit request =>
    val to = ProviderController.landingUrl
    if (SecureSocial.currentUser.isDefined) {
      Redirect(to)
    } else {
      Ok(views.html.content.homepage(request.user))
    }

  }

  def dashboard = SecuredAction { implicit request =>
    Ok(views.html.exchange.dashboard(request.user))
  }

  def account = SecuredAction { implicit request =>
    Ok(views.html.exchange.account(request.user))
  }
  def depositwithdraw = SecuredAction { implicit request =>
    Ok(views.html.exchange.depositwithdraw(request.user))
  }

  def exchange = SecuredAction { implicit request =>
    Ok(views.html.exchange.trade(request.user))
  }

  def history = SecuredAction { implicit request =>
    Ok(views.html.exchange.history(request.user))
  }

  def wallet = SecuredAction { implicit request =>
    Ok(views.html.exchange.wallet(request.user))
  }

  def viewProfile = SecuredAction { implicit request =>
    Ok(views.html.content.thankyou(request.user))
  }

  def aboutus = UserAwareAction { implicit request =>
    Ok(views.html.content.aboutus(request.user))
  }

  def terms = UserAwareAction { implicit request =>
    Ok(views.html.content.terms(request.user))
  }

  def trynexFees = UserAwareAction { implicit request =>
    Ok(views.html.content.trynexFees(request.user))
  }

  def tradingRules = UserAwareAction { implicit request =>
    Ok(views.html.content.tradingRules(request.user))
  }

  def privacyPolicy = UserAwareAction { implicit request =>
    Ok(views.html.content.privacy_policy(request.user))
  }

  def security = UserAwareAction { implicit request =>
    Ok(views.html.content.security(request.user))
  }

  def disclaimer = UserAwareAction { implicit request =>
    Ok(views.html.content.disclaimer(request.user))
  }

  def chlang(lang: String) = UserAwareAction { implicit request =>
    if (request.user.isDefined) {
      globals.userModel.changeLanguage(request.user.get.id, lang)
    }
    Redirect(request.headers.get("referer").getOrElse("/")).withLang(Lang.get(lang).getOrElse(Lang.defaultLang))
  }

  val messages = jsMessagesFactory.all

  val jsMessages = Action { implicit request =>
    Ok(messages(Some("window.Messages")))
  }

  def kycProfile = SecuredAction { implicit request =>
    /*Logger.info("=====requeseted user for kyc  ===" + request.user.id)
    new GetUserKycController().getKycUserResponse(request.user.id)*/
    Ok(views.html.exchange.viewProfile(request.user))
  }

  def getSignzyRedirectUrl = SecuredAction { implicit request =>
    var status: Boolean = false;
    /*      Logger.info("=====requeseted user===" + request.user.get.id + "====" + request.user.get.email + "=====" + request.user.get.first_name + "=========" + request.user.get.email + "=====" + request.user.get.mobile_number)
*/ var url = new UserKycAuthController().getAccessToken(request.user.id, request.user.first_name, request.user.mobile_number, request.user.email)
    Logger.info("===url is====" + url)
    if (url.startsWith("https")) {
      status = true;
    }
    if (status) {
      Redirect(url, 302)
    } else {
      BadRequest(views.html.exchange.viewProfile(request.user, Some(url)));
    }
  }

}
