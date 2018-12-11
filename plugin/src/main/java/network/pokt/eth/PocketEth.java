package network.pokt.eth;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import network.pokt.pocketsdk.exceptions.CreateQueryException;
import network.pokt.pocketsdk.exceptions.CreateTransactionException;
import network.pokt.pocketsdk.exceptions.CreateWalletException;
import network.pokt.pocketsdk.exceptions.ImportWalletException;
import network.pokt.pocketsdk.exceptions.InvalidConfigurationException;
import network.pokt.pocketsdk.interfaces.Configuration;
import network.pokt.pocketsdk.interfaces.PocketPlugin;
import network.pokt.pocketsdk.models.Query;
import network.pokt.pocketsdk.models.Transaction;
import network.pokt.pocketsdk.models.Wallet;

/**
 * PocketPlugin implementation for the Ethereum Network
 */
public class PocketEth extends PocketPlugin {

    private final String NETWORK = "ETH";


    public PocketEth(@NotNull Configuration configuration) throws InvalidConfigurationException {
        super(configuration);
    }

    @Override
    public @NotNull Wallet createWallet(@NotNull String subnetwork, Map<String, Object> data) throws CreateWalletException {
        Wallet result;

        // Try creating the wallet
        ECKeyPair ecKeyPair;
        try {
            ecKeyPair = Keys.createEcKeyPair();
        } catch (Exception e) {
            throw new CreateWalletException(data, e.getMessage());
        }

        String privateKey = ecKeyPair.getPrivateKey().toString(16);
        String address = Keys.getAddress(ecKeyPair);

        try {
            result = this.importWallet(privateKey, subnetwork, address, data);
        } catch (ImportWalletException e) {
            throw new CreateWalletException(data, e.getMessage());
        }

        if(result == null) {
            throw new CreateWalletException(data, "Unknown error occurred generating Wallet");
        }
        return result;
    }

    @Override
    public @NotNull Wallet importWallet(@NotNull String privateKey, @NotNull String subnetwork, String address, Map<String, Object> data) throws ImportWalletException {
        Wallet result;

        // Try re-creating the wallet
        ECKeyPair ecKeyPair;
        byte[] privateKeyBytes;
        try {
            privateKeyBytes = PocketEth.hexStringToByteArray(privateKey);
            ecKeyPair = ECKeyPair.create(privateKeyBytes);
        } catch (Exception e) {
            throw new ImportWalletException(privateKey, address, data, e.getMessage());
        }

        if(!Keys.getAddress(ecKeyPair).equals(address)) {
            throw new ImportWalletException(privateKey, address, data, "Invalid address provided");
        }

        try {
            // Parse data
            JSONObject walletData = data == null ? new JSONObject() : new JSONObject(data);
            result = new Wallet(address, privateKey, this.getNetwork(), subnetwork, walletData);
        } catch (JSONException e) {
            throw new ImportWalletException(privateKey, address, data, e.getMessage());
        }

        if(result == null) {
            throw new ImportWalletException(privateKey, address, data, "Unknown error occurred generating Wallet");
        }
        return result;
    }

    @Override
    public @NotNull Transaction createTransaction(@NotNull Wallet wallet, @NotNull String subnetwork, Map<String, Object> params) throws CreateTransactionException {
        Transaction result;

        BigInteger nonce;
        BigInteger gasPrice;
        BigInteger gasLimit;
        String to;
        BigInteger value;
        String data;
        byte chainId;
        Credentials credentials;

        try {
            nonce = (BigInteger) params.get("nonce");
            gasPrice = (BigInteger) params.get("gasPrice");
            gasLimit = (BigInteger) params.get("gasLimit");
            to = (String) params.get("to");
            value = (BigInteger) params.get("value");
            data = (String) params.get("data");
            chainId = subnetwork.getBytes()[0];
            ECKeyPair keyPair = ECKeyPair.create(PocketEth.hexStringToByteArray(wallet.getPrivateKey()));
            credentials = Credentials.create(keyPair);
        } catch (Exception e) {
            throw new CreateTransactionException(wallet, subnetwork, params, e.getMessage());
        }

        // Generate raw transaction object
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);

        // Encode raw transaction
        byte[] signedTxBytes = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);

        // Serialize signed tx to string
        String signedTx = new String(signedTxBytes);

        try {
            result = new Transaction(this.getNetwork(), subnetwork, signedTx, null);
        } catch (JSONException e) {
            throw new CreateTransactionException(wallet, subnetwork, params, e.getMessage());
        }

        if(result == null) {
            throw new CreateTransactionException(wallet, subnetwork, params, "Unknown error creating transaction");
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Query createQuery(@NotNull String subnetwork, Map<String, Object> params, Map<String, Object> decoder) throws CreateQueryException {
        Query result;

        // Extract rpc request
        String rpcMethod;
        List<Object> rpcParams;
        JSONObject queryData = new JSONObject();
        try {
            rpcMethod = (String) params.get("rpcMethod");
            rpcParams = (List<Object>) params.get("rpcParams");
            queryData.put("rpc_method", rpcMethod);
            queryData.put("rpc_params", rpcParams);
        } catch (Exception e) {
            throw new CreateQueryException(subnetwork, params, decoder, e.getMessage());
        }

        // Extract decoder
        List<String> returnTypes;
        JSONObject queryDecoder = new JSONObject();
        try {
            returnTypes = (List<String>) decoder.get("returnTypes");
            decoder.put("return_types", returnTypes);
        } catch (Exception e) {
            // Log message and send empty decoder
            Logger.getGlobal().warning(e.getMessage());
            queryDecoder = new JSONObject();
        }

        try {
            result = new Query(this.getNetwork(), subnetwork, queryData, queryDecoder);
        } catch (Exception e) {
            throw new CreateQueryException(subnetwork, params, decoder, e.getMessage());
        }

        if(result == null) {
            throw new CreateQueryException(subnetwork, params, decoder, "Unknown error creating query");
        }
        return result;
    }

    @Override
    public @NotNull String getNetwork() {
        return NETWORK;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
