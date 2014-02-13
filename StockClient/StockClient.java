
/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * StockClient.java
 * This client can query stock, buy/ sell stocks
 *
 */

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;


public class StockClient {

	private String userName;

	public StockClient() {
		super();
	}

	/**
	 * get user name
	 * @return user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * set user name
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * check if ticker name format is valid
	 * @param name
	 * @return true if ticker name format is valid, false otherwise
	 */
	public boolean checkName(String name) {
		if (name.matches("[A-Z]+"))
			return true;
		else
			System.out.println("Ticker name can only consist of letters");
		return false;
	}

	/**
	 * check if number of stocks are valid
	 * @param Number
	 * @return true if number of stocks are positive integers, false otherwise
	 */
	public boolean checkNumber(String Number){
		if(Number.matches("[0-9]+")){
			if((Integer.parseInt(Number))>0){
				return true;
			}
			else{
				System.out.println("Number of stock share can't be zero. ");
				return false;
			}
		}
		else{
			System.out.println("Please enter a positive number.");
			return false;
		}
	}

	/**
	 * check if the string is a valid ipv4 address
	 * source http://stackoverflow.com/questions/4581877/validating-ipv4-string-in-java
	 * @param ip
	 * @return true if the address is valid, false otherwise
	 */
	public final boolean checkIPv4(final String ip) {
		boolean isIPv4;
		try {
			final InetAddress inet = InetAddress.getByName(ip);
			isIPv4 = inet.getHostAddress().equals(ip)
					&& inet instanceof Inet4Address;
		} catch (final UnknownHostException e) {
			isIPv4 = false;
			System.out.println("This ip address is not valid. Please try again.");
		}
		return isIPv4;
	}

	/**
	 * display user stock info
	 * @param user current user
	 * @param stockList stocks that the user owns
	 */
	public void displayAccount(User user, List<Stock> stockList) {
		System.out.println("Username: " + user.getUserName());
		System.out.print("Account Blance: ");
		System.out.printf("%.2f", user.getNumMoneyLeft());
		System.out.println("\n");

		for (Stock stock : stockList) 
		{
			System.out.println(stock.getTickerName() + " " +
					stock.getCompanyName() + "\n"+"Number of Shares: " +
					Integer.toString(user.getUserShares().get(stock.getTickerName())) + "\n" +
					"Current Price: " + Double.toString(stock.getStockPrice()) + "\n"
					); 
		} 
	}


	public static void main(String[] args) {

		StockClient stockClient = new StockClient();

		String host = " ";
		Scanner input = new Scanner(System.in);

		while(true){
			System.out.println("Please select server:");
			System.out
			.println("---------------------------------------------------------");
			System.out
			.println("| Commands: 1 - local host        2 - remote server     |");
			System.out
			.println("|           e - Exit                                    |");
			System.out
			.println("---------------------------------------------------------");

			String serverType = input.nextLine();
			if(serverType.equals("1")){
				host = "127.0.0.1";
				break;
			}
			else if(serverType.equals("2")){
				System.out.println("Please enter server IP address(eg: 128.158.152.0):");
				String temp = input.nextLine();
				if(stockClient.checkIPv4(temp)){
					host = temp;
					try {
						if(InetAddress.getByName(host).isReachable(2000)){
						}
						else{
							System.out.println("Connection time out. Please try again.\n");
							continue;
						}
					} catch (UnknownHostException e) {
						System.out.println("Connection error. Please restart client");
					} catch (IOException e) {
						System.out.println("Connection error. Please restart client");
					}						

					break;
				}
				else{
					System.out.println("Please provide a correct IP address.\n");
				}
			}
			else if(serverType.equals("e")){
				input.close();
				System.out.println("Welcome to user our system.");
				System.out.println("Have a nice day!");
				System.exit(0);
			}
			else{
				System.out.println("Command can't be recognized. Please try again.");
			}
		}


		try {
			Registry registry = null;
			StockServerInterface stub = null;

			registry = LocateRegistry.getRegistry(host);
			stub = (StockServerInterface) registry.lookup("Server");

			System.out.println("Welcome to our Stock Trading System!");
			System.out.println("Please enter your User Name:");

			stockClient.setUserName(input.nextLine().trim());

			// check whether user exists in the server
			boolean userCheck = stub.checkUser(stockClient.getUserName());

			if (userCheck) {
				System.out.println("User found");
			} else {
				System.out.println("User doesn't exist. ");
				System.out.println("New user has been created.");
			}

			while (true) {
				// client prompt
				System.out
				.println("---------------------------------------------------------");
				System.out
				.println("| Commands: q - Query price        b - Buy stock        |");
				System.out
				.println("|           s - Sell stock         d - Display account  |");
				System.out
				.println("|           e - Exit                                    |");
				System.out
				.println("---------------------------------------------------------");

				// Prompt the user to enter select an option
				System.out.println("Please enter command:");
				String command = input.nextLine().trim().toLowerCase();

				if (command.equals("q")) {
					System.out.println("Please enter ticker name:");
					String tickerName = input.nextLine().trim().toUpperCase();
					boolean istickerName = stockClient.checkName(tickerName);
					if (istickerName) {
						String queryResultMessage = stub.query(tickerName);
						System.out.println(queryResultMessage);
					} 
				}

				else if (command.equals("b")) {
					System.out.println("Please enter ticker name:");
					// checks need to be done here
					String temp = input.nextLine().trim().toUpperCase();
					if(stockClient.checkName(temp))
					{
						String tickerName = temp;
						System.out.println("Please enter number of shares:");
						// checks need to be done here
						String shareNumStr =input.nextLine().trim();
						if(stockClient.checkNumber(shareNumStr)){
							int shareNum = Integer.parseInt(shareNumStr);
							String buyResultMessage = stub.buy(tickerName, shareNum,
									stockClient.getUserName());
							System.out.println(buyResultMessage+"\n");
							List<Stock> stockList = stub.getUserStockInfo(stockClient.getUserName());
							User user = stub.getUserInfo(stockClient.getUserName());
							stockClient.displayAccount(user, stockList);
						}
					}	
				}

				else if (command.equals("s")) {
					System.out.println("Please enter ticker name:");
					// checks need to be done here
					String temp = input.nextLine().trim().toUpperCase();
					if(stockClient.checkName(temp)){
						String tickerName = temp;
						System.out.println("Please enter number of shares:");
						// checks need to be done here
						String shareNumStr = input.nextLine().trim();
						if(stockClient.checkNumber(shareNumStr))
						{
							int shareNum = Integer.parseInt(shareNumStr);
							String sellResultMessage = stub.sell(tickerName, shareNum,
									stockClient.getUserName());
							System.out.println(sellResultMessage+"\n");
							List<Stock> stockList = stub.getUserStockInfo(stockClient.getUserName());
							User user = stub.getUserInfo(stockClient.getUserName());
							stockClient.displayAccount(user, stockList);
						}
					}
				}

				else if (command.equals("d")) {
					List<Stock> stockList = stub.getUserStockInfo(stockClient.getUserName());
					User user = stub.getUserInfo(stockClient.getUserName());
					stockClient.displayAccount(user, stockList);
				}

				else if (command.equals("e")) {
					input.close();
					System.out.println("Thanks for using this system.");
					System.out.println("Have a nice day!");
					break;
				}

				// other input: ask for retyping again
				else {
					System.out.println("Command can't be recognized.");
					System.out
					.println("Please follow the instructions and retype it again.");
				}
			}
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Server crashed. Please wait for maintenance.");
		}

	}

}