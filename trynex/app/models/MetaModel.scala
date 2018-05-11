

package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.~
import play.api.libs.json.Json

case class DwFee(currency: String, method: String, depositConstant: String, depositLinear: String, withdrawConstant: String, withdrawLinear: String)

object DwFee {
  implicit val writes = Json.writes[DwFee]
  implicit val format = Json.format[DwFee]
}

case class TradeFee(linear: String, one_way: Boolean)

object TradeFee {
  implicit val writes = Json.writes[TradeFee]
  implicit val format = Json.format[TradeFee]
}

case class DwLimit(currency: String, limit_min: String, limit_max: String)

object DwLimit {
  implicit val writes = Json.writes[DwLimit]
  implicit val format = Json.format[DwLimit]
}

class MetaModel(val db: String = "default") {

  val currencies = DB.withConnection(db)(implicit c => {
    SQL""" select * from get_currencies() """().map(_[String]("currency")).toList
  })

  val validPairs = DB.withConnection(db)(implicit c => {
    val t = SQL""" select * from get_pairs() """().map(row => (row[String]("base"), row[String]("counter"), row[Boolean]("active"), row[BigDecimal]("limit_min"))).toList
    println("market is" + t);
    t
  })

  val allPairsJson = validPairs.map(pair => Json.obj("base" -> pair._1, "counter" -> pair._2))

  val activeMarkets = validPairs.map {
    case (base: String, counter: String, active: Boolean, minAmount: BigDecimal) =>
      (base, counter) -> (active, minAmount)
  }.toMap

  val dwFees = DB.withConnection(db)(implicit c => {
    SQL""" select * from dw_fees() """().map(row =>
      DwFee(
        row[String]("currency"),
        row[String]("method"),
        row[BigDecimal]("deposit_constant").bigDecimal.toPlainString,
        row[BigDecimal]("deposit_linear").bigDecimal.toPlainString,
        row[BigDecimal]("withdraw_constant").bigDecimal.toPlainString,
        row[BigDecimal]("withdraw_linear").bigDecimal.toPlainString
      )
    ).toList
  })

  val tradeFees = DB.withConnection(db)(implicit c => {
    SQL""" select * from trade_fees() """().map(row =>
      TradeFee(
        row[BigDecimal]("linear").bigDecimal.toPlainString,
        row[Boolean]("one_way")
      )
    ).headOption.getOrElse(TradeFee("0", false))
  })

  val dwLimits = DB.withConnection(db)(implicit c => {
    SQL""" select * from dw_limits() """().map(row =>
      row[String]("currency") ->
        DwLimit(
          row[String]("currency"),
          row[BigDecimal]("limit_min").bigDecimal.toPlainString,
          row[BigDecimal]("limit_max").bigDecimal.toPlainString
        )
    ).toMap
  })

  val getRequiredConfirmations = DB.withConnection(db)(implicit c => {
    SQL"""select * from get_required_confirmations()"""().map(row =>
      row[String]("currency") -> row[Int]("min_deposit_confirmations").toString
    ).toMap
  })

  // privileged api

  def clean() = DB.withConnection(db)(implicit c =>
    SQL"""delete from constants;
      delete from currencies;
      delete from markets;
      """.execute()
  )

}