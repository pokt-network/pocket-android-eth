package network.pokt.eth;

import android.content.Context;
import android.support.test.*;
import android.support.test.runner.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import network.pokt.eth.util.TestConfiguration;
import network.pokt.pocketsdk.interfaces.SendRequestCallback;
import network.pokt.pocketsdk.models.Query;
import network.pokt.pocketsdk.models.Transaction;
import network.pokt.pocketsdk.models.Wallet;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PocketEthTest {

    PocketEth plugin;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("network.pokt.pocket_android_eth", appContext.getPackageName());
    }

    @Test
    public void testExecuteQuery() throws Exception {
        String[] rpcParams = {"0x0", "latest"};

        Map<String, Object> params = new HashMap<>();
        params.put("rpcMethod", "eth_getBalance");
        params.put("rpcParams", Arrays.asList(rpcParams));

        Query query = this.plugin.createQuery("1", params, null);
        Query response = this.plugin.executeQuery(query);
        assertTrue(response.isExecuted());
        assertFalse(response.isError());
    }

    @Test
    public void testSendTransaction() throws Exception {
        Wallet wallet = this.plugin.createWallet("1", null);
        Transaction transaction = this.plugin.createTransaction(wallet,"1", PocketEthTest.getTransactionParams());
        Transaction response = this.plugin.sendTransaction(transaction);
        assertTrue(response.isSent());
        assertFalse(response.isError());
    }

    @Test
    public void testExecuteQueryAsync() throws Exception {
        String[] rpcParams = {"0x0", "latest"};

        Map<String, Object> params = new HashMap<>();
        params.put("rpcMethod", "eth_getBalance");
        params.put("rpcParams", Arrays.asList(rpcParams));

        Query query = this.plugin.createQuery("1", params, null);
        this.plugin.executeQueryAsync(new SendRequestCallback<Query>(query) {
            @Override
            public void onResponse(Query response, Exception exception) {
                assertNull(exception);
                assertTrue(response.isExecuted());
                assertFalse(response.isError());
            }
        });
    }

    @Test
    public void testSendTransactionAsync() throws Exception {
        Wallet wallet = this.plugin.createWallet("1", null);
        Transaction transaction = this.plugin.createTransaction(wallet,"1", PocketEthTest.getTransactionParams());
        this.plugin.sendTransactionAsync(new SendRequestCallback<Transaction>(transaction) {
            @Override
            public void onResponse(Transaction response, Exception exception) {
                assertNull(exception);
                assertTrue(response.isSent());
                assertFalse(response.isError());
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        this.plugin = new PocketEth(new TestConfiguration());
    }

    @After
    public void tearDown() throws Exception {
        this.plugin = null;
    }

    private static Map<String, Object> getTransactionParams(){
        Map<String, Object> result = new HashMap<>();
        result.put("nonce", BigInteger.ZERO);
        result.put("gasPrice", new BigInteger("1000000000"));
        result.put("gasLimit", new BigInteger("21000"));
        result.put("to", "0x0");
        result.put("value", new BigInteger("1000000000"));
        result.put("data", "");
        return result;
    }
}