
package controllers.API

import javax.inject.Inject

import play.api._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.i18n.{ Lang, MessagesApi, I18nSupport, Messages }
import java.sql.Timestamp
import service.CryptoPanicService
class APIv1 @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /* Public */

  def recentTrades(base: String, counter: String) = Action { implicit request =>
    // a specific pair will be given as an argument
    if (globals.metaModel.activeMarkets.contains(base, counter)) {
      Ok(globals.engineModel.recentTrades(base, counter))
    } else {
      BadRequest(Json.obj("message" -> Messages("messages.api.error.invalidpair")))
    }
  }

  def volume() = Action { implicit request =>
    Ok(globals.engineModel.volume())
  }

  def pairsData() = Action { implicit request =>
    val list = globals.engineModel.pairsCompleteData();
    Ok(Json.toJson(list));
  }

  def newsFromCryptopanic() = Action { implicit request =>
    val news = new CryptoPanicService().getNewsOfCurrency();
    Ok(Json.obj("news" -> news));
  }

  def getUpdatedMarketData(from: String, to: String) = Action { implicit request =>
    var data = globals.engineModel.getUpdatedMarkets(new Timestamp(from.toLong), new Timestamp(to.toLong));
    Ok(Json.obj("update" -> data));
  }

  def getUpdatedOrderData(from: String, to: String) = Action { implicit request =>
    var data = globals.engineModel.getUpdatedOrders(new Timestamp(from.toLong), new Timestamp(to.toLong));
    Logger.info("data of orders is " + data);
    Ok(Json.obj("update" -> data));
  }

  def get_last_24_price(base: String, counter: String) = Action { implicit request =>

    Ok(Json.toJson(globals.engineModel.get_last_24_price(base, counter)))
  }

  def openTrades(base: String, counter: String) = Action { implicit request =>
    // a specific pair will be given as an argument
    if (globals.metaModel.activeMarkets.contains(base, counter)) {
      Ok(globals.engineModel.ordersDepth(base, counter))
    } else {
      BadRequest(Json.obj("message" -> Messages("messages.api.error.invalidpair")))
    }
  }

  /* Authenticated */

  def balance = Action(parse.json) { implicit request =>
    val body = request.body
    (for (
      apiKey <- (body \ "api_key").validate[String]
    ) yield {
      val balances = globals.engineModel.balance(None, Some(apiKey))
      Ok(Json.toJson(balances.map({ c =>
        Json.obj(
          "currency" -> c._1,
          "amount" -> c._2._1.bigDecimal.toPlainString,
          "hold" -> c._2._2.bigDecimal.toPlainString
        )
      })
      ))
    }).getOrElse(
      BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
    )
  }

  def ask = Action(parse.json) { implicit request =>
    try {
      val body = request.body
      (for (
        apiKey <- (body \ "api_key").validate[String];
        base <- (body \ "base").validate[String];
        counter <- (body \ "counter").validate[String];
        amount <- (body \ "amount").validate[BigDecimal];
        price <- (body \ "price").validate[BigDecimal]
      ) yield {
        if (price > 0 && amount > 0) {
          globals.metaModel.activeMarkets.get(base, counter) match {
            case Some((active, minAmount)) if active && amount >= minAmount =>
              val res = globals.engineModel.askBid(None, Some(apiKey), base, counter, amount, price, isBid = false)
              if (res.isDefined) {
                Ok(Json.toJson(res.get))
              } else {
                BadRequest(Json.obj("message" -> Messages("messages.api.error.nonsufficientfunds")))
              }
            case Some((active, minAmount)) if active =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.amountmustbeatleast").format(minAmount)))
            case Some((active, minAmount)) =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.tradingsuspendedon").format(base, counter)))
            case _ =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.invalidpair")))
          }
        } else {
          BadRequest(Json.obj("message" -> Messages("messages.api.error.thepriceandamountmustbepositive")))
        }
      }).getOrElse(
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
      )
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoplaceask")))
    }
  }

  def bid = Action(parse.json) { implicit request =>
    try {
      val body = request.body
      (for (
        apiKey <- (body \ "api_key").validate[String];
        base <- (body \ "base").validate[String];
        counter <- (body \ "counter").validate[String];
        amount <- (body \ "amount").validate[BigDecimal];
        price <- (body \ "price").validate[BigDecimal]
      ) yield {
        if (price > 0 && amount > 0) {
          globals.metaModel.activeMarkets.get(base, counter) match {
            case Some((active, minAmount)) if active && amount >= minAmount =>
              val res = globals.engineModel.askBid(None, Some(apiKey), base, counter, amount, price, isBid = true)
              if (res.isDefined) {
                Ok(Json.toJson(res.get))
              } else {
                BadRequest(Json.obj("message" -> Messages("messages.api.error.nonsufficientfunds")))
              }
            case Some((active, minAmount)) if active =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.amountmustbeatleast").format(minAmount)))
            case Some((active, minAmount)) =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.tradingsuspendedon").format(base, counter)))
            case _ =>
              BadRequest(Json.obj("message" -> Messages("messages.api.error.invalidpair")))
          }
        } else {
          BadRequest(Json.obj("message" -> Messages("messages.api.error.thepriceandamountmustbepositive")))
        }
      }).getOrElse(
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
      )
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoplacebid")))
    }
  }

  def cancel = Action(parse.json) { implicit request =>
    val body = request.body
    (for (
      apiKey <- (body \ "api_key").validate[String];
      order <- (body \ "order").validate[Long]
    ) yield {
      val res = globals.engineModel.cancel(None, Some(apiKey), order)
      if (res) {
        Ok(Json.obj())
      } else {
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtocancelorder")))
      }
    }).getOrElse(
      BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
    )
  }

  def tradeHistory = Action(parse.json) { implicit request =>
    val body = request.body
    (for (
      apiKey <- (body \ "api_key").validate[String];
      before = (body \ "before").asOpt[DateTime];
      limit = (body \ "limit").asOpt[Int];
      lastId = (body \ "last_id").asOpt[Long]
    ) yield {
      Ok(Json.toJson(globals.userModel.tradeHistory(None, Some(apiKey), before, limit, lastId)))
    }).getOrElse(
      BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
    )
  }

  def pendingTrades = Action(parse.json) { implicit request =>
    val body = request.body
    (for (
      apiKey <- (body \ "api_key").validate[String];
      base <- (body \ "base").validate[String];
      counter <- (body \ "counter").validate[String]
    ) yield {
      val orders = globals.engineModel.userPendingTrades(None, Some(apiKey), base, counter)
      Ok(Json.toJson(orders))
    }).getOrElse(
      BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
    )
  }

}
