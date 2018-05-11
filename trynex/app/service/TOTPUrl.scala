
package service

import play.api.Play
import views.html.helper

object TOTPUrl {
  def totpSecretToUrl(email: String, secret: TOTPSecret) = {
    val applicationName = Play.current.configuration.getString("application.name").get +
      (if (Play.current.configuration.getBoolean("fakeexchange").get) " (testnet)" else "")
    "otpauth://totp/%s?secret=%s&issuer=%s".format(
      helper.urlEncode("%s: %s".format(applicationName, email)).replace("+", "%20"),
      secret.toBase32,
      helper.urlEncode(applicationName))
  }
}
