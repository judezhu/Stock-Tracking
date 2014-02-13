/**
 * CICS 525 Assignment 1
 * 
 * Huang Zhu 	15509094
 * Jiahua Chen 	87269122
 * Mu-Jen Wang	44371029
 * 
 * StockServerInterface.java
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface StockServerInterface extends Remote{

	public boolean checkUser(String userName) throws RemoteException;// true if user exists; false if not, also adds user
	
	public boolean isTickerNameValid(String tickerName) throws RemoteException; // true if tickerName is valid, false otherwise
	
	public String query(String tickerName) throws RemoteException; // at least tickerName, price, stockname
	public String buy(String tickerName, int numStocks, String userName) throws RemoteException; //synchronized 
	public String sell(String tickerName, int numStocks, String userName) throws RemoteException; //synchronized
	
	public User getUserInfo(String userName) throws RemoteException;
	public String updateStockPrice(String tickerName, double newPrice) throws RemoteException;
	public List<Stock> getUserStockInfo(String userName) throws RemoteException;
}
