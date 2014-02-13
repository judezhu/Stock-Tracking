/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * Stock.java
 * 
 */
import java.io.Serializable;

public class Stock implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int INITIALSTOCK = 1000;

	private final String tickerName;
	private volatile double price;
	private volatile int numStockLeft;
	private final String companyName;

	public Stock(String newTickerName) {
		this.tickerName = newTickerName;
		this.companyName = getCompanyNameOnline(newTickerName);
		this.numStockLeft = INITIALSTOCK;
		updateLatestStockPrice();

	}
	
//	@Override
//	public boolean equals(Object aStock) {
//		String name = this.getTickerName();
//		String anotherName = ((Stock) aStock).getTickerName();
//		if (name.equals(anotherName)) {
//			return true;
//		}
//		return false;
//	}
	
	/**
	 * Get the ticker name
	 * @return the ticker name of the stock
	 */
	public String getTickerName() {
		return tickerName;
	}

	/**
	 * Get the price
	 * @return the price of the stock
	 */
	public double getStockPrice() {
		return price;
	}

	/**
	 * set the price of the stock
	 * @param newPrice the price to set the stock to
	 */
	public synchronized void setStockPrice(double newPrice) {
		this.price = newPrice;
	}

	/**
	 * get the number of stock left
	 * @return number of stock left
	 */
	public int getNumStockLeft() {
		return numStockLeft;
	}

	/**
	 * change the number of stock left
	 * @param newNumStockLeft the updated number of stock
	 */
	public synchronized void setNumStockLeft(int newNumStockLeft) {
		this.numStockLeft = newNumStockLeft;
	}

	/**
	 * get the company name of the stock
	 * @return company name 
	 */
	public String getCompanyName() {
		return companyName;
	}


	/**
	 * get the latest stock price online
	 * @return stock price from online source
	 */
	public synchronized double updateLatestStockPrice() {
		double price = 0;

		// construct the URL to retrieve the stock price
		String url = "http://download.finance.yahoo.com/d/quotes.csv?s="
				+ tickerName + "&f=l1=.csv";

		// the result is a comma separated file, stored as a string
		String urlReturn = GetURL.getHTML(url);

		// parse the substring before the first comma, which is the price
		int commaIndex = urlReturn.indexOf(',');
		String field0 = urlReturn.substring(0, commaIndex);

		price = Double.parseDouble(field0);

		this.price = price;
		return price;

	}

	/**
	 * get the company name online from ticker name
	 * @param newTickername
	 * @return company name
	 */
	private String getCompanyNameOnline(String newTickername){		
		String companyName = null;

		// construct the URL to retrieve the stock name
		String url = "http://download.finance.yahoo.com/d/quotes.csv?s="
				+ tickerName + "&f=n=.csv";

		// the result is a comma separated file, stored as a string
		String urlReturn = GetURL.getHTML(url);

		// parse the substring before the first comma, which is the price
		int commaIndex = urlReturn.indexOf(',');
		companyName = urlReturn.substring(1, commaIndex-1);

		return companyName;
	}

}