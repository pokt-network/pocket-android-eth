package network.pokt.eth;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.util.List;
import java.util.Map;

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
 * Android Ethereum Plugin
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
            throw new CreateWalletException(data, "Error generating private keys");
        }

        byte[] privateKeyBytes = Keys.serialize(ecKeyPair);
        String privateKey = new String(privateKeyBytes);
        String address = Keys.getAddress(ecKeyPair);

        try {
            result = this.importWallet(privateKey, subnetwork, address, data);
        } catch (ImportWalletException e) {
            throw new CreateWalletException(data, "Error serializing wallet");
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
        try {
            ecKeyPair = Keys.deserialize(privateKey.getBytes());
        } catch (Exception e) {
            throw new ImportWalletException(privateKey, address, data, "Error deserializing private key");
        }

        byte[] privateKeyBytes = Keys.serialize(ecKeyPair);

        if(privateKey != new String(privateKeyBytes)) {
            throw new ImportWalletException(privateKey, address, data, "Invalid privatekey provided");
        }

        if(address != Keys.getAddress(ecKeyPair)) {
            throw new ImportWalletException(privateKey, address, data, "Invalid address provided");
        }

        try {
            result = new Wallet(address, privateKey, this.getNetwork(), subnetwork, new JSONObject(data));
        } catch (JSONException e) {
            throw new ImportWalletException(privateKey, address, data, "Error generating wallet");
        }

        if(result == null) {
            throw new ImportWalletException(privateKey, address, data, "Unknown error occurred generating Wallet");
        }
        return result;
    }

    @Override
    public @NotNull Transaction createTransaction(@NotNull Wallet wallet, @NotNull String subnetwork, Map<String, Object> params) throws CreateTransactionException {
        return null;
    }

    @Override
    public @NotNull Query createQuery(@NotNull String subnetwork, Map<String, Object> params, Map<String, Object> decoder) throws CreateQueryException {
        return null;
    }

    @Override
    public @NotNull String getNetwork() {
        return NETWORK;
    }

    @Override
    public @NotNull List<String> getSubnetworks() {
        return null;
    }
}
