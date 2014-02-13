/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * User.java
 * 
 */
import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final double INTIALMONEY = 1000;

	private final String userName;
	private final HashMap<String, Integer> userShares;
	private volatile double numMoneyLeft;

	public User(String newUserName){
		this.userName = newUserName;
		this.userShares = new HashMap<String, Integer>();
		this.numMoneyLeft = INTIALMONEY;
	}

	
	/**
	 * Get the username
	 * @return the username of the user
	 */
	public String getUserName(){
		return userName;
	}

	/**
	 * Get the amount of money owned
	 * @return the amount of money owned by the user
	 */
	public double getNumMoneyLeft() {
		return numMoneyLeft;
	}

	public HashMap<String, Integer> getUserShares() {
		return userShares;
	}


	/**
	 * Buy stock for a given amount of share
	 * @param aStock the stock to be bought by the user
	 * @param share the amount of stock to be bought by the user
	 * @return the status of the execution
	 */
	public synchronized String buyStock (Stock aStock, int share) {
		
		String tickerName = aStock.getTickerName();
		
		if (this.getNumMoneyLeft() < aStock.getStockPrice() * share) {
			return "Insufficient fund";
		}

		// get the lock of the stock
		synchronized (aStock) {
			if (aStock.getNumStockLeft() < share) {
				return "Stock out of Share";
			}
			aStock.setNumStockLeft(aStock.getNumStockLeft() - share);
		}

		if (userShares.get(tickerName) == null) {
			userShares.put(tickerName, 0);
		}
		userShares.put(tickerName, userShares.get(tickerName) + share);
		numMoneyLeft -= aStock.getStockPrice() * share;
		return "The transaction is completed.";
	}

	/**
	 * Sell stock for a given amount of share
	 * @param aStock the stock to be sold by the user
	 * @param share the amount of share to be sold by the user
	 * @return the status of the execution
	 */
	public synchronized String sellStock (Stock aStock, int share) {
		
		String tickerName = aStock.getTickerName();
		
		if (userShares.get(tickerName) == null) {
			return "You have not purchased the stock.";
		}

		if (userShares.get(tickerName) < share) {
			return "You don't have enough share to sell.";
		}
		// get the lock of the stock
		synchronized (aStock) {
			aStock.setNumStockLeft(aStock.getNumStockLeft() + share);
		}
		userShares.put(tickerName, userShares.get(tickerName) - share);
		numMoneyLeft += aStock.getStockPrice() * share;
		return "The transaction is completed.";
	}

}
