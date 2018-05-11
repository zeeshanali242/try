package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.Play
import java.sql.Timestamp
import org.joda.time.DateTime
import securesocial.core.{ Token, SocialUser }
import service.{ PGP, TOTPSecret }
import play.api.libs.json.Json
import java.security.SecureRandom
import anorm.JodaParameterMetaData._
import play.api.Logger

case class TradeHistory(amount: String, fee: String, created: DateTime, price: String, base: String, counter: String, typ: String, id: Option[Long] = None)

case class Volume(base: String, volume: String)

case class Chart(start_of_period: DateTime, open: BigDecimal, high: BigDecimal, low: BigDecimal, close: BigDecimal, volume: BigDecimal)

case class UserKycProvider(user_ifsc_code: Option[String], bank_name: Option[String], bank_branch_name: Option[String], user_mobile_no: Option[String], user_account_no: Option[String], user_account_holder_name: Option[String], account_type: Option[String], pancard_number: Option[String], pancard_dob: Option[String], pancard_image_path: Option[String], aadhar_name: Option[String], aadhar_number: Option[String], aadhar_address: Option[String], aadhar_front_image_path: Option[String], aadhar_back_image_path: Option[String], aadhar_pincode: Option[String])

object TradeHistory {
  implicit val writes = Json.writes[TradeHistory]
  implicit val format = Json.format[TradeHistory]
}

object Volume {
  implicit val writes = Json.writes[Volume]
  implicit val format = Json.format[Volume]
}

case class DepositWithdrawHistory(id: Long, amount: String, fee: String, created: DateTime, currency: String, typ: String, status: String, address: String, remarks: String)
object DepositWithdrawHistory {
  implicit val writes = Json.writes[DepositWithdrawHistory]
  implicit val format = Json.format[DepositWithdrawHistory]
}

case class ApiKey(api_key: String, comment: String, created: DateTime, trading: Boolean, trade_history: Boolean, list_balance: Boolean)

object ApiKey {
  implicit val writes = Json.writes[ApiKey]
  implicit val format = Json.format[ApiKey]
}

object UserKycProvider {
  implicit val writes = Json.writes[UserKycProvider]
  implicit val format = Json.format[UserKycProvider]
}

class UserModel(val db: String = "default") {

  def create(email: String, first_name: String, last_name: String, mobile_number: String, password: String, onMailingList: Boolean, pgp: Option[String], token: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select create_user_complete as id from create_user_complete($email, $first_name , $last_name , $mobile_number,  $password, $onMailingList, $pgp, $token)
    """.map(row => row[Option[Long]]("id")).list.head
  }

  // insecure version, usable only in tests
  def create(email: String, password: String, onMailingList: Boolean) = DB.withConnection(db) { implicit c =>
    SQL"""
    select create_user as id from create_user($email, $password, $onMailingList, null, 'en')
    """.map(row => row[Long]("id")).list.headOption
  }

  def addFakeMoney(uid: Long, currency: String, amount: BigDecimal) = DB.withConnection(db) { implicit c =>
    if (Play.current.configuration.getBoolean("fakeexchange").get) {
      try {
        SQL"""select * from add_fake_money($uid, $currency, ${amount.bigDecimal})""".execute()
      } catch {
        case _: Throwable =>
          false
      }
    } else {
      false
    }
  }

  def exportDataOrderTransaction(finalString: String) = DB.withConnection(db) { implicit c =>
    SQL"""select export_order as success from export_order($finalString)"""().map(row =>
      row[Boolean]("success")
    ).head
  }

  def exportDataWalletTransaction(finalString: String) = DB.withConnection(db) { implicit c =>
    SQL"""select export_wallet as success from export_wallet($finalString)"""().map(row =>
      row[Boolean]("success")
    ).head
  }

  def findUserById(id: Long): Option[SocialUser] =
    DB.withConnection(db) { implicit c =>
      SQL"""select * from find_user_by_id($id)"""().map(row =>
        new SocialUser(
          row[Long]("id"),
          row[String]("email"),
          row[String]("first_name"),
          row[String]("last_name"),
          row[String]("mobile_number"),
          row[Int]("verification"),
          row[String]("language"),
          row[Boolean]("on_mailing_list"),
          row[Boolean]("tfa_enabled"),
          row[Option[String]]("pgp"),
          row[String]("kyc")
        )
      ).headOption
    }
  def findUserByEmail(email: String): Option[SocialUser] =
    DB.withConnection(db) { implicit c =>
      SQL"""select * from find_user($email)"""().map(row =>
        new SocialUser(
          row[Long]("id"),
          row[String]("email"),
          row[String]("first_name"),
          row[String]("last_name"),
          row[String]("mobile_number"),
          row[Int]("verification"),
          row[String]("language"),
          row[Boolean]("on_mailing_list"),
          row[Boolean]("tfa_enabled"),
          row[Option[String]]("pgp"),
          row[String]("kyc")
        )
      ).headOption
    }

  def getUserKycData(id: Long) = DB.withConnection(db) { implicit c =>
    SQL"""select * from get_kyc_data($id)"""().map(row =>
      UserKycProvider(
        row[Option[String]]("user_ifsc_code"),
        row[Option[String]]("bank_name"),
        row[Option[String]]("bank_branch_name"),
        row[Option[String]]("user_mobile_no"),
        row[Option[String]]("user_account_no"),
        row[Option[String]]("user_account_holder_name"),
        row[Option[String]]("account_type"),
        row[Option[String]]("pancard_number"),
        row[Option[String]]("pancard_dob"),
        row[Option[String]]("pancard_image_path"),
        row[Option[String]]("aadhar_name"),
        row[Option[String]]("aadhar_number"),
        row[Option[String]]("aadhar_address"),
        row[Option[String]]("aadhar_front_image_path"),
        row[Option[String]]("aadhar_back_image_path"),
        row[Option[String]]("aadhar_pincode")
      )
    ).headOption
  }

  def userExists(email: String, mobile: String): Boolean = DB.withConnection(db) { implicit c =>
    SQL"select * from user_exists($email,$mobile)"().map(row =>
      row[Boolean]("user_exists")
    ).head
  }

  def userExistsByEmail(email: String): Boolean = DB.withConnection(db) { implicit c =>
    SQL"select * from user_exists_email($email)"().map(row =>
      row[Boolean]("user_exists")
    ).head
  }

  def userExistsByMobile(mobile: String): Boolean = DB.withConnection(db) { implicit c =>
    SQL"select * from user_exists_mobile($mobile)"().map(row =>
      row[Boolean]("user_exists")
    ).head
  }

  def userSmsOtpCheck(id: Long, otp: Integer): Boolean = DB.withConnection(db) { implicit c =>
    SQL"select * from user_sms_otp_check($id,$otp)"().map(row =>
      row[Boolean]("user_sms_otp_check")
    ).head
  }

  def userHasTotp(email: String): Boolean = DB.withConnection(db) { implicit c =>
    SQL"select * from user_has_totp($email)"().map(row =>
      row[Option[Boolean]]("user_has_totp").getOrElse(false)
    ).head
  }

  def totpLoginStep1(email: String, password: String, browserHeaders: String, ip: String, os: String, browser: String): Option[String] = DB.withConnection(db) { implicit c =>
    SQL"""
     select totp_login_step1($email, $password, $browserHeaders, inet($ip),$os,$browser)
    """().map(row =>
      row[Option[String]]("totp_login_step1")
    ).head
  }

  def totpLoginStep2(email: String, totpHash: String, totpToken: String, browserHeaders: String, ip: String, os: String, browser: String): Option[SocialUser] = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from totp_login_step2($email, $totpHash, ${safeToInt(totpToken)}, $browserHeaders, inet($ip),$os,$browser)
    """().map(row => (row[Option[Long]]("id"),
      row[Option[String]]("email"),
      row[Option[String]]("first_name"),
      row[Option[String]]("last_name"),
      row[Option[String]]("mobile_number"),
      row[Option[Int]]("verification"),
      row[Option[Boolean]]("on_mailing_list"),
      row[Option[Boolean]]("tfa_enabled"),
      row[Option[String]]("pgp"),
      row[String]("language")) match {
        case (Some(id: Long),
          Some(email: String),
          Some(first_name: String),
          Some(last_name: String),
          Some(mobile_number: String),
          Some(verification: Int),
          Some(on_mailing_list: Boolean),
          Some(tfa_enabled: Boolean),
          pgp: Option[String],
          language: String) =>
          Some(SocialUser(id, email, first_name, last_name, mobile_number, verification, language, on_mailing_list, tfa_enabled, pgp))
        case _ =>
          None
      }
    ).head
  }
  def totpLoginSMS(email: String, otp: Int, browserHeaders: String, ip: String, os: String, browser: String): Option[SocialUser] = DB.withConnection(db) { implicit c =>
    SQL"""
     select * from totp_login_smsotp($email, $otp, $browserHeaders, inet($ip),$os,$browser)
     """().map(row => (row[Option[Long]]("id"),
      row[Option[String]]("email"),
      row[Option[String]]("first_name"),
      row[Option[String]]("last_name"),
      row[Option[String]]("mobile_number"),
      row[Option[Int]]("verification"),
      row[Option[Boolean]]("on_mailing_list"),
      row[Option[Boolean]]("tfa_enabled"),
      row[Option[String]]("pgp"),
      row[Option[String]]("language")) match {
        case (Some(id: Long),
          Some(email: String),
          Some(first_name: String),
          Some(last_name: String),
          Some(mobile_number: String),
          Some(verification: Int),
          Some(on_mailing_list: Boolean),
          Some(tfa_enabled: Boolean),
          pgp: Option[String],
          Some(language: String)) =>
          Some(SocialUser(id, email, first_name, last_name, mobile_number, verification, language, on_mailing_list, tfa_enabled, pgp))
        case _ =>
          None
      }
    ).head
  }
  def findUserByEmailAndPassword(email: String, password: String, browserHeaders: String, ip: String, os: String, browser: String): Option[SocialUser] = DB.withConnection(db) { implicit c =>
    SQL"""
     select * from find_user_by_email_and_password($email, $password, $browserHeaders, inet($ip), $os, $browser)
    """().map(row => (row[Option[Long]]("id"),
      row[Option[String]]("email"),
      row[Option[String]]("first_name"),
      row[Option[String]]("last_name"),
      row[Option[String]]("mobile_number"),
      row[Option[Int]]("verification"),
      row[Option[Boolean]]("on_mailing_list"),
      row[Option[Boolean]]("tfa_enabled"),
      row[Option[String]]("pgp"),
      row[String]("language")) match {
        case (Some(id: Long),
          Some(email: String),
          Some(first_name: String),
          Some(last_name: String),
          Some(mobile_number: String),
          Some(verification: Int),
          Some(on_mailing_list: Boolean),
          Some(tfa_enabled: Boolean),
          pgp: Option[String],
          language: String) =>
          Some(SocialUser(id, email, first_name, last_name, mobile_number, verification, language, on_mailing_list, tfa_enabled, pgp))
        case _ =>
          None
      }
    ).head
  }

  def tradeHistory(uid: Option[Long], apiKey: Option[String], before: Option[DateTime] = None, limit: Option[Int] = None, lastId: Option[Long] = None) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from trade_history($uid, $apiKey, $before, $limit, $lastId)
    """().map(row =>
      TradeHistory(row[BigDecimal]("amount").bigDecimal.toPlainString,
        row[BigDecimal]("fee").bigDecimal.toPlainString,
        row[DateTime]("created"),
        row[BigDecimal]("price").bigDecimal.toPlainString,
        row[String]("base"),
        row[String]("counter"),
        if (row[Boolean]("is_bid")) "bid" else "ask",
        Some(row[Long]("id")))
    ).toList
  }

  def depositWithdrawHistory(uid: Long, before: Option[DateTime] = None, limit: Option[Int] = None, lastId: Option[Long] = None) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from deposit_withdraw_history($uid, $before, $limit, $lastId)
    """().map(row =>
      DepositWithdrawHistory(
        row[Long]("id"),
        row[BigDecimal]("amount").bigDecimal.toPlainString,
        row[BigDecimal]("fee").bigDecimal.toPlainString,
        row[DateTime]("created"),
        row[String]("currency"),
        row[String]("type"),
        row[String]("status"),
        row[Option[String]]("address").getOrElse("N/A"),
        row[String]("remarks"))
    ).toList
  }

  def findToken(token: String): Option[Token] = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from find_token($token)
    """().map(row =>
      Token(token, row[String]("email"), row[DateTime]("creation"), row[DateTime]("expiration"), row[Boolean]("is_signup"), row[String]("language"), row[String]("first_name"), row[String]("last_name"), row[String]("mobile_number"), row[String]("password"))
    ).headOption
  }

  def deleteToken(token: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from delete_token($token)
    """.execute()
  }

  def deleteExpiredTokens() = DB.withConnection(db) { implicit c =>
    SQL"""select * from delete_expired_tokens()""".execute()
  }

  def deleteExpiredTOTPBlacklistTokens() = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from delete_expired_totp_blacklist_tokens()
    """.execute()
  }

  def saveUser(id: Long, email: String, onMailingList: Boolean) = DB.withConnection(db) { implicit c =>
    SQL"select * from update_user($id, $email, $onMailingList)".execute()
  }

  def userChangePass(id: Long, oldPassword: String, newPassword: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select user_change_password($id, $oldPassword, $newPassword)
    """().map(row =>
      row[Boolean]("user_change_password")
    ).head
  }

  def userResetPassComplete(email: String, token: String, password: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select user_reset_password_complete as success from user_reset_password_complete($email, $token, $password)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def trustedActionStart(email: String, isSignup: Boolean, language: String, first_name: String, last_name: String, mobile_number: String, password: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select trusted_action_start as success from trusted_action_start($email, $isSignup, $language, $first_name, $last_name, $mobile_number,$password)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def genTFASecret(uid: Long) = DB.withConnection(db) { implicit c =>
    val secret = TOTPSecret()
    val random = new SecureRandom
    // build a string that will be parsed into an array in the postgres function
    // generate a 6 digit random number that doesn't start with a 0
    val otps = Seq.fill(10)((random.nextInt(9) + 1).toString concat "%05d".format(random.nextInt(100000)))
    // everything is off by default
    val success = SQL"""
    select update_tfa_secret as success from update_tfa_secret($uid, ${secret.toBase32}, ${otps.mkString(",")})
    """().map(row =>
      row[Boolean]("success")
    ).head
    if (success) {
      Some(secret, otps)
    } else {
      None
    }
  }

  def turnOffTFA(uid: Long, tfa_code: String, password: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select turnoff_tfa as success from turnoff_tfa($uid, ${safeToInt(tfa_code)}, $password)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def addPGP(uid: Long, password: String, tfa_code: Option[String], pgp: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select user_add_pgp as success from user_add_pgp($uid, $password, ${optStrToInt(tfa_code)}, $pgp)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def removePGP(uid: Long, password: String, tfa_code: Option[String]) = DB.withConnection(db) { implicit c =>
    SQL"""
    select user_remove_pgp as success from user_remove_pgp($uid, $password, ${optStrToInt(tfa_code)});
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def turnOnTFA(uid: Long, tfa_code: String, password: String) = DB.withConnection(db) { implicit c =>
    SQL"""
     select turnon_tfa as success from turnon_tfa($uid, ${safeToInt(tfa_code)}, $password)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def turnOffEmails(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select turnoff_emails as success from turnoff_emails($uid)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def turnOnEmails(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select turnon_emails as success from turnon_emails($uid)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def changeLanguage(uid: Long, language: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select change_language as success from change_language($uid, $language)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def userPgpByEmail(email: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from user_pgp_by_email($email)
    """().map(row =>
      row[Option[String]]("pgp")
    ).head
  }

  def addApiKey(uid: Long, apiKey: String) = DB.withConnection(db) { implicit c =>
    SQL"""
     select add_api_key($uid, $apiKey)
    """.execute()
  }

  def updateApiKey(uid: Long, tfa_code: Option[String], apiKey: String, comment: String, trading: Boolean, tradeHistory: Boolean, listBalance: Boolean) = DB.withConnection(db) { implicit c =>
    SQL"""
     select update_api_key as success from update_api_key($uid, ${optStrToInt(tfa_code)}, $apiKey, $comment, $trading, $tradeHistory, $listBalance)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def disableApiKey(uid: Long, tfa_code: Option[String], apiKey: String) = DB.withConnection(db) { implicit c =>
    SQL"""
     select disable_api_key as success from disable_api_key($uid, ${optStrToInt(tfa_code)}, $apiKey)
    """().map(row =>
      row[Boolean]("success")
    ).head
  }

  def getApiKeys(uid: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
     select * from get_api_keys($uid)
    """().map(row => ApiKey(
      row[String]("api_key"),
      row[String]("comment"),
      row[DateTime]("created"),
      row[Boolean]("trading"),
      row[Boolean]("trade_history"),
      row[Boolean]("list_balance"))
    ).toList
  }

  def saveToken(token: Token) = DB.withConnection(db) { implicit c =>
    SQL"""
   insert into tokens (token, email, creation, expiration, is_signup, language, first_name, last_name, mobile_number,password)
   values (${token.uuid}, ${token.email}, ${new Timestamp(token.creationTime.getMillis)}, ${new Timestamp(token.expirationTime.getMillis)}, ${token.isSignUp}, ${token.language}, ${token.first_name}, ${token.last_name}, ${token.mobile_number},${token.password})
   """.execute
  }

  def saveWithdrawalToken(id: Long, token: String, expiration: DateTime) = DB.withConnection(db) { implicit c =>
    SQL"""update withdrawals set confirmation_token = $token, token_expiration = ${new Timestamp(expiration.getMillis)} where id = $id""".execute
  }
  def saveOTP(id: Long, sms_otp: Int) = DB.withConnection(db) { implicit c =>
    SQL"""
  insert into mobile_otps (user_id , otp)
  values($id, $sms_otp)
  """.execute()
  }

  def updateOTP(id: Long, otp: Int) = DB.withConnection(db) { implicit c =>
    SQL"""
     select update_otp as success from update_otp($id,$otp)
    """().map(row =>
      row[Boolean]("success")
    ).head

  }

  private def optStrToInt(optStr: Option[String]) = {
    safeToInt(optStr.getOrElse(""))
  }

  private def safeToInt(str: String) = {
    try {
      str.toInt
    } catch {
      case _: Throwable => 0
    }
  }
}
