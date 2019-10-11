package com.template.webserver

import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName
import com.r3.corda.lib.accounts.workflows.flows.AllAccounts
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
//import com.r3.corda.lib.accounts.workflows.externalIdCriteria
import com.template.flows.*

import org.json.simple.JSONObject

//import net.corda.testing.driver.NodeParameters
import java.util.*

/**
 * Define your API endpoints here.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }


    private val proxy = rpc.proxy
    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name



    @GetMapping(value = ["/templateendpoint"], produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }


    /*API to create accounts in nodes*/
    @PostMapping(value = ["createAccount"],produces = [ APPLICATION_JSON_VALUE ],
            headers = ["Content-Type=application/json"])
    fun createAccount( @RequestBody request: JSONObject): JSONObject {
        //Name of the account
        val name = request.get("accountName").toString()
        var Response: JSONObject = JSONObject()
        if (name == null) {
            Response.put("Response", "Failure")
            Response.put("Message", "Query parameter 'accountName' must not be null.\\n")
            return Response
        }

        return try {
            //Calling CreateBankAccount flow
            val signedTx = proxy.startTrackedFlow(::CreateAccount, name).returnValue.getOrThrow()
            Response.put("Response", "Success")
            Response.put("TxnId", signedTx)
            Response

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.put("Response", "Failure")
            Response.put("Message", ex.message)
            Response
        }
    }

    /*API to create an IOU between accounts in different nodes*/
    //CQE-47
    @PostMapping(value = ["createIOU"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun createIOU( @RequestBody request: JSONObject): JSONObject {
        //IOU value
        val iouValue = request.get("value").toString().toInt()
        //Account name of Lender account
        val lender = request.get("lender").toString()
        //Account name of Borrower account
        val borrower = request.get("borrower").toString()

        var Response: JSONObject = JSONObject()
        if(iouValue == null||borrower == null) {
            Response.put("Response", "Failure")
            Response.put("Message", "Query parameter 'value' and 'borrower' must not be null.\\n")
            return Response
        }

        return try {
            //Calling IOUFlow for accounts in different nodes
            val signedTx = proxy.startFlowDynamic(IOUFlow.Initiator::class.java, iouValue, lender, borrower).returnValue.getOrThrow()
            Response.put("Response", "Success")
            Response.put("TxnId", signedTx)
            Response

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.put("Response", "Failure")
            Response.put("Message", ex.message)
            Response
        }
    }

    /*API to create an IOU between two accounts within same node*/
    //CQE-47
    @PostMapping(value = ["createIOUSameHost"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun createIOUSameHost( @RequestBody request: JSONObject): JSONObject {
        //IOU value
        val iouValue = request.get("value").toString().toInt()
        //Account name of Lender account
        val lender = request.get("lender").toString()
        //Account name of Borrower account
        val borrower = request.get("borrower").toString()

        var Response: JSONObject = JSONObject()
        if(iouValue == null||borrower == null) {
            Response.put("Response", "Failure")
            Response.put("Message", "Query parameter 'value' and 'borrower' must not be null.\\n")
            return Response
        }

        return try {
            //Calling IOUFlow for accounts within same node
            val signedTx = proxy.startFlowDynamic(IOUFlowSameHost.Initiator::class.java, iouValue, lender, borrower).returnValue.getOrThrow()
            Response.put("Response", "Success")
            Response.put("TxnId", signedTx)
            Response

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.put("Response", "Failure")
            Response.put("Message", ex.message)
            Response
        }
    }
    //CQE-47
    @PostMapping(value = ["acceptIOU"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun acceptIOU( @RequestBody request: JSONObject): JSONObject {
        val lenderHostName = request.get("lenderHostName").toString()
        val borrowerAccountName = request.get("borrowerAccountName").toString()
        var Response: JSONObject = JSONObject()
        return try {
            val signedTx = proxy.startFlowDynamic(AcceptIOUFlow.Initiator::class.java, lenderHostName, borrowerAccountName).returnValue.getOrThrow()
            Response.put("Response", "Success")
            Response.put("TxnId", signedTx)
            Response

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.put("Response", "Failure")
            Response.put("Message", ex.message)
            Response
        }
    }

    //API to ShareAccount between different nodes
    @PostMapping(value = ["ShareAccountInfo"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun ShareAccountInfoFlow( @RequestBody request: JSONObject): JSONObject {

        // Giving the accountName
        val accountName = request.get("accountName").toString()
        // Giving the partyName
        val toHost = request.get("toHost").toString()
        var Response: JSONObject = JSONObject()
        if(accountName == null||toHost == null) {
            Response.put("Response", "Failure")
            Response.put("Message", "Query parameter 'accountName' and 'toHost' must not be null.\\n")
            return Response
        }
        val account = proxy.startFlowDynamic(AccountInfoByName::class.java, accountName).returnValue.getOrThrow().single()
        val party = proxy.partiesFromName("PartyA", true).single()
        println("partyName: "+party)

        return try {
            // ShareAccountFlow is called
            val signedTx = proxy.startFlowDynamic(ShareAccountInfo::class.java, account, listOf(party)).returnValue.getOrThrow()
            Response.put("Response", "Success")
            Response.put("TxnId", signedTx)
            Response

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.put("Response", "Failure")
            Response.put("Message", ex.message)
            Response
        }
    }

    //API for getting AllAccounts in a node
    @GetMapping(value = ["AllAccounts"],produces = [MediaType.APPLICATION_JSON_VALUE])
    fun allAccounts() {
        //AllAccountsFlow is called
        val accounts = proxy.startFlowDynamic(AllAccounts::class.java).returnValue.getOrThrow()

        for (account in accounts) {
            println("accountName: "+account.state.data.name)
        }
    }

    /*
    API to get all the states created by a particular account, by passing the account name as parameter
 */
    //Refer001
    @PostMapping(value = ["getIOUStates"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun GetIOUStates( @RequestBody request: JSONObject): ArrayList<JSONObject> {
        val accountName = request.get("lenderAccountName").toString()
        val JSONArrayRequests = ArrayList<JSONObject>()
        return try {
            var requestListData = JSONObject()
            // Get the account from AccountInfoByName by passing account name
            val accountList = proxy.startFlowDynamic(AccountInfoByName::class.java, accountName).returnValue.getOrThrow()
            val account = accountList.single()
            // Get the UUID of account
            val lenderUUID = account.state.data.identifier.id
            // Get all the AccountsIOUStates
            val accountsIOUStatesList = proxy.startFlowDynamic(GetIOUFlow::class.java, lenderUUID).returnValue.getOrThrow()
            for (i in accountsIOUStatesList.indices) {
                var rootObject = JSONObject()
                rootObject.put("iouValue", accountsIOUStatesList[i].state.data.value)
                rootObject.put("lender", accountsIOUStatesList[i].state.data.lender)
                rootObject.put("borrower", accountsIOUStatesList[i].state.data.borrower)
                rootObject.put("status", accountsIOUStatesList[i].state.data.status)
                requestListData = rootObject
                JSONArrayRequests.add(requestListData)
            }
            // Return the final list
            return JSONArrayRequests
        } catch (ex: Throwable) {
            var failureResponse: JSONObject = JSONObject()
            failureResponse.put("result", "Failure")
            failureResponse.put("response", "Error")
            JSONArrayRequests.add(failureResponse)
            return JSONArrayRequests
        }
    }

    /*
 API to get all the states created by a list of accounts under a particular host, by passing the list of account names as parameter
  */
    //Refer001
    @PostMapping(value = ["getIOUStatesOfAccounts"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun GetIOUStatesOfAccounts( @RequestBody request: JSONObject): ArrayList<JSONObject>{
        // Get the list
        val accountIdList = request.get("accountNameList") as List<String>
        val list = ArrayList<UUID>()
        for(item in accountIdList)  {
            // Get the AccountInfo of account by passing name
            val accountInfo = proxy.startFlowDynamic(AccountInfoByName::class.java, item).returnValue.getOrThrow().single()
            // Get the UUID of account
            val accountUUID = accountInfo.state.data.identifier.id
            // Add UUID to list
            list.add(accountUUID)
        }
        // Assign final list to accountsIDList which is List<UUID>
        val accountsIDList: List<UUID> = list
        val JSONArrayRequests = ArrayList<JSONObject>()
        return try {
            var requestListData = JSONObject()
            // Get all the AccountsIOUStates by passing accountsIDList
            val accountsIOUStatesList = proxy.startFlowDynamic(GetIOUOfAccountsFlow::class.java, accountsIDList).returnValue.getOrThrow()
            for (i in accountsIOUStatesList.indices) {
                var rootObject = JSONObject()
                rootObject.put("iouValue", accountsIOUStatesList[i].state.data.value)
                rootObject.put("lender", accountsIOUStatesList[i].state.data.lender)
                rootObject.put("borrower", accountsIOUStatesList[i].state.data.borrower)
                rootObject.put("status", accountsIOUStatesList[i].state.data.status)
                requestListData = rootObject
                JSONArrayRequests.add(requestListData)
            }
            // Return the final list
            return JSONArrayRequests
        } catch (ex: Throwable) {
            var failureResponse: JSONObject = JSONObject()
            failureResponse.put("result", "Failure")
            failureResponse.put("response", "Error")
            JSONArrayRequests.add(failureResponse)
            return JSONArrayRequests
        }
    }

    //Identity  service
    //CQE-46
    @PostMapping(value = ["getAccountFromKey"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun getAccountFromKey( @RequestBody request: JSONObject): JSONObject {
        val key = request.get("key").toString()
        val JSONArrayRequests = ArrayList<JSONObject>()
        return try {

            // Get the account from GetAccount by passing account name
            val accountResponse = proxy.startFlowDynamic(GetAccountFromKey::class.java, key).returnValue.getOrThrow()




            var rootObject = JSONObject()
            rootObject.put("name",accountResponse.state.data.name)
            rootObject.put("id", accountResponse.state.data.identifier.id)
            rootObject.put("host", accountResponse.state.data.host)



            // Return the final list
            return rootObject
        } catch (ex: Throwable) {
            var failureResponse: JSONObject = JSONObject()
            failureResponse.put("result", "Failure")
            failureResponse.put("response", ex)

            return failureResponse
        }
    }

    //identity service flow endpoints
    //CQE-46 && CQE-49
    @PostMapping(value = ["getKeysFromExternalId"],produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/json"])
    fun getKeysForAccount( @RequestBody request: JSONObject): ArrayList<JSONObject> {
        val accountName = request.get("accountName").toString()
        var   JsonArrayRequests=ArrayList<JSONObject>()
        return try {


            val keyResponse = proxy.startFlowDynamic(GetKeysForExternalId::class.java, accountName).returnValue.getOrThrow()

            for(i in keyResponse){
                var rootObject=JSONObject()
                rootObject.put("key",i)
                JsonArrayRequests.add(rootObject)

            }

            // Return the final list
            return JsonArrayRequests
        } catch (ex: Throwable) {
            var failureResponse: JSONObject = JSONObject()
            failureResponse.put("result", "Failure")
            failureResponse.put("response", ex)
            JsonArrayRequests.add(failureResponse)
            return JsonArrayRequests
        }
    }



}