package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import com.template.states.AccountsIOUState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.parsePublicKeyBase58
import org.bouncycastle.util.encoders.Base64Encoder
import java.security.KeyFactory
import java.util.*
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import com.r3.corda.lib.accounts.workflows.accountService
import java.util.UUID



/**
 * CQE-46 - Flow to get  Account from public key
 * @param key is the public key as a String


 **/
@StartableByRPC
class GetExternalIdFromKey(val key:String) :
        FlowLogic<UUID>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): UUID {

        //parse the public key to convert string to PublicKey
        val pubKey= parsePublicKeyBase58(key)
        //get the  account mapped to the key
        var id = accountService.accountIdForKey(pubKey)
        return id?:FlowException("No externalId found to the key '$pubKey'")

    }
}
