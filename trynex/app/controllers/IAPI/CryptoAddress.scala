
package controllers.IAPI

import org.bitcoinj.core.Address
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.NetworkParameters._
import play.Logger
import org.web3j.crypto.WalletUtils

object CryptoAddress {

  private val BitcoinTestnet = new NetworkParameters {
    Logger.info(">>>bitcoin start")
    id = ID_TESTNET
    port = 18332
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val Bitcoin = new NetworkParameters {
    Logger.info(">>>bitcoin start>>>>>>")
    id = ID_MAINNET
    port = 8333
    addressHeader = 0
    p2shHeader = 5
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_MAINNET
  }

  private val LitecoinTestnet = new NetworkParameters {
    id = ID_TESTNET
    port = 19333
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val Litecoin = new NetworkParameters {
    id = ID_MAINNET
    port = 9333
    addressHeader = 48
    p2shHeader = 5
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_MAINNET
  }

  private val PeercoinTestnet = new NetworkParameters {
    id = ID_TESTNET
    port = 9903
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val Peercoin = new NetworkParameters {
    id = ID_MAINNET
    port = 9901
    addressHeader = 55
    p2shHeader = 117
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_MAINNET
  }

  private val PrimecoinTestnet = new NetworkParameters {
    id = ID_TESTNET
    port = 9913
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val Primecoin = new NetworkParameters {
    id = ID_MAINNET
    port = 9911
    addressHeader = 23
    p2shHeader = 83
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_MAINNET
  }

  private val EtherTestnet = new NetworkParameters {
    Logger.info(">>>EtherTestnet start")
    id = ID_TESTNET
    port = 8545
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)
    Logger.info(">>>EtherTestnet end")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET

  }

  private val ZCashTestnet = new NetworkParameters {
    Logger.info(">>>EtherTestnet start")
    id = ID_TESTNET
    port = 55678
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)
    Logger.info(">>>ZCashTestnet end")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET

  }

  private val DashTestnet = new NetworkParameters {
    Logger.info(">>>EtherTestnet start")
    id = ID_TESTNET
    port = 9998
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)
    Logger.info(">>>DashTestnet end")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET

  }

  private val EtherMain = new NetworkParameters {
    Logger.info(">>>Ethermain start")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_MAINNET
  }

  private val BitcoinCashTestnet = new NetworkParameters {
    Logger.info(">>>bitcoin start")
    id = ID_TESTNET
    port = 18332
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)

    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val XrpIsValidAddressTest = new NetworkParameters {
    Logger.info(">>>EtherTestnet start")
    id = ID_TESTNET
    port = 8545
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)
    Logger.info(">>>EtherTestnet end")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  private val XrpIsValidAddressMain = new NetworkParameters {
    Logger.info(">>>EtherTestnet start")
    id = ID_TESTNET
    port = 8545
    addressHeader = 111
    p2shHeader = 196
    acceptableAddressCodes = Array[Int](addressHeader, p2shHeader)
    Logger.info(">>>EtherTestnet end")
    val getPaymentProtocolId: String = PAYMENT_PROTOCOL_ID_TESTNET
  }

  def isValid(address: String, currency: String, test: Boolean): Boolean = {
    var testnet: Boolean = true;
    Logger.info(">>>>>>>>isValid>>>>>>>>" + currency)
    val network = currency match {

      case "BTC" if testnet => BitcoinTestnet
      case "BTC" => Bitcoin
      case "LTC" if testnet => LitecoinTestnet
      case "LTC" => Litecoin
      case "PPC" if testnet => PeercoinTestnet
      case "PPC" => Peercoin
      case "XPM" if testnet => PrimecoinTestnet
      case "XPM" => Primecoin
      case "ETH" if testnet => EtherTestnet
      case "ETH" => EtherMain
      case "XRP" if testnet => XrpIsValidAddressTest
      case "XRP" => XrpIsValidAddressMain
      case "DASH" if testnet => XrpIsValidAddressTest
      case "DASH" => XrpIsValidAddressMain
      case "ZEC" if testnet => XrpIsValidAddressTest
      case "ZEC" => XrpIsValidAddressMain

      case _ =>
        return false
    }
    try {
      if (currency == "ETH") {
        WalletUtils.isValidAddress(address)
      } else if (currency == "XRP") {
        // Logger.info("adrees is==========" + xrpIsValidAddress(address))
        val res = xrpIsValidAddress(address)
        if (res == None) {
          Logger.info("invalid address for xrp")
          return false
        }

      } else if (currency == "DASH") {
        // Logger.info("adrees is==========" + xrpIsValidAddress(address))
        val res = checkDashAddress(address)
        if (res == None) {
          Logger.info("invalid address for dash >> " + address)
          return false
        }
      } else if (currency == "ZEC") {
        // Logger.info("adrees is==========" + xrpIsValidAddress(address))
        val res = checkZcashAddress(address)
        if (res == None) {
          Logger.info("invalid address for zcash")
          return false
        }

      } else
        new Address(network, address)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return false
    }
    true
  }

  def xrpIsValidAddress(address: String) = {
    val pattern = "^[r][A-za-z0-9]{27,33}\b".r
    val res = pattern findFirstIn address
    Logger.info("check " + res)
    res
  }

  def checkDashAddress(address: String) = {
    val pattern = "^[a-z][1-9A-HJ-NP-Za-km-z]{33}".r
    val res = pattern findFirstIn address
    Logger.info("check " + res)
    res
  }

  def checkZcashAddress(address: String) = {
    Logger.info("====address-----" + address)
    val pattern = "^[t][0-9A-HJ-NP-Za-km-z]{34}".r
    Logger.info("===pattern===" + pattern)
    val res = pattern findFirstIn address
    Logger.info("check " + res)
    res
  }

}
