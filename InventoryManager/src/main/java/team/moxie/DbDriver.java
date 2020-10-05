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
		// cannot be longer than 12 char
		if (id.length() > 12 || supplierId.length() > 12) return false;

		if (quantity < 0 || wholesalePrice < 0 || salePrice < 0) return false;

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();

			//insert into inv.inventory(product_id, quantity, wholesale_cost, sale_price, supplier_id) values ('2FT57YS7CM97',112,112.0,112.0, 'YTCMSBPA')
			int result = statement.executeUpdate(
				String.format(
					"insert into inv.inventory(product_id, quantity, wholesale_cost, sale_price, supplier_id) values ('%s',%d,%s,%s,'%s')",
					id,
					quantity,
					wholesalePrice,
					salePrice,
					supplierId
				)
			);

			return result == 1;
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return false;
		}
	}

	/**
	 * Deletes the entry with the given ID
	 *
	 * @param id The id of the product
	 * @return Boolean for whether the operation was successful
	 */
	public boolean deleteEntry(String id) {
		// cannot be longer than 12 char
		if (id.length() > 12) return false;

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();

			//delete from inv.inventory where product_id='2FT57YS7CM97';
			int result = statement.executeUpdate(String.format("DELETE FROM inv.inventory WHERE product_id='%s'", id));

			return result == 1;
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return false;
		}
	}

	/**
	 * Searches and returns the entry with the given ID
	 *
	 * @param id The id of the product
	 * @return A dbEntry array if successful or null in not
	 * @see dbEntry
	 */
	public dbEntry searchById(String id) {
		// cannot be longer than 12 char
		if (id.length() > 12) return null;

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			ResultSet resultSet = statement.executeQuery(
				String.format("select * from `inv`.`inventory` where product_id = '%s'", id)
			);

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
	public LinkedList<dbEntry> searchBySupplier(String id) {
		// cannot be longer than 12 char
		if (id.length() > 12) return null;

		// Create a list to add the entry objects to
		LinkedList<dbEntry> entryList = new LinkedList<>();

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			ResultSet resultSet = statement.executeQuery(
				String.format("select * from `inv`.`inventory` where supplier_id = '%s'", id)
			);

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
			return entryList;
			//			return entryList.toArray(new dbEntry[entryList.size()]);
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
		// cannot be longer than 12 char
		if (id.length() > 12 || supplierId.length() > 12) return false;
		// verify that the values are not negative
		if (quantity < 0 || wholesalePrice < 0 || salePrice < 0) return false;

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			int result = statement.executeUpdate(
				String.format(
					"update inv.inventory set quantity = %d, wholesale_cost = %s, sale_price = %s where product_id = '%s'",
					quantity,
					wholesalePrice,
					salePrice,
					id
				)
			);

			return result == 1;
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return false;
		}
	}

	/**
	 * Returns the entire database
	 * PLEASE REFRAIN FROM USING UNLESS YOU HAVE TO
	 * There are a lot of items and this is very slow, only use when you absolutely need to
	 * @return LinkedList of dbEntries
	 * @see dbEntry
	 */
	public LinkedList<dbEntry> returnAllEntries() {
		// Create a list to add the entry objects to
		LinkedList<dbEntry> entryList = new LinkedList<>();

		try {
			//create and execute the statement
			Statement statement = dbConn.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from `inv`.`inventory`");

			// In this case there should only ever be one as the IDs are set to be unique
			// TODO: 8/28/2020 Make this more robust and catch when there is more than one item
			while (resultSet.next()) {
				String id = resultSet.getString("product_id");
				int quantity = resultSet.getInt("quantity");
				double whole = resultSet.getDouble("wholesale_cost");
				double sale = resultSet.getInt("sale_price");
				String supplier = resultSet.getString("supplier_id");

				// Create and return the entry object
				entryList.add(new dbEntry(id, quantity, whole, sale, supplier));
			}
			return entryList;
			//			return entryList.toArray(new dbEntry[entryList.size()]);
		} catch (Exception ex) {
			// Print out the reason and return null
			System.out.println(ex.toString());
			return null;
		}
	}
}
