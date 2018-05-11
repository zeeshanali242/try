
package wallet

import java.io.File
import play.api.i18n.{ DefaultLangs, DefaultMessagesApi, Lang, Messages }
import play.api.{ Mode, Play }
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.googlecode.jsonrpc4j.JsonRpcHttpClient
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import play.api.libs.json._
import play.Logger
import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.collection.mutable
import scala.collection.JavaConverters._
import wallet.WalletModel._
import wallet.Wallet._
import wallet.Wallet.CryptoCurrency.CryptoCurrency
import wallet.Wallet.CryptoCurrency.CryptoCurrency
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.crypto.{ Credentials, ECKeyPair, Keys, WalletFile, WalletUtils, Wallet => WebWallet }
import org.web3j.crypto.{ Credentials, RawTransaction, TransactionEncoder }
import java.math.BigInteger;
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName.LATEST
import wallet._
import org.web3j.tx.Transfer
import org.web3j.utils.Convert.Unit.{ ETHER, WEI }
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import models.EtherCrypto
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys;
import scala.collection.JavaConversions._
import resource._
import org.json.simple.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedWriter;
import com.fasterxml.jackson.annotation.{ JsonIgnore, JsonProperty }
import scala.collection.mutable.ListBuffer

class Wallet(rpc: JsonRpcHttpClient, currency: CryptoCurrency, nodeId: Int, params: WalletParams, walletModel: WalletModel, test: Boolean = false) extends Actor {
  import context.system
  implicit val ec: ExecutionContext = system.dispatcher

  final val (active, minDepositConfirmations, minWithdrawalConfirmations) = walletModel.getMinConfirmations(currency)
  final val minConfirmations = math.max(minDepositConfirmations, minWithdrawalConfirmations)
  final val (retired, balanceMin, balanceWarn, balanceTarget, balanceMax) = walletModel.getNodeInfo(currency, nodeId)

  private var lastBlockRead: Int = _
  private var lastWithdrawalTimeReceived: Int = _
  private var lastWithdrawalBlock: Int = _
  private var firstUpdate: Boolean = _
  private var changeAddressCount: Int = _
  private var balance: BigDecimal = _
  private var refillRequested: Boolean = _

  // Cache of pending deposits, initialized from DB
  private var pendingDeposits: mutable.Map[Deposit, Long] = _
  // Cache of confirmed deposits, populated on first run of update()
  private var confirmedDeposits: mutable.Set[Deposit] = _
  // Last batch withdrawal created, initialized from DB
  private var sentWithdrawalTx: ListBuffer[Option[(Long, String)]] = _
  // Last withdrawal if it could not be sent
  private var stalledWithdrawalTx: Some[(Long, BigDecimal, Map[String, BigDecimal])] = _

  private def initMemberVars(): Unit = {
    /* val map = collection.mutable.Map[String, BigDecimal]()
    map.put("n2kLYchixAQCEon628imNfmSp8yRsxGvs1", 1)
    val fr = "mp2bcdSJU24HnJjnSCw79PKZsKMy37v7jX";

    val h = sendMany(map.asJava, fr);
    Logger.info("hash is  >>>>>>>>>>>>>>>>>>> " + h);*/
    Logger.info("-----walletModel.getLastBlockRead(currency, nodeId)------" + walletModel.getLastBlockRead(currency, nodeId))
    walletModel.getLastBlockRead(currency, nodeId) match {
      case (lastBlock, withdrawalTimeReceived) if lastBlock > minConfirmations =>
        Logger.info("-------------lastBlock-----------" + lastBlock)
        Logger.info("-----------withdrawalTimeReceived-------" + withdrawalTimeReceived)

        lastBlockRead = lastBlock
        lastWithdrawalTimeReceived = withdrawalTimeReceived
      case (lastBlock, withdrawalTimeReceived) =>
        lastBlockRead = minConfirmations
        lastWithdrawalTimeReceived = withdrawalTimeReceived
    }
    lastWithdrawalBlock = lastBlockRead
    firstUpdate = true
    changeAddressCount = params.addressPool / 2
    balance = walletModel.getBalance(currency, nodeId)
    refillRequested = false
    pendingDeposits = mutable.Map(walletModel.getPendingDeposits(currency, nodeId): _*)
    confirmedDeposits = mutable.Set.empty
    sentWithdrawalTx = walletModel.getUnconfirmedWithdrawalTx(currency, nodeId).to[ListBuffer]
    Logger.info("sentWithdrawalTx list is >>>>>>> " + sentWithdrawalTx);
    /*for (sentWithdrawalObject <- sentWithdrawalTx) {

    }*/

    /*stalledWithdrawalTx = sentWithdrawalTx match {
      // If a withdrawal was created but not marked as sent
      case Some((id, "")) =>
        val withdrawalId = id

        // Set sentWithdrawalTx to the last confirmed withdrawal for verification
        sentWithdrawalTx = walletModel.getLastConfirmedWithdrawalTx(currency, nodeId)
        val withdrawals = walletModel.getWithdrawalTxData(withdrawalId) // break call send many with list
        val withdrawalsTotal = withdrawals.view.map(_._2).sum
        Logger.info(" 2nd If any unconfirmed trascation found then withdrawals data is " + withdrawals + " withdrawalsTotal is " + withdrawalsTotal + "  Transaction id is " + withdrawalId + " LastConfirmedWithdrawalTx is " + sentWithdrawalTx);
        Logger.info("2nd walletModel.getColdStorageTransfer(withdrawalId) >> " + walletModel.getColdStorageTransfer(withdrawalId));
        Some(withdrawalId, withdrawalsTotal, walletModel.getColdStorageTransfer(withdrawalId) match {
          case Some((address, value)) =>
            withdrawals + (address -> value)
          case _ =>
            withdrawals
        })
      case _ =>
        None
    }*/

  }

  // Check for new deposits every average block time
  // Check available addresses every 2 hours
  val (blockTimer, addressTimer) = {
    // Logger.info("init call scheduler blockTimer is " + blockTimer + "  >>> addressTimer is " + addressTimer);
    initMemberVars()
    //ccall send transaction for testing
    Logger.info(">>>>>>>>>>>. Test is " + test + " stalledWithdrawalTx >>> " + stalledWithdrawalTx);
    if (test) {
      Logger.info(" >>>>>>>>>> if scheduler called");
      (null, null)
    } else if (active && !retired && walletModel.obtainSessionLock(currency, nodeId)) {
      Logger.info(" >>>>>>>>>> Else scheduler called");
      (
        system.scheduler.schedule(params.checkDelay, params.checkInterval)(update()),
        system.scheduler.schedule(params.addressDelay, params.addressInterval)(generateAddresses())
      )
    } else {
      throw new RuntimeException("Wallet disabled or already running")
      (null, null)
    }
  }

  def update(): Unit = {
    Logger.info(">>>>>>>>>>>>>>> update invoked >>>>>>>>>>>>>")

    Logger.debug("[wallet] [%s, %s] running update".format(currency, nodeId))
    if (currency.toString() != "ETH") {
      Logger.info("---------------------currency is bTC or LTC---------")

      val blockHeight = try {
        var a = getBlockCount
        Logger.debug("[wallet] [%s] block number is and last read block is [%s]".format(a, lastBlockRead))
        a
      } catch {
        case _: Throwable =>
          Logger.error("[wallet] [%s, %s] cannot get block count from RPC".format(currency, nodeId))
          return
      }
      if (blockHeight <= lastBlockRead) {
        Logger.debug("[wallet] [%s, %s , %s , %s] no new blocks".format(currency, nodeId, blockHeight, lastBlockRead))
        return
      }

      try {
        Logger.info("[wallet] [%s, %s] Begin processing transactions up to block %s".format(currency, nodeId, blockHeight))
        // Retrieve transactions up to the minimum number of confirmations required
        val lastBlockHash = getBlockHash(lastBlockRead - minConfirmations)
        Logger.info(">>>>>>>>>>>>>>. last block hash is " + lastBlockHash + " >>>>>>>>>>>>>>")
        val list = listSinceBlock(lastBlockHash)
        val transactions = list.get("transactions")
        val transactionIterator = transactions.elements()
        val sinceBlockConfirmedDeposits: mutable.Set[Deposit] = mutable.Set.empty
        var withdrawalConfirmations: Int = Int.MaxValue
        var withdrawalTxHash: String = ""
        var withdrawalTxFee: BigDecimal = 0
        var withdrawalTimeReceived: Int = lastWithdrawalTimeReceived
        val recoverWithdrawalTxData: Option[mutable.Map[String, BigDecimal]] = if (firstUpdate /*&& stalledWithdrawalTx.isDefined*/ ) {
          Some(mutable.Map.empty)
        } else {
          None
        }

        while (transactionIterator.hasNext) {
          try {
            val transaction = transactionIterator.next()
            val category: String = transaction.get("category").textValue()
            val confirmations: Int = transaction.get("confirmations").asInt()
            val address: String = transaction.get("address").textValue()
            val amount: BigDecimal = transaction.get("amount").decimalValue()
            val txid: String = transaction.get("txid").textValue()

            // Logger.debug("[wallet] [%s, %s] processing transaction %s of type %s for %s with %s confirmations for address %s".format(currency, nodeId, txid, category, amount, confirmations, address))

            /*if (category == "send") {
            Logger.info(">>>>>>inside category == send>>>>>>>>>>>>>>>>>>" + txid + " recoverWithdrawalTxData is " + recoverWithdrawalTxData + " sentWithdrawalTx is " + sentWithdrawalTx)
            if (sentWithdrawalTx.length > 0 || recoverWithdrawalTxData.isDefined) {
              val timeReceived: Int = transaction.get("timereceived").asInt()
              if (confirmations < withdrawalConfirmations && confirmations >= 0 && timeReceived > withdrawalTimeReceived) {
                withdrawalConfirmations = confirmations
                withdrawalTxHash = txid
                // Withdrawal fees are negative so take the absolute value
                withdrawalTxFee = transaction.get("fee").decimalValue().abs()
                withdrawalTimeReceived = timeReceived
                if (recoverWithdrawalTxData.isDefined) {
                  recoverWithdrawalTxData.get.clear()
                }
              }
              if (recoverWithdrawalTxData.isDefined && txid == withdrawalTxHash) {
                // Withdrawal amounts are negative so take the absolute value
                recoverWithdrawalTxData.get.put(address, amount.abs)
              }
            }
          } else */ if (category == "receive" && confirmations > 0) {
              Logger.info(">>>>>>>>>>>>>>>category == receive>>>>>>>>>>>>>" + txid)
              val deposit = Deposit(address, amount, txid)

              if (confirmations < minDepositConfirmations) {

                Logger.info("<<<<<<<<<<<<<confirmations < minDepositConfirmations<<<<<<<<<<")
                // Unconfirmed deposit with at least 1 confirmation
                if (!pendingDeposits.contains(deposit)) {
                  Logger.info(">>>>>>>>>>>>>>>>>>>>> pendingDeposits currency " + currency + " >>> nodeId >> " + nodeId + " >>> " + deposit.address + " >> " + deposit.amount.bigDecimal + " >> " + deposit.txHash);
                  try {
                    val id = walletModel.createDeposit(currency, nodeId, deposit)
                    pendingDeposits.put(deposit, id)
                  } catch {
                    case ex: org.postgresql.util.PSQLException =>
                      ex.printStackTrace();
                  }
                }
              } else {
                // Confirmed deposit
                if (confirmedDeposits.add(deposit)) {
                  pendingDeposits.remove(deposit) match {
                    case Some(id) =>
                      Logger.info(">>>>>>>>>>>>>>>>>>>>> confirmedDeposit address " + deposit.address + " >>>> hash is >>>> " + deposit.txHash);
                      walletModel.confirmedDeposit(deposit, id, nodeId)
                    case _ =>
                      // If confirmed deposits cache has not been initialized, check if the deposit is in the DB
                      if (!firstUpdate || (firstUpdate && !walletModel.isConfirmedDeposit(deposit))) {
                        walletModel.createConfirmedDeposit(currency, nodeId, deposit)
                      }
                  }
                }
                sinceBlockConfirmedDeposits.add(deposit)
              }
            }
          } catch {
            case ex: Throwable =>
              ex.printStackTrace();
          }
        }

        //end of while
        // Remove confirmed deposits that are no longer returned
        confirmedDeposits = confirmedDeposits & sinceBlockConfirmedDeposits
        Logger.info(currency + ">>>>>>>>withdrawalConfirmations is " + withdrawalConfirmations + " >= minWithdrawalConfirmations>>>>>>>>>> " + minWithdrawalConfirmations)
        if (withdrawalConfirmations >= minWithdrawalConfirmations) {
          Logger.info(currency + ">>>>>>>>withdrawalConfirmations >= minWithdrawalConfirmations>>>>>>>>>>")
          lastWithdrawalBlock = withdrawalConfirmations;
          var unprocessedWithdrawalTxList = walletModel.getUnprocessedWithdrawalTx(currency);
          Logger.info(currency + "unprocessedWithdrawalTxList is " + unprocessedWithdrawalTxList);
          for (unprocessTransaction <- unprocessedWithdrawalTxList) {
            Logger.info(currency + "Found unprocess transaction for " + currency + " trascation is " + unprocessTransaction);
            var wId = walletModel.createWithdrawalTx(currency, nodeId, unprocessTransaction.id);
            Logger.info(currency + "Create Id is " + wId);
            wId match {
              case Some(withdrawalId) =>
                val rawHash = sendMany(unprocessTransaction.senderAddress, unprocessTransaction.receiverAddress, unprocessTransaction.amount);
                Logger.info(currency + "Raw hash code is " + rawHash);
                if (rawHash.isDefined) {
                  Logger.info(currency + "Raw hash code is " + rawHash.get);
                  walletModel.sentWithdrawalTx(withdrawalId, rawHash.get, unprocessTransaction.amount);
                }
            }

          }

        }
        for (sentWithdrawalObject <- sentWithdrawalTx) {
          sentWithdrawalObject match {
            case Some((id, txHash)) =>
              var txHashData = gettransaction(txHash);
              Logger.info(currency + "txHash is >>>>>>>>>>>>>>>>>>> " + txHashData);
              var confirm = txHashData.get("confirmations").asInt();
              var feeOfTx = txHashData.get("fee").decimalValue().abs();
              if (confirm >= minWithdrawalConfirmations) {
                walletModel.confirmedWithdrawalTx(id, feeOfTx)
              }

          }
        }

        var getStalledWithdrawalTxList = walletModel.getStalledWithdrawalTx(currency);
        Logger.info(currency + "getStalledWithdrawalTxList is >>>>  " + getStalledWithdrawalTxList);
        for (stalledWithdrawalTxObject <- getStalledWithdrawalTxList) {
          Logger.info(currency + "Found unprocess transaction for " + currency + " trascation is " + stalledWithdrawalTxObject);
          val rawHash = sendMany(stalledWithdrawalTxObject.senderAddress, stalledWithdrawalTxObject.receiverAddress, stalledWithdrawalTxObject.amount);
          Logger.info(currency + "Raw hash code is " + rawHash);
          if (rawHash.isDefined) {
            Logger.info(currency + "Raw hash code is " + rawHash.get);
            walletModel.sentWithdrawalTx(stalledWithdrawalTxObject.id, rawHash.get, stalledWithdrawalTxObject.amount);
          }

        }

        // Recover if there is an unconfirmed withdrawal that may or may not have been sent
        /*if (recoverWithdrawalTxData.isDefined) {
          Logger.info(">>>>>>>>recoverWithdrawalTxData.isDefined>>>>>>>>>> and sentWithdrawalTx is " + sentWithdrawalTx + "  stalledWithdrawalTx " + stalledWithdrawalTx)
          Logger.info("withdrawalTxHash is " + withdrawalTxHash + "  withdrawalTimeReceived " + withdrawalTimeReceived + "  lastWithdrawalTimeReceived " + lastWithdrawalTimeReceived);
          sentWithdrawalTx match {
            case Some((id, txHash)) if txHash == withdrawalTxHash && withdrawalTimeReceived >= lastWithdrawalTimeReceived =>
              // The most recent withdrawal was previously confirmed so the stalled withdrawal was never sent
              Logger.info("sentWithdrawalTx case with value is id %s , txHash is %s".format(id, txHash))
              sentWithdrawalTx = None
            case _ =>
              stalledWithdrawalTx match {
                case Some((withdrawalId, withdrawalsTotal, withdrawals)) if withdrawalTxHash != "" && withdrawals == recoverWithdrawalTxData.get =>
                  // The most recent withdrawal was the last unconfirmed withdrawal so update its status as sent
                  Logger.info(">>>>>>>>>>>>>> withdrawalId " + withdrawalId + " >>>>>> withdrawalTxHash " + withdrawalTxHash + " >> withdrawalsTotal " + withdrawalsTotal + " >>>>> withdrawals >>> " + withdrawals)
                  walletModel.sentWithdrawalTx(withdrawalId, withdrawalTxHash, withdrawalsTotal)
                  sentWithdrawalTx = Some(withdrawalId, withdrawalTxHash)
                case _ =>
                  Logger.warn("[wallet] [%s, %s] Unexpected most recent withdrawal with tx hash %s".format(currency, nodeId, withdrawalTxHash))
                  sentWithdrawalTx = None
              }
              // The most recent withdrawal was not the last to confirm so do not resend the last unconfirmed withdrawal
              stalledWithdrawalTx = None
          }
          Logger.info("After sentWithdrawalTx match in recoverWithdrawalTxData is " + sentWithdrawalTx);
        }
        // If last batch withdrawal is confirmed, send the next batch
        if (withdrawalConfirmations >= minWithdrawalConfirmations) {
          Logger.info(">>>>>>>>withdrawalConfirmations >= minWithdrawalConfirmations>>>>>>>>>>")
          lastWithdrawalBlock = withdrawalConfirmations
          sentWithdrawalTx match {
            case Some((id, txHash)) if withdrawalTxHash != "" =>
              if (txHash != withdrawalTxHash)
                walletModel.setWithdrawalTxHashMutated(id, withdrawalTxHash)
              walletModel.confirmedWithdrawalTx(id, withdrawalTxFee)
              lastWithdrawalTimeReceived = withdrawalTimeReceived
            case _ =>
          }
          balance = walletModel.getBalance(currency, nodeId)
          sentWithdrawalTx = if (balance > balanceMin) {
            Logger.info("After compare balance stalledWithdrawalTx is " + stalledWithdrawalTx);
            stalledWithdrawalTx match {
              case Some((withdrawalId, withdrawalsTotal, withdrawals)) if balance >= withdrawalsTotal + params.maxTxFee =>
                /*  var user_id = walletModel.getUserIdForRawTransaction(withdrawalId)
                val fromUserDetails = globals.engineModel.get_addresses_by_currency(user_id, currency.toString())
                val fromAddress = fromUserDetails.apply(0)._1*/
                Logger.info("balance compare sendMany invoked");
                val withdrawalTxHash = sendMany(withdrawals.asJava)
                // Logger.info(">>>>>>>>>>>>>. withdrawalTxHash is " + withdrawalTxHash + " >>>>>>>" + fromAddress + " >>>>>>>>>>>>>>>>>>>>>>>>>");
                Logger.info(">>>>>>>>>>>>>> withdrawalId " + withdrawalId + " >>>>>> withdrawalTxHash " + withdrawalTxHash + " >> withdrawalsTotal " + withdrawalsTotal + " >>>> withdrawals >> " + withdrawals)
                walletModel.sentWithdrawalTx(withdrawalId, withdrawalTxHash, withdrawalsTotal)
                Logger.info(">>>>>>>>>>>>>. sentWithdrawalTx data is withdrawalId:" + withdrawalId + " withdrawalTxHash : " + withdrawalTxHash + " withdrawalsTotal is " + withdrawalsTotal + ">>>>>>>>>>>>>>>>>>>>>>>>>");
                changeAddressCount += 1
                stalledWithdrawalTx = None
                Some(withdrawalId -> withdrawalTxHash)
              case Some((withdrawalId, withdrawalsTotal, withdrawals)) =>
                None
              case _ =>
                var wId = walletModel.createWithdrawalTx(currency, nodeId);
                Logger.info("Withdrawal Id at creation time is " + wId)
                wId match {
                  case Some(withdrawalId) =>
                    val withdrawals = walletModel.getWithdrawalTxData(withdrawalId)
                    Logger.info("WithdrawalId withdrawals is >>> at default statement is " + withdrawals)
                    val withdrawalsTotal = withdrawals.view.map(_._2).sum
                    val balanceLowerBound = balance - withdrawalsTotal - params.maxTxFee
                    Logger.info("withdrawalsTotal is " + withdrawalsTotal + " and balanceLowerBound is " + balanceLowerBound)
                    if (balanceLowerBound < 0) {
                      stalledWithdrawalTx = Some(withdrawalId, withdrawalsTotal, withdrawals)
                      None
                    } else {
                      val withdrawalsJava = (
                        if (balanceLowerBound > balanceMax && params.coldAddress.isDefined) {
                          // Transfer to cold coldStorageValue
                          val coldStorageValue = balanceLowerBound - balanceTarget
                          walletModel.createColdStorageTransfer(withdrawalId, params.coldAddress.get, coldStorageValue)
                          withdrawals + (params.coldAddress.get -> coldStorageValue)
                        } else {
                          withdrawals
                        }).asJava
                      /*var user_id = walletModel.getUserIdForRawTransaction(withdrawalId)
                      val fromUserDetails = globals.engineModel.get_addresses_by_currency(user_id, currency.toString())
                      val fromAddress = fromUserDetails.apply(0)._1
                      Logger.info(">>>>>>>>>>>>>>>>>>> Cold storage address of user is " + fromAddress + ">>> send many withdrawalsJava is " + withdrawalsJava);
                      */
                      Logger.info("withdrawalsJava is " + withdrawalsJava + " and sendMany invoked")
                      val withdrawalTxHash = sendMany(withdrawalsJava)

                      Logger.info(" >>>>>>>>>>>>>>>>. Cold storage wallet is withdrawalTxHash >>>>>>>>>>>>>>.  " + withdrawalTxHash);
                      Logger.info(">>>>>>>>>>>>>> withdrawalId " + withdrawalId + " >>>>>> withdrawalTxHash " + withdrawalTxHash + " >> withdrawalsTotal " + withdrawalsTotal)
                      walletModel.sentWithdrawalTx(withdrawalId, withdrawalTxHash, withdrawalsTotal)
                      changeAddressCount += 1
                      Some(withdrawalId -> withdrawalTxHash)
                    }
                  case _ =>
                    None
                }
            }
            //Logger.info("After excution complete sentWithdrawalTx is "+sentWithdrawalTx);
          } else {
            None
          }
        }*/

        balance = walletModel.getBalance(currency, nodeId)

        // Notify that the wallet needs refilling
        Logger.info("Balance is " + balance + " balanceWarn is " + balanceWarn);
        if (balance <= balanceWarn && params.refillEmail.isDefined) {
          if (!refillRequested) {
            refillRequested = true
            try {
              // XXX: temporary hack to make Messages work in emails (only english for now)
              implicit val messages = new Messages(new Lang("en", "US"), new DefaultMessagesApi(play.api.Environment.simple(new File("."), Mode.Prod),
                play.api.Play.current.configuration,
                new DefaultLangs(play.api.Play.current.configuration))
              )

              securesocial.core.providers.utils.Mailer.sendRefillWalletEmail(params.refillEmail.get, currency.toString, nodeId, balance, balanceTarget)
            } catch {
              case ex: Throwable =>
                // If email cannot be sent, log an error
                Logger.error("[wallet] [%s, %s] Error sending wallet refill email".format(currency, nodeId))
            }
          }
        } else {
          refillRequested = false
        }

        // Subtract minConfirmations as it could be decreased when actor is restarted
        // Make sure we have a withdrawal to check against for crash recovery
        walletModel.setLastBlockRead(currency, nodeId, math.min(blockHeight, lastWithdrawalBlock), lastWithdrawalTimeReceived)
        lastBlockRead = blockHeight
        firstUpdate = false
        Logger.info("[wallet] [%s, %s] Finished processing transactions up to block %s".format(currency, nodeId, blockHeight))
      } catch {
        case ex: Throwable =>
          Logger.error("[wallet] [%s, %s] error processing transactions: %s".format(currency, nodeId, ex))
          ex.printStackTrace()
          // Reset state
          initMemberVars()
      }
    }
  }

  def generateAddresses(): Unit = {
    val freeAddressCount = try {
      walletModel.getFreeAddressCount(currency, nodeId).toInt
    } catch {
      case _: Throwable =>
        Logger.error("[wallet] [%s, %s] cannot get free address count".format(currency, nodeId))
        return
    }
    if (freeAddressCount < params.addressPool / 2 || changeAddressCount > params.addressPool / 2) {
      val generateAddressCount = params.addressPool - freeAddressCount
      try {
        if (currency.toString() == "ETH") {
          Logger.info(">>>>>>>>>>>>>>>>>>>> inside ETH")
          val etherList = List.range(0, generateAddressCount).map(i => etherAddress)
          val a = generateAddressCount
          Logger.info(">>>>>>>>>>>>>>>>>>>>ether object is " + etherList)
          walletModel.addNewAddressBatchForEther(currency, nodeId, etherList)
        } else {
          Logger.info(">>>>>>>>>>>>>>>>>>>> inside other than ETH")
          val addresses = List.range(0, generateAddressCount).map(i => getNewAddress)
          walletModel.addNewAddressBatch(currency, nodeId, addresses)
        }

      } catch {
        case _: Throwable =>
          Logger.error("[wallet] [%s, %s] cannot get new addresses from RPC".format(currency, nodeId))
          return
      }
      // Refill the key pool before backing up the wallet
      try {
        keyPoolRefill()
      } catch {
        case _: Throwable =>
          Logger.error("[wallet] [%s, %s] cannot refill keypool from RPC".format(currency, nodeId))
          return
      }
      // back up the wallet only after we've generated new keys
      if (params.backupPath.isDefined) {
        try {
          backupWallet(params.backupPath.get)
          Logger.info("[wallet] [%s, %s] Backed up wallet to %s".format(currency, nodeId, params.backupPath.get))
        } catch {
          case _: Throwable =>
            Logger.error("[wallet] [%s, %s] cannot backup wallet from RPC".format(currency, nodeId))
            return
        }
      }
      changeAddressCount = 0
    }
  }

  private def backupWallet(destination: String) = {
    rpc.invoke("backupwallet", Array(destination), classOf[Any])
  }

  private def getBalance = {
    // This method is unused as balance is now tracked in the database
    rpc.invoke("getbalance", null, classOf[BigDecimal])
  }

  private def getBlockCount = {
    rpc.invoke("getblockcount", null, classOf[Int])
  }

  private def getBlockHash(index: Int) = {
    rpc.invoke("getblockhash", Array[Any](index), classOf[String])
  }

  private def getNewAddress = {
    rpc.invoke("getnewaddress", Array(), classOf[String])
  }

  private def keyPoolRefill() = {
    rpc.invoke("keypoolrefill", null, classOf[Any])
  }

  private def listSinceBlock(blockHash: String) = {
    rpc.invoke("listsinceblock", Array(blockHash), classOf[ObjectNode])
  }

  private def gettransaction(blockHash: String) = {
    rpc.invoke("gettransaction", Array(blockHash), classOf[ObjectNode])
  }

  private def sendMany(senderAddress: String, receiverAddress: String, sendAmount: BigDecimal): Option[String] = {
    Logger.info(">>>>>>>>>>>>> sendMany senderAddress is " + senderAddress + " receiverAddress  " + receiverAddress + "    >> >>>>>>>>>>>>>>>>>>>>>>>>>" + currency.toString())
    try {
      val unspentList = rpc.invoke("listunspent", Array(3, 9999, Array(senderAddress)), classOf[Array[ObjectNode]])
      var senderJsonNodeArray = Array[ObjectNode]();
      var sumOfAmount: BigDecimal = 0;
      if (unspentList.length > 0) {
        for (objectNode <- unspentList) {
          Logger.info("Inside send form loop >>>>>>>>>>>>> ");

          val amount: BigDecimal = objectNode.get("amount").decimalValue()
          Logger.info("amount is " + amount + " sendAmount " + sendAmount);
          if (sendAmount > sumOfAmount) {
            sumOfAmount = sumOfAmount + amount;
            val senderJsonNode = JsonNodeFactory.instance.objectNode();
            senderJsonNode.put("txid", objectNode.get("txid").textValue());
            senderJsonNode.put("vout", objectNode.get("vout").asInt());
            senderJsonNodeArray :+= senderJsonNode;
          }
        }
        val fee = sendAmount / 1000
        val receiveerJsonNode = JsonNodeFactory.instance.objectNode();
        receiveerJsonNode.put(receiverAddress, sendAmount.doubleValue());
        receiveerJsonNode.put(senderAddress, ((sumOfAmount - sendAmount) - fee).doubleValue());
        Logger.info("receiveerJsonNode is  >>>>>>>>> " + receiveerJsonNode + " currency is " + currency);
        val hash = rpc.invoke("createrawtransaction", Array(senderJsonNodeArray, receiveerJsonNode), classOf[String])
        Logger.info(">>>>>>>>>>>>. Raw hash is " + hash + " >>>>>>>>>>>>>>>>>>>>>>>>")
        val signHash = rpc.invoke("signrawtransaction", Array(hash), classOf[ObjectNode])
        Logger.info(">>>>>>>>>>>>. Raw signhash is " + signHash + " >>>>>>>>>>>>>>>>>>>>>>>>")
        val hashOfRawTransaction = rpc.invoke("sendrawtransaction", Array(signHash.get("hex").textValue()), classOf[String])
        Logger.info(" >>>>>>>>>>>>>>>>>>>>>>>>>. Raw Transaction is ID  " + hashOfRawTransaction + " >>>>>>>>>>>>>>>>>>>>>>>>");
        Some(hashOfRawTransaction)
      } else {
        None
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        None
    }
    /* Logger.info("Return is sendMany >>>>>>>>>>>>> " + rawHash + " curr" + currency.toString());*/
    // rpc.invoke("sendmany", Array("", withdrawals), classOf[String])
  }

  private def etherAddress() = {
    val ecKeyPair = Keys.createEcKeyPair()
    val publicKey = ecKeyPair.getPublicKey().toString(16)
    val privateKey = ecKeyPair.getPrivateKey().toString(16);
    val address = Keys.getAddress(ecKeyPair)
    var ether = new models.EtherCrypto(address, privateKey, publicKey)
    ether
  }

  def receive = {
    case _ =>
  }

}

object Wallet {
  def props(rpc: JsonRpcHttpClient, currency: CryptoCurrency, nodeId: Int, params: WalletParams, walletModel: WalletModel, test: Boolean = false) = {
    Props(classOf[Wallet], rpc, currency, nodeId, params, walletModel, test)
  }
  object CryptoCurrency extends Enumeration {
    type CryptoCurrency = Value
    val BTC = Value(0)
    val LTC = Value(1)
    val PPC = Value(2)
    val XPM = Value(3)
    val ETH = Value(4)
    val BCH = Value(5)
    val DASH = Value(6)
    val ZEC = Value(7)
  }
  case class WalletParams(
    checkDelay: FiniteDuration,
    checkInterval: FiniteDuration,
    addressDelay: FiniteDuration,
    addressInterval: FiniteDuration,
    addressPool: Int,
    backupPath: Option[String],
    coldAddress: Option[String],
    refillEmail: Option[String],
    maxTxFee: BigDecimal)
}