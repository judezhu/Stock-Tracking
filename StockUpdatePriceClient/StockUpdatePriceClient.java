
/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * StockUpdatePriceClient.java
 * This client can update stock price on server
 *
 */
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class StockUpdatePriceClient {

	private StockUpdatePriceClient(){}

	/**
	 * check if the string is a valid ipv4 address
	 * source http://stackoverflow.com/questions/4581877/validating-ipv4-string-in-java
	 * @param ip
	 * @return true if the address is valid, false otherwise
	 */
	public static final boolean checkIPv4(final String ip) {
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


	public static void main(String[] args){

		String tickerName;
		Double stockPrice;

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
				if(checkIPv4(temp)){
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


		// get the remote object from the registry
		try{
			Registry registry = LocateRegistry.getRegistry(host);
			StockServerInterface stub = (StockServerInterface)registry.lookup("Server");

			while(true){

				System.out
				.println("-----------------------------------------------------------");
				System.out
				.println("| Commands: u - update stock price     e - exit            |");
				System.out
				.println("-----------------------------------------------------------");

				// Prompt the user to enter select an option
				System.out.println("Please enter command:");
				String command = input.nextLine().toLowerCase().trim();

				if (command.equals("u")) {
					System.out.println("Please enter the ticker name for price update:");
					tickerName = input.nextLine().toUpperCase().trim();
					boolean istickerName = stub.isTickerNameValid(tickerName);
					if (istickerName) {

						System.out.println("Please enter the new price:");
						try{
							stockPrice = Double.parseDouble(input.nextLine().trim());
							if(stockPrice<=0){
								System.out.println("invalid value, new price must be a positive number!");
							}
							else {
								String updateResult = stub.updateStockPrice(tickerName, stockPrice);
								System.out.println(updateResult);	
							}

						}
						catch(IllegalArgumentException e){
							System.out.println("invalid value, new price must be a positive number!");
						} 
					}
					else{
						System.out.println("Ticker name is not valid. Please try again.");
					}
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

		}

		catch (Exception e) {
			System.out.println("Server crashed. Please wait for maintenance.");
		}
	}
}

