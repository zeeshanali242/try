package service;
import java.math.BigInteger;
import wallet._
import play.Logger
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

import org.web3j.crypto.Credentials
import org.web3j.crypto.{ Credentials, ECKeyPair, Keys, WalletFile, WalletUtils, Wallet => WebWallet }
import org.web3j.crypto.{ Credentials, RawTransaction, TransactionEncoder }
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName.LATEST
import org.web3j.utils.Convert.Unit.{ ETHER, WEI }
import org.web3j.tx.Transfer
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName

class EtherTransactionService {

  def ethRawTransaction(fromAddress: String, fromPPK: String, toAddress: String, amount: BigInteger) = {
    Logger.info("-----------------------currency is ETH----------------------------")
    //calculate next available nonce before creating transaction
    val address: Address = Address(fromAddress)
    val nonce: Nonce = nextNonce(address)
    Logger.info("--------------------nonce----------" + nonce)
    val GAS_PRICE: BigInteger = BigInteger.valueOf(20000000000L)
    val GAS_LIMIT_ETHER_TX: BigInteger = BigInteger.valueOf(21000)
    // Logger.info("-----amount-----" + amount.bigInteger)
    Logger.info("-----nonce-----" + nonce.bigInteger)
    Logger.info("-----GAS_PRICE-----" + GAS_PRICE)
    Logger.info("-----GAS_LIMIT_ETHER_TX-----" + GAS_LIMIT_ETHER_TX)
    Logger.info("-----toAddress-----" + toAddress)
    //create raw transaction
    var staticInt = 1;
    val rawTransaction: RawTransaction =
      RawTransaction.createEtherTransaction(nonce.bigInteger, GAS_PRICE, GAS_LIMIT_ETHER_TX, toAddress, BigInt(staticInt).bigInteger)
    Logger.info("-----------------rawTransaction-------------" + rawTransaction)

    val credentials: Credentials = Credentials.create(fromPPK)
    val keyPair: ECKeyPair = credentials.getEcKeyPair();

    val web3j = Web3j.build(new HttpService())

    // Sign & send the transaction
    val signedMessage: Array[Byte] = TransactionEncoder.signMessage(rawTransaction, credentials)
    val hexValue: String = javax.xml.bind.DatatypeConverter.printHexBinary(signedMessage)

    try {
      Logger.info("------------hexValue------------" + hexValue)
      val transactionHash: String = web3j.ethSendRawTransaction(hexValue).send().getTransactionHash()
      // Thread.sleep(300000)
      Logger.info("-------------transactionHash--------" + transactionHash)

    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }

    // Now let's transfer some funds. Be sure a wallet is available in the client’s keystore. TODO how?
    // One option is to use web3j’s `Transfer` class for transacting with Ether.

    //Transfer.sendFunds(web3j, credentials, "0x861ae192db8d68b839108023b7fb20ce541a1b03", BigDecimal(1).bigDecimal, WEI)

    val ethGetBalance: EthGetBalance = web3j
      .ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST)
      .sendAsync()
      .get();

    val wei: BigInteger = ethGetBalance.getBalance();
    Logger.info("-----------------sender balance aftere tx------------" + wei)

    Logger.info("---receiver amount after tx--------" + web3j
      .ethGetBalance(toAddress, DefaultBlockParameterName.LATEST)
      .sendAsync()
      .get().getBalance)

  }
  private def nonce(address: Address, defaultBlockParameter: DefaultBlockParameter): Nonce = {
    val web3j = Web3j.build(new HttpService())
    Nonce(web3j.ethGetTransactionCount(address.value, defaultBlockParameter).send.getTransactionCount)
  }

  private def nextNonce(address: Address): Nonce = nonce(address, LATEST)

}
