
/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * StockServer.java
 * 
 *
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * StockServer class that stores tracked stocks and user information
 * also saves serialzed stock and user data for persistent storage
 *
 */
public class StockServer implements StockServerInterface {

	private HashMap<String, Stock> stockInquired;
	private HashMap<String, User> usersList;
	private static final String stockFile = "./stock.ser"; 
	private static final String userFile = "./user.ser";

	public StockServer() throws RemoteException {
		super();
		readFile();
		Thread updateThread = new Thread(new PeriodicUpdate(stockInquired));
		updateThread.start();
	}

	/**
	 * check to see if the user is in the user list
	 * if not, create the user and add to the user list
	 */
	@Override
	public boolean checkUser(String userName) throws RemoteException {

		Set<String> keySet = usersList.keySet();
		for (String key : keySet) { 
			if (key.equals(userName)) 
				return true; 
		}
		User newUser = new User(userName); 
		usersList.put(userName, newUser);
		saveFile();
		return false;
	}

	/** 
	 * check if ticker name entered is valid
	 * if price for the specified tickerName is non-zero, the name is valid
	 */
	@Override
	public boolean isTickerNameValid(String tickerName) throws RemoteException {
		double tempPrice = 0;

		// construct the URL to retrive the stock price
		String url = "http://download.finance.yahoo.com/d/quotes.csv?s="
				+ tickerName + "&f=l1=.csv";

		// the result is a comma separated file, stored as a string
		String urlReturn = GetURL.getHTML(url);

		// parse the substring before the first comma, which is the price
		int commaIndex = urlReturn.indexOf(',');
		String field0 = urlReturn.substring(0, commaIndex);

		tempPrice = Double.parseDouble(field0);

		if (tempPrice != 0.0)
			return true;

		else
			return false;
	}

	/**
	 * buy stocks with the specified ticker name, number of stocks and user name
	 */
	@Override	
	public String buy(String tickerName, int numStocks, String userName)
			throws RemoteException {
		String result = "";
		User user = usersList.get(userName);
		Stock stock = stockInquired.get(tickerName);
		if (stock == null) {
			if (isTickerNameValid(tickerName)) {
				stock = new Stock(tickerName);
				stockInquired.put(tickerName, stock);

			}
			else {
				result = "The ticker name provided is invalid.";
				saveFile();
				return result;
			}
		}
		result = user.buyStock(stock, numStocks);
		saveFile();
		return result;
	}

	/**
	 * sell stocks with the specified ticker name, number of stocks and user name
	 */
	@Override	
	public String sell(String tickerName, int numStocks, String userName)
			throws RemoteException {
		String result = "";
		User user = usersList.get(userName);
		Stock stock = stockInquired.get(tickerName);
		if (stock == null) {
			if (isTickerNameValid(tickerName)) {
				stock = new Stock(tickerName);
				stockInquired.put(tickerName, stock);
			}
			else {
				result = "The ticker name provided is invalid.";
				saveFile();
				return result;
			}
		}
		result = user.sellStock(stock, numStocks);
		saveFile();
		return result;
	}

	/**
	 * get user object from user name
	 */
	@Override
	public User getUserInfo(String userName) throws RemoteException {
		return usersList.get(userName);
	}

	/**
	 * update stock to user specified price
	 */
	@Override
	public String updateStockPrice(String tickerName, double newPrice) throws RemoteException {
		String result = "";
		Stock stock = stockInquired.get(tickerName);
		if (stock == null) {
			if (isTickerNameValid(tickerName)) {
				stock = new Stock(tickerName);
				stockInquired.put(tickerName, stock);
			}
			else {
				result = "The ticker name provided is invalid.";
				saveFile();
				return result;
			}
		}
		stock.setStockPrice(newPrice);
		result = "The price has been updated.";
		saveFile();
		return result;
	}
	
	/**
	 * get the list of stocks own by user
	 */
	@Override
	public List<Stock> getUserStockInfo(String userName) throws RemoteException {
		List<Stock> stockList = new ArrayList<Stock>();
		User user = usersList.get(userName);
		for (String tickerName : user.getUserShares().keySet()) {
			stockList.add(stockInquired.get(tickerName));
		}
		return stockList;
	}
	
	/**
	 * query stock price using ticker name
	 * if the ticker name is valid, return the stock name and price
	 * if not, return error message
	 */
	public String query(String tickerName) throws RemoteException {
		String result;
		String companyName;
		double price;

		// check to see if the tickerName is currently being tracked
		if (stockInquired.containsKey(tickerName)) {
			price = (stockInquired.get(tickerName)).getStockPrice();
			companyName = (stockInquired.get(tickerName)).getCompanyName();
			result = "The stock price for " + companyName + " (" + tickerName
					+ ") is :" + price;
		}

		// check to see if the tickerName is valid
		else if (isTickerNameValid(tickerName)) {
			Stock stock = new Stock(tickerName);
			stockInquired.put(tickerName, stock);
			price = (stockInquired.get(tickerName)).getStockPrice();
			companyName = (stockInquired.get(tickerName)).getCompanyName();
			result = "The stock price for " + companyName + " (" + tickerName
					+ ") is :" + price
					+ ".\n The stock has been added to the track list.";
		}

		// the tickerName is invalid
		else {
			result = "The ticker name provided is invalid";

		}
		saveFile();
		return result;
	}

	public static void main(String args[]) {
		try {
			// System.setProperty("java.rmi.server.hostname", "127.0.0.1");
			System.out.println(java.net.InetAddress.getLocalHost());

			// create and export a remote object "server"
			StockServer server = new StockServer();
			// create a remote object stub
			StockServerInterface stub = (StockServerInterface) UnicastRemoteObject
					.exportObject(server, 0);
			// register the remote object with a java RMI registry
			Registry registry = LocateRegistry.createRegistry(1099);
			// bind the name "Server" to the remote object stub
			registry.rebind("Server", stub); 

			System.out.println("Server is ready.");

			while (true) {

				System.out
				.println("---------------------------------------------------------");
				System.out
				.println("| Commands: s - Save Status  	e -  Exit                 |");
				System.out
				.println("---------------------------------------------------------");

				System.out.println("Please enter command:");
				Scanner input = new Scanner(System.in);

				String command = input.nextLine();

				if (command.equals("s")) {
					try {
						server.saveFile();
						Thread.sleep(1000);
						System.out
						.println("Current system status has been saved.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				else if (command.equals("e")) {

					int succesful = 0;

					try {
						server.saveFile();
						Thread.sleep(1000);
						System.out
						.println("Current system status has been saved.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					try {
						registry.unbind("Server");
						UnicastRemoteObject.unexportObject(server, true);
						Thread.sleep(1000);
					} catch (NotBoundException e) {
						e.printStackTrace();
						succesful = -1;
					} catch (InterruptedException e) {
						e.printStackTrace();
						succesful = -1;
					} catch (AccessException e) {
						System.out.println(e.detail.getMessage());
						succesful = -1;
					} catch (UnmarshalException e) {
						System.out.println(e.detail.getMessage());
						succesful = -1;
					} catch (RemoteException e) {
						System.out.println(e.detail.getMessage());
						succesful = -1;
					}

					input.close();
					System.out.println("Server shut down.");
					System.exit(succesful);
				}

				else {
					System.out.println("Please provide a valid command.");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * save serialized file
	 */
	private synchronized void saveFile() {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		try {
			fos = new FileOutputStream(stockFile);
			out = new ObjectOutputStream(fos);
			out.writeObject(stockInquired);
			out.close();
			fos.close();

			fos = new FileOutputStream(userFile);
			out = new ObjectOutputStream(fos);
			out.writeObject(usersList);
			out.close();
			fos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * read serialized file
	 */
	@SuppressWarnings("unchecked")
	private synchronized void readFile() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(stockFile);
			in = new ObjectInputStream(fis);
			stockInquired = (HashMap<String, Stock>) in.readObject();
			in.close();
			fis.close();
		} catch (FileNotFoundException ex) {
			stockInquired = new HashMap<String, Stock>();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try {
			fis = new FileInputStream(userFile);
			in = new ObjectInputStream(fis);
			usersList = (HashMap<String, User>) in.readObject();
			in.close();
			fis.close();

		} catch (FileNotFoundException ex) {
			usersList = new HashMap<String, User>();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}

/**
 * thread to update stock price every 2 mins
 */
class PeriodicUpdate implements Runnable {
	private HashMap<String, Stock> stockInquired;

	public PeriodicUpdate(HashMap<String, Stock> stockInquired) {
		this.stockInquired = stockInquired;
	}
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(120000); // sleep for 2 mins
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Stock stock : stockInquired.values()) {
				System.out.println(stock.getTickerName());
				System.out.println(stock.getStockPrice());
				stock.updateLatestStockPrice();
			}
		}
	}
}
