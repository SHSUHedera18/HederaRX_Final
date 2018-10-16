import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.hedera.sdk.account.AccountGetBalance;
import com.hedera.sdk.account.AccountSend;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contracts.ContractCall;
import com.hedera.sdk.contracts.ContractCreate;
import com.hedera.sdk.contracts.ContractGetBytecode;
import com.hedera.sdk.contracts.ContractGetInfo;
import com.hedera.sdk.contracts.SoliditySupport;
import com.hedera.sdk.file.FileCreate;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.utilities.ExampleUtilities;

public class Main 
{
	private static HederaTransactionAndQueryDefaults txQueryDefaults;

	public static void main(String[] args) 
	{
		try
		{
			HederaAccount account = new HederaAccount();
		    HederaContract contract= new HederaContract();
			txQueryDefaults = new HederaTransactionAndQueryDefaults();
		    txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		    account.txQueryDefaults = txQueryDefaults;
		    HederaNode node = new HederaNode();
			//set up to get the node info
			Properties prop = new Properties();
			InputStream input = null;
			try
			{
				input = new FileInputStream("node.properties");
				//load a properties file
				prop.load(input);
				node.setHostPort(prop.getProperty("nodeaddress"), Integer.valueOf(prop.getProperty("nodeport")));
				node.setAccountID(Long.parseLong(prop.getProperty("nodeAccountShard")), Long.parseLong(prop.getProperty("nodeAccountRealm")), Long.parseLong(prop.getProperty("nodeAccountNum")));
				//get the property value of the contract
		        account.accountNum = Long.parseLong(prop.getProperty("payingAccountNum"));
		        account.realmNum = Long.parseLong(prop.getProperty("payingAccountRealm"));
		        account.shardNum = Long.parseLong(prop.getProperty("payingAccountShard"));
				contract.contractAccountAccountNum = Long.parseLong(prop.getProperty("payingAccountNum"));
				contract.contractAccountRealmNum = Long.parseLong(prop.getProperty("payingAccountRealm"));
				contract.contractAccountShardNum = Long.parseLong(prop.getProperty("payingAccountShard"));
				
			    account.setNode(node);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			account.getInfo();	
			
			Thread.sleep(1500);
			AccountGetBalance.getBalance(account);
			
			Thread.sleep(1500);
			sendCustomMemo("2018101610010001REF987654321", account, new HederaAccount(0, 0, 1008), 5000);
			
			Thread.sleep(1500);
			accountRecord(account);	
			
			Thread.sleep(1500);
			contractRecord(contract);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//create new contract
	public static void newContract()
	{
		try
		{
	    	// create a file
	    	// new file object
	    	HederaFile file = new HederaFile();
	    	// setup transaction/query defaults (durations, etc...)
	    	txQueryDefaults = new HederaTransactionAndQueryDefaults();
	    	file.txQueryDefaults = txQueryDefaults;
	    	// get file contents
	    	InputStream is = Main.class.getResourceAsStream("/main/resources/simpleStorage.bin");
	    	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    	int nRead;
	    	byte[] data = new byte[4096];
	    	while ((nRead = is.read(data, 0, data.length)) != -1) {
	    		buffer.write(data, 0, nRead);
	    	}
	    			 
	    	buffer.flush();
	    	byte[] fileContents = buffer.toByteArray();
	    			    
	    	// create a file with contents
	
	
			file = FileCreate.create(file, fileContents);
	
	    	Thread.sleep(1500);
	    	
	    	// new contract object
	    	HederaContract contract = new HederaContract();
	    	// setup transaction/query defaults (durations, etc...)
	    	contract.txQueryDefaults = txQueryDefaults;
	
	    	// create a contract
	    	contract = ContractCreate.create(contract, file.getFileID(), 0);
	    	//contract = create(contract, file.getFileID(), 1);
	    	if (contract != null)
	    	{    					
	    		if (contract != null) 
	    		{
	    			// getinfo
	    			Thread.sleep(1500);
	    			ContractGetInfo.getInfo(contract);
	    			// get bytecode
	    			Thread.sleep(1500);
	    			ContractGetBytecode.getByteCode(contract);
	    			// call
	    			final String SC_SET_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
	    			long gas = 250000000;
	    			long amount = 14;
	    			byte[] functionParameters = SoliditySupport.encodeSet(10,SC_SET_ABI);	 
	    			ContractCall.call(contract, gas, amount, functionParameters);
	    		} 
	    	}	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//get contract records
	public static void contractRecord(HederaContract contract) throws Exception
	{
		
		List<HederaTransactionRecord> records = new ArrayList<HederaTransactionRecord>();
		records = contract.getTransactionRecords();
		if (records != null) 
		{
			for (HederaTransactionRecord record : records)
			{
				System.out.println(record);
			}
		} 
		else 
		{
			System.out.println("Empty!!!");
		}
	}
	
	//get account record
	public static void accountRecord(HederaAccount account) throws Exception
	{
		List<HederaTransactionRecord> records = new ArrayList<HederaTransactionRecord>();
		records = account.getRecords();
		if (records != null) 
		{
			for (HederaTransactionRecord record : records)
			{
				System.out.println(record);
			}
		} 
		else 
		{
			System.out.println("Empty!!!");
		}
	}
	
	//create memo transac
	public static void sendCustomMemo(String memo, HederaAccount account, HederaAccount toAccount, long transaction)
	{
		try
		{
			txQueryDefaults.memo = memo;
			txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
			AccountSend.send(account, toAccount, transaction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
