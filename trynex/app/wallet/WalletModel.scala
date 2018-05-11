

package wallet

import anorm._
import play.api.db.DB
import play.api.Play.current
import WalletModel._
import wallet.Wallet.CryptoCurrency.CryptoCurrency
import models.EtherCrypto

class WalletModel(val db: String = "default") {

  def obtainSessionLock(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select pg_try_advisory_lock(${currency.id}, $nodeId) """().map(row => row[Boolean]("pg_try_advisory_lock")).head
  }

  def getSenderAdress(amount: BigDecimal, currency: String, address: String) = DB.withConnection(db) { implicit c =>
    SQL""" select get_senderAddress($currency,$amount,$address) """().map(row => row[String]("get_senderAddress")).head
  }

  def addNewAddress(currency: CryptoCurrency, nodeId: Int, address: String) = DB.withConnection(db) { implicit c =>
    SQL"""
    insert into users_addresses (address, currency, node_id)
    values($address, ${currency.toString}, $nodeId)
    """.execute()
  }

  def addNewAddressBatch(currency: CryptoCurrency, nodeId: Int, addresses: List[String]) = DB.withConnection(db) { implicit c =>
    (SQL("""
      insert into users_addresses (address, currency, node_id)
      values({address}, {currency}, {node_id})"""
    ).asBatch /: addresses)(
      (sql, address) => sql.addBatchParams(address, currency.toString, nodeId)
    ).execute()
  }
  def addNewAddressBatchForEther(currency: CryptoCurrency, nodeId: Int, etherList: List[EtherCrypto]) = DB.withConnection(db) { implicit c =>
    (SQL("""
      insert into users_addresses (address, currency, node_id , private_key , public_key)
      values({ether.address}, {currency}, {node_id} , {ether.privateKey} , {ether.publicKey})"""
    ).asBatch /: etherList)(
      (sql, ether) => sql.addBatchParams(ether.address, currency.toString, nodeId, ether.privateKey, ether.publicKey)
    ).execute()
  }

  def getFreeAddressCount(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select free_address_count(${currency.toString}, $nodeId) """().map(row => row[Long]("free_address_count")).head
  }

  def getMinConfirmations(currency: CryptoCurrency) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_min_confirmations(${currency.toString}) """().map(row => (
      row[Boolean]("active"),
      row[Int]("min_deposit_confirmations"),
      row[Int]("min_withdrawal_confirmations")
    )).headOption.getOrElse(false, 0, 0)
  }

  def getNodeInfo(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_node_info(${currency.toString}, $nodeId) """().map(row => (
      row[Boolean]("retired"),
      row[BigDecimal]("balance_min"),
      row[BigDecimal]("balance_warn"),
      row[BigDecimal]("balance_target"),
      row[BigDecimal]("balance_max")
    )).headOption.getOrElse(false, BigDecimal("0"), BigDecimal("0"), BigDecimal("0"), BigDecimal("0"))
  }

  def getBalance(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select get_balance(${currency.toString}, $nodeId) """().map(row => row[BigDecimal]("get_balance")).headOption.getOrElse(BigDecimal("0"))
  }

  def getLastBlockRead(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_last_block_read(${currency.toString}, $nodeId) """().map(row => (
      row[Option[Int]]("last_block_read").getOrElse(0),
      row[Option[Int]]("last_withdrawal_time_received").getOrElse(0)
    )).headOption.getOrElse((0, 0))
  }

  def setLastBlockRead(currency: CryptoCurrency, nodeId: Int, blockCount: Int, lastWithdrawalTimeReceived: Int) = DB.withConnection(db) { implicit c =>
    SQL"""
      select set_last_block_read(${currency.toString}, $nodeId,
      $blockCount, $lastWithdrawalTimeReceived)
    """.execute()
  }

  def createDeposit(currency: CryptoCurrency, nodeId: Int, deposit: Deposit) = DB.withConnection(db) { implicit c =>
    SQL""" select create_deposit(
      ${currency.toString},
      $nodeId,
      ${deposit.address},
      ${deposit.amount.bigDecimal},
      ${deposit.txHash})
      """().map(row => row[Long]("create_deposit")).head
  }

  def createConfirmedDeposit(currency: CryptoCurrency, nodeId: Int, deposit: Deposit) = DB.withConnection(db) { implicit c =>
    SQL"""
      select create_confirmed_deposit(
      ${currency.toString},
      $nodeId,
      ${deposit.address},
      ${deposit.amount.bigDecimal},
      ${deposit.txHash})
    """.execute()
  }

  def isConfirmedDeposit(deposit: Deposit) = DB.withConnection(db) { implicit c =>
    SQL""" select is_confirmed_deposit(
      ${deposit.address},
      ${deposit.amount.bigDecimal},
      ${deposit.txHash})
      """().map(row => row[Boolean]("is_confirmed_deposit")).head
  }

  def getPendingDeposits(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_pending_deposits(${currency.toString}, $nodeId) """().map(row =>
      Deposit(row[String]("address"), row[BigDecimal]("amount"), row[String]("tx_hash")) -> row[Long]("id")
    ).toList
  }

  def confirmedDeposit(deposit: Deposit, id: Long, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select confirmed_deposit(
      $id,
      ${deposit.address},
      ${deposit.txHash},
      $nodeId) """.execute()
  }

  def getUnprocessedWithdrawalTx(currency: CryptoCurrency) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_unprocessed_withdrawal_tx(${currency.toString}) """().map(row =>
      UnprocessedWithdrawalTx(row[Long]("id"), row[String]("sender_address"), row[String]("rec_address"), row[BigDecimal]("amount"))
    ).toList
  }

  def getUnconfirmedWithdrawalTx(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_unconfirmed_withdrawal_tx(${currency.toString}, $nodeId) """().map(row => Option(row[Long]("id") -> row[String]("tx_hash"))).toList
  }

  def getLastConfirmedWithdrawalTx(currency: CryptoCurrency, nodeId: Int) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_last_confirmed_withdrawal_tx(${currency.toString}, $nodeId) """().map(row => row[Option[Long]]("confirmed_id") -> row[Option[String]]("confirmed_tx_hash")).head match {
      case (Some(id: Long), Some(txHash: String)) => Some(id, txHash)
      case _ => None
    }
  }

  def createWithdrawalTx(currency: CryptoCurrency, nodeId: Int, id: Long) = DB.withConnection(db) { implicit c =>
    SQL""" select create_withdrawal_tx(${currency.toString}, $nodeId,$id) """().map(row => row[Option[Long]]("create_withdrawal_tx")).head
  }

  def getWithdrawalTxData(txId: Long) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_withdrawal_tx_data($txId) """().map(row => row[String]("address") -> row[BigDecimal]("value")).toMap
  }

  def getStalledWithdrawalTx(currency: CryptoCurrency) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_stalled_withdrawal_tx(${currency.toString}) """().map(row =>
      UnprocessedWithdrawalTx(row[Long]("id"), row[String]("sender_address"), row[String]("rec_address"), row[BigDecimal]("amount"))
    ).toList
  }

  def sentWithdrawalTx(txId: Long, txHash: String, txAmount: BigDecimal) = DB.withConnection(db) { implicit c =>
    SQL""" select sent_withdrawal_tx($txId, $txHash, $txAmount) """.execute()
  }

  def confirmedWithdrawalTx(txId: Long, txFee: BigDecimal) = DB.withConnection(db) { implicit c =>
    SQL""" select confirmed_withdrawal_tx($txId, ${txFee.bigDecimal}) """.execute()
  }

  def createColdStorageTransfer(txId: Long, address: String, value: BigDecimal) = DB.withConnection(db) { implicit c =>
    SQL""" select create_cold_storage_transfer($txId, $address, ${value.bigDecimal}) """.execute()
  }

  def getColdStorageTransfer(txId: Long) = DB.withConnection(db) { implicit c =>
    SQL""" select * from get_cold_storage_transfer($txId) """().map(row => row[Option[String]]("address") -> row[Option[BigDecimal]]("value")).head match {
      case (Some(address: String), Some(value: BigDecimal)) => Some(address, value)
      case _ => None
    }
  }

  def setWithdrawalTxHashMutated(txId: Long, txHash: String) = DB.withConnection(db) { implicit c =>
    SQL""" select set_withdrawal_tx_hash_mutated($txId, $txHash) """.execute()
  }
  def getUserIdForRawTransaction(txId: Long) = DB.withConnection(db) { implicit c =>
    SQL"""
    select * from get_userid_for_rawtransaction($txId)
    """().map(row => (row[Long]("user_id"))
    ).head
  }

}

object WalletModel {
  case class Deposit(address: String, amount: BigDecimal, txHash: String)
  case class UnprocessedWithdrawalTx(id: Long, senderAddress: String, receiverAddress: String, amount: BigDecimal)
}

