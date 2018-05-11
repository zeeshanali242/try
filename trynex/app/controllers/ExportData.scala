
package controllers

import _root_.java.util.UUID
import javax.inject.Inject
import play.api.mvc.{ Result, Action, Controller }
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
import java.math.BigInteger
import java.sql.Date
import java.sql.Timestamp
import org.apache.commons.mail.EmailAttachment
import java.io.File
import play.api.libs.json._
/**
 * A controller to handle user registration.
 *
 */
class ExportData @Inject() (val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {
  import controllers.ExportData._
  import PasswordChange._

  val check: String = "or";

  def tradeReport = SecuredAction(parse.json) { implicit request =>

    val body = request.body
    Logger.info("=================tradeReportData===" + body)

    try {
      val body = request.body
      (for (
        startDate <- (body \ "startDate").validate[String];
        endDate <- (body \ "endDate").validate[String];
        selectedCoin <- (body \ "selectedCoin").validate[String];
        selectedTransaction <- (body \ "selectedTransaction").validate[String]

      ) yield {
        Logger.info("=====startDate===" + startDate)
        Logger.info("======enddate===" + endDate)
        Logger.info("====selectedCoin===" + selectedCoin)
        var uid = request.user.id
        var uemailId = request.user.email
        Logger.info("----------User Id in EXPORT------------" + uid)
        Logger.info("----------User Detail in EXPORT------------" + uemailId)
        var userID: String = uid.toString()

        var squery: String = " select th.id, th.amount, th.created, th.price, th.base, th.counter, th.is_bid, th.fee from (( select m.id, m.amount, m.created, m.price, m.base, m.counter, true as is_bid, m.bid_fee as fee from matches m where m.bid_user_id = "
        var squerySell: String = " select th.id, th.amount, th.created, th.price, th.base, th.counter, th.is_bid, th.fee from (( select m.id, m.amount, m.created, m.price, m.base, m.counter, false as is_bid, m.ask_fee as fee from matches m where m.ask_user_id = "

        var st1: String = " and m.created between '"
        var st2: String = "' and '"
        var st3: String = "' order by m.created desc, m.id desc )union all (select m.id, m.amount, m.created, m.price, m.base, m.counter, false as is_bid, m.ask_fee as fee from matches m where m.ask_user_id = "
        var st4: String = "' order by m.created desc, m.id desc ) ) as th where th.base = '"
        var st5: String = selectedCoin
        var st6: String = "' order by created desc, id desc"

        val finalBuyString: String = squery.concat(userID).concat(st1).concat(startDate).concat(st2).concat(endDate).concat(st4).concat(st5).concat(st6)

        val finalSellString: String = squerySell.concat(userID).concat(st1).concat(startDate).concat(st2).concat(endDate).concat(st4).concat(st5).concat(st6)
        val finalBothString: String = squery.concat(userID).concat(st1).concat(startDate).concat(st2).concat(endDate).concat(st3).concat(userID).concat(st1).concat(startDate).concat(st2).concat(endDate).concat(st4).concat(st5).concat(st6)
        val tradeType = selectedTransaction
        var finalString: String = ""
        if (tradeType == "Buy") {
          finalString = finalBuyString
        } else if (tradeType == "Sell") {
          finalString = finalSellString
        } else {
          finalString = finalBothString
        }

        Logger.info(" SQL String for Order" + finalString + ">>>>>>>---------")

        globals.userModel.exportDataOrderTransaction(finalString)
        val attachment: EmailAttachment = new EmailAttachment()
        attachment.setPath("/tmp/OrderData.csv")
        attachment.setDisposition(EmailAttachment.ATTACHMENT)
        attachment.setDescription("Data Details")
        attachment.setName("Order Transaction.csv")
        Mailer.sendExportData(uemailId, attachment, "Order")

        Ok("")
      }).getOrElse(

        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
      )

    } catch {

      case e: Throwable =>
        e.printStackTrace()
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoplacebid")))
    }

  }

  def walletReport = SecuredAction(parse.json) { implicit request =>

    val body = request.body
    Logger.info("=================walletReportData===" + body)
    try {
      val body = request.body
      (for (
        startDate <- (body \ "startDate").validate[String];
        endDate <- (body \ "endDate").validate[String];
        selectedTransaction <- (body \ "selectedTransaction").validate[String];
        selectedCoinWallet <- (body \ "selectedCoinWallet").validate[String]
      ) yield {
        var uid = request.user.id
        var uemailId = request.user.email
        Logger.info("----------User Id in EXPORT------------" + uid)
        Logger.info("----------User Detail in EXPORT------------" + uemailId)
        var userID: String = uid.toString()
        Logger.info("=userID=====" + userID)
        var squery: String = "select * from ( ( "
        var st1: String = "select w.created, w.currency, 'Withdraw' as type, w.amount, w.fee from withdrawals w left join withdrawals_crypto wc on w.id = wc.id where w.user_id = "
        var st2: String = " and (wc.withdrawals_crypto_tx_id is not null or wc.id is null or w.user_rejected = true) and w.created between '"
        var st3: String = "' and '"
        var st4: String = " order by w.created desc, w.id desc "
        var st5: String = ") union all ("
        var st6: String = " select d.created, d.currency, 'Deposit' as type, d.amount, d.fee from deposits d left join deposits_crypto dc on d.id = dc.id where d.user_id = "
        var st7: String = " and (dc.confirmed is not null or dc.id is null) and d.created between '"
        var st8: String = "  order by d.created desc, d.id desc "
        var st9: String = selectedCoinWallet
        var st10: String = "' order by created desc"
        var st11: String = "' and w.currency = '"
        var st12: String = "' and d.currency = '"
        var st13: String = "'"
        var st14: String = ") ) as a where a.currency = '"
        val walletType = selectedTransaction
        val finalBothString: String = squery.concat(st1).concat(userID).concat(st2).concat(startDate).concat(st3).concat(endDate).concat("' ").concat(st4).concat(st5).concat(st6).concat(userID).concat(st7).concat(startDate).concat(st3).concat(endDate) concat ("' ").concat(st8).concat(st14).concat(st9).concat(st10)
        val finalWithdrawString: String = st1.concat(userID).concat(st2).concat(startDate).concat(st3).concat(endDate).concat(st11).concat(st9).concat(st13).concat(st4)
        val finalDepositString: String = st6.concat(userID).concat(st7).concat(startDate).concat(st3).concat(endDate).concat(st12).concat(st9).concat(st13).concat(st8)
        var finalString: String = ""
        if (walletType == "Withdraw") {
          finalString = finalWithdrawString

        } else if (walletType == "Deposit") {
          finalString = finalDepositString
        } else {
          finalString = finalBothString
        }

        Logger.info(" SQL String for Wallet " + finalString + ">>>>>>>---------")

        globals.userModel.exportDataWalletTransaction(finalString)
        val attachment: EmailAttachment = new EmailAttachment()
        attachment.setPath("/tmp/WalletData.csv")
        attachment.setDisposition(EmailAttachment.ATTACHMENT)
        attachment.setDescription("Data Details")
        attachment.setName("Wallet Transaction.csv")
        Mailer.sendExportData(uemailId, attachment, "Wallet")

        Ok("")
      }).getOrElse(

        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoparseinput")))
      )

    } catch {

      case _: Throwable =>
        BadRequest(Json.obj("message" -> Messages("messages.api.error.failedtoplacebid")))
    }

  }

}

object ExportData {

  val ExportDataMessage = "auth.signup.exportData"
  val ThankYouCheckEmail = "auth.signup.thankYouCheckEmail"

  val startDate = "startDate"
  val endDate = "endDate"
  val typeExport = "typeExport"
  val Success = "success"
  val Error = "error"
  val Pgp = "pgp"
  val Language = "language"

  val onHandleExportGoTo = stringConfig("securesocial.onExportToGO", controllers.routes.Application.history().url)

  private def stringConfig(key: String, default: => String) = {
    Play.current.configuration.getString(key).getOrElse(default)
  }

  case class ExportDataInfo(startDate: DateTime, endDate: DateTime, typeExport: String)
}
