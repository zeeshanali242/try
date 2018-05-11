package controllers

import javax.inject.Inject

import play.api.mvc.Action
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.i18n.{ I18nSupport, Messages }
import play.api.i18n.MessagesApi
import play.Logger
import service.EtherTransactionService
import org.web3j.utils.Convert;
import java.math.BigInteger
import java.math.BigDecimal
import securesocial.core.providers.utils.Mailer

class WithdrawalConfirmation @Inject() (val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {
  //method call on click of confirmation link in email
  def confirm(idStr: String, token: String) = SecuredAction(ajaxCall = true)(parse.anyContent) { implicit request =>
    val id = idStr.toLong
    //get user address by user id
    val toAddress = "0x" + (globals.engineModel.to_addresses_eth(id)).apply(0)
    val currency = (globals.engineModel.get_withdrawals_currency(id)).apply(0)
    /*  val fromUserDetails = glaobals.engineModel.addresses_eth(request.user.id, "ETH")

    val fromUserAddress = "0x" + (fromUserDetails.apply(0)._1)
    val fromUserPPK = (fromUserDetails.apply(0)._2).trim()*/
    val amount = (globals.engineModel.amount_eth(id)).apply(0)
    val user = globals.userModel.findUserById(request.user.id).get
    Logger.info("----fromuser---" + currency + "---fromUserPPK---" + user.email + "----" + "----toAddress---" + toAddress + "-----amount----" + amount)

    if (globals.engineModel.confirmWithdrawal(id, token)) {
      //call service method to perform raw transaction
      // val wei = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
      // new EtherTransactionService().ethRawTransaction(fromUserAddress, fromUserPPK, toAddress, wei)
      Mailer.sendWithdrawalAdminEmail("adithihdgowda@gmail.com", amount.toString(), currency, user.email, toAddress, user.first_name, user.last_name);
      Redirect(controllers.routes.Application.index()).flashing("success" -> Messages("withdrawal.confirm.success"))
    } else {
      Redirect(controllers.routes.Application.index()).flashing("error" -> Messages("withdrawal.confirm.fail"))
    }
  }

  /*//method call on click of reject link in email
  def reject(idStr: String, token: String) = Action { implicit request =>
    val id = idStr.toLong
    if (globals.engineModel.rejectWithdrawal(id, token)) {
      Redirect(controllers.routes.Application.index()).flashing("success" -> Messages("withdrawal.reject.success"))
    } else {
      Redirect(controllers.routes.Application.index()).flashing("error" -> Messages("withdrawal.reject.fail"))
    }
  }*/
}
