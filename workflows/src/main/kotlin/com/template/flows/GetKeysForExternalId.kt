package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import com.r3.corda.lib.accounts.workflows.flows.RequestAccountInfoFlow
import com.r3.corda.lib.accounts.workflows.internal.accountService
//import com.r3.corda.lib.ci.RequestKeyForAccount
import com.template.states.AccountsIOUState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import org.bouncycastle.util.encoders.Base64Encoder
import java.security.KeyFactory
import java.util.*
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec



/*
   CQE-46 && CQE-49 - Flow to get Public Keys  of the  account from account name
 */
@StartableByRPC
class GetKeysForExternalId(val accountName:String) :
        FlowLogic<Iterable<PublicKey>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Iterable<PublicKey> {

        //get internal id of the account
        val accountState=subFlow(AccountInfoByName(accountName)).single()
        val externalId=accountState.state.data.identifier.id
        //get the list of keys  mapped to the id
        val publicKeyList = serviceHub.identityService.publicKeysForExternalId(externalId)
        return publicKeyList
    }
}
