# Pocket Android Ethereum Plugin
Ethereum Android Plugin to connect to any Ethereum compatible Pocket Node. 
For more information about Pocket Node you can checkout the repo [here](https://github.com/pokt-network/pocket-node).

# Installation
This project is hosted in Github and you can install it using [Jitpack](https://www.jitpack.io/).

First add the following to your root `build.gradle`:

```
    allprojects {
		repositories {
			maven { url 'https://www.jitpack.io' }
		}
	}
```

Add the following to your Gradle file `dependencies` closure:

`implementation 'com.github.pokt-network:pocket-android-eth:0.0.1'`

***Optional***

In the case of having errors installing the dependency with the above steps, try adding the following
to the `dependencies` closure:

`implementation 'com.android.support:support-core-utils'`

# About this plugin
A Pocket Network plugin will allow your application to send `Transaction` and `Query` objects to any given Pocket Node
that supports the Ethereum network.

A `Transaction` refers to any calls that alter the state of the network: sending Eth from one account to another, calling a smart contract, etc.

A `Query` refers to any calls that read data from the current state of the network: Getting an account balance, reading from a smart contract.

## Subnetwork considerations
A subnetwork in terms of a Pocket Node is any given parallel network for a decentralized system, for example
in the case of Ethereum, besides Mainnet (subnetwork `1`), you also have access to the Rinkeby testnet (subnetwork `4`). 
In the case of connecting to a custom network, make sure the Pocket Node you are connecting to supports the given subnetwork.

This is useful to allow users to hop between networks, or for establishing differences between your application's 
test environment and production environments.

# Using a Pocket Android Plugin
Just import the `PocketEth` class and call into any of the functions described below. In addition to that you can use
the functions below to send the created `Transaction` and `Query` objects to your configured Pocket Node, either synchronously or asynchronously.

## The Configuration object
The constructor for any given `PocketEth` instance requires a class implementing the `Configuration` interface. 
Let's take a look at the example below:

```
PocketEth pocketEth = new PocketEth(new Configuration() {
    @Override
    public URL getNodeUrl() throws MalformedURLException {
        return new URL("https://ethereum.pokt.network");
    }
});
```

## Creating and Importing a Wallet
Follow the following example to create an Ethereum Wallet:

```
Wallet newWallet = pocketEth.createWallet("4", null);
```

And to import:

```
String privateKey = "0x";
String address = "0x";

Wallet importedWallet = pocketEth.importWallet(privateKey, "4", address, null);
```

## Creating and sending a Transaction
Follow the example below to create a `Transaction` object to write to the given Ethereum network with the parameters below and `subnetwork`. 
Throws `CreateTransactionException` in case of errors.

```
// First import the sender's wallet
String privateKey = "0x";
String address = "0x";

Wallet senderWallet = pocketEth.importWallet(privateKey, "4", address, null);

// Build your transaction parameters
Map<String, Object> txParams = new HashMap<>();
txParams.put("nonce", "0x0");
txParams.put("to", "0x");
txParams.put("value", "0x989680");
// You can pass in correctly encoded data argument to your transaction in the case of calling a smart contract.
txParams.put("data", null);
txParams.put("gas", "0x989680");
txParams.put("gasPrice", "0x989680");

// Create and sign your Transaction object
Transaction transaction = pocketEth.createTransaction(senderWallet, "4", txParams);
```

To send your newly created `Transaction` to the node use the `sendTransaction` method:

```
try {
   Transaction response = pocketEth.sendTransaction(transaction);
} catch(JSONException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```

## Creating and sending a Query
Follow the example below to create a `Transaction` object to read from the given Ethereum network with the parameters below and `subnetwork`. 
Throws `CreateQueryException` in case of errors.

```
String[] rpcParams = {"0x0", "latest"};
Map<String, Object> params = new HashMap<>();
params.put("rpcMethod", "eth_getBalance");
params.put("rpcParams", Arrays.asList(rpcParams));
Query query = pocketEth.createQuery("4", params, null);
```

To send your `Query` to the node use the `executeQuery` method:

```
try {
   Query response = pocketEth.executeQuery(query);
} catch(JSONException e) {
    e.printStackTrace();
} catch (IOException e) {
    e.printStackTrace();
}
```

# References
Refer to the Ethereum JSON RPC documentation [here](https://github.com/ethereum/wiki/wiki/JSON-RPC) for more information on the available RPC methods you can call from your application.
