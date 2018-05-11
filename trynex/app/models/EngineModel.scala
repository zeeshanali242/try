

package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import wallet.Wallet
import org.joda.time.DateTime
import securesocial.core.Token
import play.api.libs.json.Json
import java.sql.Timestamp
import play.Logger

class EngineModel(val db: String = "default") {

  import service.AnormParsers.rowToBigDecimalArrayArray

  // privileged apis

  def clean() = DB.withConnection(db)(implicit c =>
    SQL"""
      delete from deposits_crypto;
      delete from deposits_other;
      delete from deposits;
      delete from users_passwords;
      delete from users_tfa_secrets;
      delete from users_backup_otps;
      delete from users_addresses;
      delete from dw_fees;
      delete from trade_fees;
      delete from totp_tokens_blacklist;
      delete from withdrawals_other;
      delete from withdrawals_crypto;
      delete from withdrawals_crypto_tx_mutated;
      delete from withdrawals_crypto_tx_cold_storage;
      delete from withdrawals_crypto_tx;
      delete from withdrawals;
      delete from currencies_crypto;
      delete from wallets_crypto;
      delete from balances;
      delete from matches;
      delete from stats_30_min;
      delete from orders;
      delete from markets;
      delete from withdrawal_limits;
      delete from currencies;
      delete from event_log;
      delete from users;
      """.execute()
  )

  def testSetup() = DB.withConnection(db)(implicit c =>
    SQL"""

      select currency_insert('BTC',10);
      select currency_insert('LTC',20);
      select currency_insert('USD',30);
      select currency_insert('CAD',40);

      insert into markets(base,counter,limit_min,position) values('BTC','USD',0.01,10);
      insert into markets(base,counter,limit_min,position) values('LTC','USD',0.1,20);
      insert into markets(base,counter,limit_min,position) values('LTC','BTC',0.1,30);
      insert into markets(base,counter,limit_min,position) values('USD','CAD',1.00,40);

      insert into dw_fees(currency, method, deposit_constant, deposit_linear, withdraw_constant, withdraw_linear) values ('LTC', 'blockchain', 0.000, 0.000, 0.000, 0.000);
      insert into dw_fees(currency, method, deposit_constant, deposit_linear, withdraw_constant, withdraw_linear) values ('BTC', 'blockchain', 0.000, 0.000, 0.000, 0.000);
      insert into dw_fees(currency, method, deposit_constant, deposit_linear, withdraw_constant, withdraw_linear) values ('USD', 'wire', 0.000, 0.000, 0.000, 0.000);
      insert into dw_fees(currency, method, deposit_constant, deposit_linear, withdraw_constant, withdraw_linear) values ('CAD', 'wire', 0.000, 0.000, 0.000, 0.000);

      insert into trade_fees(linear, one_way) values (0.005, false);

      insert into withdrawal_limits(currency, limit_min, limit_max) values ('LTC', 0.001, 100);
      insert into withdrawal_limits(currency, limit_min, limit_max) values ('BTC', 0.001, 100);
      insert into withdrawal_limits(currency, limit_min, limit_max) values ('USD', 1, 10000);
      insert into withdrawal_limits(currency, limit_min, limit_max) values ('CAD', 1, 10000);

      insert into currencies_crypto(currency) values('BTC');
      insert into currencies_crypto(currency) values('LTC');

      insert into wallets_crypto(currency, last_block_read, balance_min, balance_warn, balance_target, balance_max, balance) values('LTC', 42, 0, 0, 1000, 10000, 9999);
      insert into wallets_crypto(currency, last_block_read, balance_min, balance_warn, balance_target, balance_max, balance) values('BTC', 42, 0, 0, 100, 1000, 9999);

      insert into users(id, email) values (0, '');
      insert into balances (user_id, currency) select 0, currency from currencies;
    """.execute()
  )

  def changeFeesToOneWay() = DB.withConnection(db)(implicit c =>
    SQL"""update trade_fees set one_way=true""".execute()
  )

  def setTradeFees(linear: BigDecimal) = DB.withConnection(db)(implicit c =>
    SQL"""update trade_fees set linear = ${linear.bigDecimal}""".execute()
  )

  def setFees(currency: String, method: String, depositConstant: BigDecimal, depositLinear: BigDecimal, withdrawConstant: BigDecimal, withdrawLinear: BigDecimal) = DB.withConnection(db)(implicit c =>
    SQL"""update dw_fees set
         deposit_constant = ${depositConstant.bigDecimal},
         deposit_linear = ${depositLinear.bigDecimal},
         withdraw_constant = ${withdrawConstant.bigDecimal},
         withdraw_linear = ${withdrawLinear.bigDecimal}
         where currency = $currency and method = $method""".execute()
  )

  // regular apis

  def flushMarketCaches(base: String, counter: String) {
    val caches = List("orders", "trades", "stats", "ticker")
    caches.foreach { c =>
      play.api.cache.Cache.remove("%s.%s.%s".format(base, counter, c))
    }
  }

  def balance(uid: Option[Long], apiKey: Option[String]) = DB.withConnection(db) { implicit c =>
    SQL"""select * from balance($uid, $apiKey)"""().map(row =>
      row[String]("currency") -> (row[BigDecimal]("amount"), row[BigDecimal]("hold"))
    ).toMap
  }

  def askBid(uid: Option[Long], apiKey: Option[String], base: String, counter: String, amount: BigDecimal, price: BigDecimal, isBid: Boolean) = DB.withConnection(db) { implicit c =>
    val mm = SQL""" select * from order_new($uid, $apiKey, $base, $counter, ${amount.bigDecimal}, ${price.bigDecimal}, $isBid) """();
    Logger.info("-------------------- mm is ------------- " + mm);
    val res = mm.map(row =>
      row[Option[Long]]("new_id") -> row[Option[BigDecimal]]("new_remains")
    ).head match {
      case (Some(id: Long), Some(remains: BigDecimal)) => Some(TradeResult(id, remains.bigDecimal.toPlainString))
      case _ => None
    }
    if (res.isDefined) {
      flushMarketCaches(base, counter)
    }
    res
  }

  def ordersDepth(base: String, counter: String) = DB.withConnection(db) { implicit c =>
    play.api.cache.Cache.getOrElse("%s.%s.orders".format(base, counter)) {
      val (asks, bids, total_base, total_counter) = SQL""" select * from orders_depth($base, $counter) """().map(row => (
        row[Option[Array[Array[java.math.BigDecimal]]]]("asks").getOrElse(Array[Array[java.math.BigDecimal]]()),
        row[Option[Array[Array[java.math.BigDecimal]]]]("bids").getOrElse(Array[Array[java.math.BigDecimal]]()),
        row[java.math.BigDecimal]("total_base"),
        row[java.math.BigDecimal]("total_counter")
      )).head
      EngineModel.orderBookFormat(asks, bids, total_base, total_counter)
    }
  }

  def userPendingTrades(uid: Option[Long], apiKey: Option[String], base: String, counter: String) = DB.withConnection(db) { implicit c =>
    SQL""" select * from user_pending_trades($uid, $apiKey,$base,$counter) """().map(row =>
      Trade(row[Long]("id"), if (row[Boolean]("is_bid")) "bid" else "ask", row[BigDecimal]("amount").bigDecimal.toPlainString, row[BigDecimal]("price").bigDecimal.toPlainString, row[DateTime]("created"))
    ).toList
  }

  def cancel(uid: Option[Long], apiKey: Option[String], orderId: Long) = DB.withConnection(db) { implicit c =>
    // the database confirms for us that the user owns the transaction before cancelling it
    val res = SQL""" select base, counter from order_cancel($uid, $apiKey, $orderId) """().map(row =>
      (row[Option[String]]("base"), row[Option[String]]("counter"))
    ).head
    res match {
      case (Some(base: String), Some(counter: String)) =>
        flushMarketCaches(base, counter)
        true
      case _ =>
        false
    }
  }

  //this method stores data in withdrawals_crypto table and returns id (process before mail sending)
  def withdraw(uid: Long, currency: String, amount: BigDecimal, address: String, otp: Option[String], tfa_code: Option[String], remarks: String) = DB.withConnection(db) { implicit c =>
    val code = tfa_code.getOrElse("0") match {
      case "" => 0
      case c: String =>
        try {
          Logger.info("<<<<<<<<<<<<<<<<<<<<<inside try of enginemodal c >>>>>>>>>>>>>>>>>>>>>>>>" + c)
          c.toInt
        } catch {
          case _: Throwable => 0
        }
    }
    val otpCode = otp.getOrElse("0") match {
      case "" => 0
      case c: String =>
        try {
          Logger.info("<<<<<<<<<<<<<<<<<<<<<inside try of enginemodal c >>>>>>>>>>>>>>>>>>>>>>>>" + c)
          c.toInt
        } catch {
          case _: Throwable => 0
        }
    }

    Logger.info("<<<<<<<<<<<<<<<<<<<<<inside try of enginemodal code >>>>>>>>>>>>>>>>>>>>>>>>" + code)
    Logger.info("<<<<<<<<<<<<uid--address><>>---address---currency>>>>>>>" + uid + "---------" + address + "----" + currency)
    SQL"""
  select withdraw_crypto as id from withdraw_crypto($uid, ${amount.bigDecimal}, $address, $currency,$otpCode, $code, $remarks)
  """().map(row => row[Option[Long]]("id")).head
  }

  def confirmWithdrawal(id: Long, token: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select confirm_withdrawal as success from confirm_withdrawal($id, $token)
    """().map(row => row[Boolean]("success")).head
  }

  def addresses(uid: Long, currency: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_addresses($uid, $currency)
    """().map(row => row[String]("o_address")).toList
  }

  def addresses_eth(uid: Long, currency: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_addresses_eth($uid, $currency)
    """().map(row => (row[String]("o_address"), row[String]("o_private_key"), row[String]("o_public_key"))
    )
  }
  def get_addresses_by_currency(uid: Long, currency: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_addresses_by_currency($uid, $currency)
    """().map(row => (row[String]("o_address"), row[Int]("o_node_id"), row[String]("o_currency"))
    )
  }

  //method for calling procedure to get toaddress based on id
  def to_addresses_eth(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_toAddress_eth($uid)
    """().map(row => (row[String]("address"))
    )
  }

  def get_withdrawals_currency(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_withdrawals_currency($uid)
    """().map(row => (row[String]("currency"))
    )
  }

  def amount_eth(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_amount_eth($uid)
    """().map(row => (row[BigDecimal]("amount"))
    )
  }

  def addresses(uid: Long) = DB.withConnection(db) { implicit c =>
    tuplesToGroupedMap(
      SQL"""select * from get_all_addresses($uid)"""().map(
        row => (row[String]("o_currency"), row[String]("o_address"))
      ).toList
    )
  }

  def get_last_24_price(base: String, counter: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from last_24_price($base,$counter);
    """().map(row => (row[Option[BigDecimal]]("price"))
    )
  }

  def tuplesToGroupedMap[K, L](list: List[(K, L)]) = {
    list.groupBy(_._1).map { (element: (K, List[(K, L)])) =>
      element._1 -> element._2.map(_._2)
    }
  }

  def pendingWithdrawals(uid: Long) = DB.withConnection(db) { implicit c =>
    tuplesToGroupedMap(
      SQL"""select * from user_pending_withdrawals($uid)"""().map(row =>
        (
          row[String]("currency"),
          Withdrawal(
            row[Long]("id"),
            row[BigDecimal]("amount").bigDecimal.toPlainString,
            row[BigDecimal]("fee").bigDecimal.toPlainString,
            row[DateTime]("created"),
            row[String]("info"),
            row[String]("currency"),
            Some(row[Boolean]("user_confirmed"))
          )
        )
      ).toList
    )
  }

  def pendingDeposits(uid: Long) = DB.withConnection(db) { implicit c =>
    tuplesToGroupedMap(
      SQL""" select * from user_pending_deposits($uid) """().map(row =>
        (
          row[String]("currency"),
          Withdrawal(
            row[Long]("id"),
            row[BigDecimal]("amount").bigDecimal.toPlainString,
            row[BigDecimal]("fee").bigDecimal.toPlainString,
            row[DateTime]("created"),
            row[String]("info"),
            row[String]("currency")
          )
        )
      ).toList
    )
  }

  def pairsCompleteData() = DB.withConnection(db) { implicit c =>
    var dd = BigDecimal.valueOf(0.0);
    SQL"""
    select * from pairs_complete_data()
    """().map(row =>
      PairsData(
        row[String]("base"),
        row[String]("counter"),
        row[Option[BigDecimal]]("sum"),
        row[Option[BigDecimal]]("last"),
        row[Option[BigDecimal]]("price")
      )
    ).toList
  }

  def recentTrades(base: String, counter: String) = DB.withConnection(db) { implicit c =>

    Json.toJson(SQL""" select * from recent_trades($base, $counter) """().map(row =>
      TradeHistory(row[BigDecimal]("amount").bigDecimal.toPlainString,
        BigDecimal(0).toString(), //the fee doesn't matter... TODO: don't include the fee here
        row[DateTime]("created"),
        row[BigDecimal]("price").bigDecimal.toPlainString,
        row[String]("base"),
        row[String]("counter"),
        if (row[Boolean]("is_bid")) "bid" else "ask")
    ).toList
    )
  }
  def volume() = DB.withConnection(db) { implicit c =>
    play.api.cache.Cache.getOrElse("volume") {
      Json.toJson(SQL""" select * from volume_sum() """().map(row =>
        Volume(row[String]("base"),
          row[BigDecimal]("volume").bigDecimal.toPlainString
        )
      ).toList
      )
    }
  }

  def getUpdatedMarkets(from: Timestamp, to: Timestamp) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_update_markets($from,$to) """().map(row =>
      row[Boolean]("get_update_markets")
    ).head;
  }

  def getUpdatedOrders(from: Timestamp, to: Timestamp) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_update_orders($from,$to) """().map(row =>
      row[Boolean]("get_update_orders")
    ).head;
  }

}
object EngineModel {
  def orderBookFormat(asks: Array[Array[java.math.BigDecimal]], bids: Array[Array[java.math.BigDecimal]], totalBase: java.math.BigDecimal, totalCounter: java.math.BigDecimal) = {
    val PriceIndex = 0
    val AmountIndex = 1
    Json.obj(
      "asks" -> asks.map { a: Array[java.math.BigDecimal] =>
        Json.obj(
          "amount" -> a(AmountIndex).toPlainString,
          "price" -> a(PriceIndex).toPlainString
        )
      },
      "bids" -> bids.map { b: Array[java.math.BigDecimal] =>
        Json.obj(
          "amount" -> b(AmountIndex).toPlainString,
          "price" -> b(PriceIndex).toPlainString
        )
      },
      "total_base" -> totalBase.toPlainString,
      "total_counter" -> totalCounter.toPlainString
    )
  }
}

case class Withdrawal(id: Long, amount: String, fee: String, created: DateTime, info: String, currency: String, confirmed: Option[Boolean] = None)

object Withdrawal {
  implicit val writes = Json.writes[Withdrawal]
  implicit val format = Json.format[Withdrawal]
}

case class Trade(id: Long, typ: String, amount: String, price: String, created: DateTime)

object Trade {
  implicit val writes = Json.writes[Trade]
  implicit val format = Json.format[Trade]
}

case class PairsData(base: String, counter: String, sum: Option[BigDecimal] = None, last: Option[BigDecimal] = None, price: Option[BigDecimal] = None)

object PairsData {
  implicit val writes = Json.writes[PairsData]
  implicit val format = Json.format[PairsData]
}

case class TradeResult(order: Long, remains: String)

object TradeResult {
  implicit val writes = Json.writes[TradeResult]
  implicit val format = Json.format[TradeResult]
}

case class Match(amount: BigDecimal, price: BigDecimal, created: DateTime, base: String, counter: String)
object Match {
  def toJson(m: Match) = {
    Json.obj(
      "amount" -> m.amount.bigDecimal.toPlainString,
      "price" -> m.price.bigDecimal.toPlainString,
      "created" -> m.created.toString,
      "pair" -> "%s/%s".format(m.base, m.counter),
      "base" -> m.base,
      "counter" -> m.counter
    )
  }
}
