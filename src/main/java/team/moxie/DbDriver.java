package team.moxie;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A driver for handling all the SCUD operations for the inventory database
 *
 * @author Dustin
 */
public class DbDriver {
	/**
	 * The connection to the database
	 * @see DriverManager
	 */
	private final Connection dbConn;

	/**
	 * Constructor for DbDriver
	 *
	 * @param hostIP        IP of the database host
	 * @param hostPort      Port of the database
	 * @param databaseName  Name of the database
	 * @param dbUser        The username to be used
	 * @param dbPass        The password for the user
	 * @throws SQLException Thrown if the database connection fails
	 */
	public DbDriver(String hostIP, String hostPort, String databaseName, String dbUser, String dbPass)
		throws SQLException {
		// Create the database connection

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		String connString = buildConnString(hostIP, hostPort, databaseName);
		System.out.println("Connecting with the URL: " + connString);
		this.dbConn = DriverManager.getConnection(connString, dbUser, dbPass);
	}

	/**
	 * A helper function to format the info into a connection string
	 *
	 * @param hostIP        IP of the database host
	 * @param hostPort      Port of the database
	 * @param databaseName  Name of the database
	 * @return A formated connection string
	 * @see DbDriver
	 * @see DriverManager
	 */
	private String buildConnString(String hostIP, String hostPort, String databaseName) {
		return "jdbc:mysql://" + hostIP + ":" + hostPort + "/" + databaseName;
	}

	/**
	 * Creates an entry in the database with the given data
	 *
	 * @param id             Product ID
	 * @param quantity       Quantity of the product
	 * @param wholesalePrice Wholesale price of the product
	 * @param salePrice      Sale price of the product
	 * @param supplierId     ID of the supplier for the item
	 * @return Boolean for whether the operation was successful
	 */
	public boolean createEntry(String id, int quantity, double wholesalePrice, double salePrice, String supplierId) {
		return true;
	}

	/**
	 * Deletes the entry with the given ID
	 *
	 * @param id The id of the product
	 * @return Boolean for whether the operation was successful
	 */
	public boolean deleteEntry(String id) {
		return true;
	}

	/**
	 * Searches and returns the entry with the given ID
	 *
	 * @param id The id of the product
	 * @return A dbEntry array if successful or null in not
	 * @see dbEntry
	 */
	public dbEntry searchById(String id) throws SQLException {
		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from `inv`.`inventory` where product_id = '" + id + "'");

			// In this case there should only ever be one as the IDs are set to be unique
			// TODO: 8/28/2020 Make this more robust and catch when there is more than one item
			resultSet.next();

			int quantity = resultSet.getInt("quantity");
			double whole = resultSet.getDouble("wholesale_cost");
			double sale = resultSet.getInt("sale_price");
			String supplier = resultSet.getString("supplier_id");

			// Create and return the entry object
			return new dbEntry(id, quantity, whole, sale, supplier);
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return null;
		}
	}

	/**
	 * Returns an array of dbEntry with all the items with the given supplier ID
	 *
	 * @param id The id of the supplier
	 * @return A dbEntry array if successful or null in not
	 * @see dbEntry
	 */
	public dbEntry[] searchBySupplier(String id) {
		// Create a list to add the entry objects to
		LinkedList<dbEntry> entryList = new LinkedList<>();

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from `inv`.`inventory` where supplier_id = '" + id + "'");

			// In this case there should only ever be one as the IDs are set to be unique
			// TODO: 8/28/2020 Make this more robust and catch when there is more than one item
			while (resultSet.next()) {
				id = resultSet.getString("product_id");
				int quantity = resultSet.getInt("quantity");
				double whole = resultSet.getDouble("wholesale_cost");
				double sale = resultSet.getInt("sale_price");
				String supplier = resultSet.getString("supplier_id");

				// Create and return the entry object
				entryList.add(new dbEntry(id, quantity, whole, sale, supplier));
			}
			return entryList.toArray(new dbEntry[entryList.size()]);
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return null;
		}
	}

	/**
	 * Updates the entry with given ID and info
	 *
	 * @param id             Product ID
	 * @param quantity       Quantity of the product
	 * @param wholesalePrice Wholesale price of the product
	 * @param salePrice      Sale price of the product
	 * @param supplierId     ID of the supplier for the item
	 * @return boolean whether the operation completed successfully
	 */
	public boolean updateEntry(String id, int quantity, double wholesalePrice, double salePrice, String supplierId) {
		return true;
	}
}
