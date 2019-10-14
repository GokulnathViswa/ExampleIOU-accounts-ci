# accounts-iou
* This is project to create IOU between two accounts.
* Run the command in the project directory './gradlew clean deployNodes'
* Start all the node.
* Start the client webserver
* Create an account in PartyA and PartyB using the api 'createAccount' in the Controller.kt
        Input: {"accountName": "alice"} from PartyA
        Input: {"accountName": "bob"} from PartyB
* Share PartyB's account to PartyA using the api 'ShareAccountInfo' in the Controller.kt
        Input: {"accountName": "bob",
                "toHost": "PartyA"}  from PartyB
* Create IOU from PartyA-"alice" as a lender using the api 'createIOU' in the Controller.kt
        Input: {"value": "100",
                "lender": "alice",
                "borrower": "bob"}  from PartyA
* Check the AccountsIOUState data using the api 'getIOUStates' in the Controller.kt
        Input: {"lenderAccountName": "alice"} from PartyA
        Input: {"lenderAccountName": "bob"} from PartyB

      Note: You can get bob's iou state from PartyA too as bob is shared to PartyA.

* To get AccountsIOUState of multiple accounts of a host, try creating multiple IOU's between
  accounts and check the api 'getIOUStatesOfAccounts' in the Controller.kt.
          Input: {"accountNameList": ["$account1","$account2"]} from PartyA

* Once the IOU is created, accept the iou from the borrower-"bob" Node using the api
  'acceptIOU' in the Controller.kt
          Input: {"lenderHostName": "PartyA",
                  "borrowerAccountName": "bob"} from PartyB

* To get ExternalId from the accounts's publicKey, check the api 'getExternalIdFromKey'
  in the Controller.kt.
         Input: {"key": "$publicKey"} from PartyA

* To get public keys from the accounts's external uuid, check the api 'getKeysFromExternalId'
   in the Controller.kt.
          Input: {"accountName": "$accountName"} from PartyA
          Note: AccountInfo state will be retrieved from accountName and externalid will be used from
                the state to get keys.

Sprint7 Tasks

* Rebase CIs and accounts to use new IdentityService API
https://r3-cev.atlassian.net/browse/CQE-46
SEARCHCODE: 'CQE-46'

* Rebase accounts to use new CI flows
https://r3-cev.atlassian.net/browse/CQE-47
SEARCHCODE: 'CQE-47'

* There is no easy way to lookup a public key to an account without depending on `node`
https://r3-cev.atlassian.net/browse/CQE-49
SEARCHCODE: 'CQE-49'

* Query the state by an account without depending on node.
SEARCHCODE: 'Refer001'


Note: Search the 'SEARCHCODE' in the project(ctrl+Shift+f) to refer the place of the ticket tested.

