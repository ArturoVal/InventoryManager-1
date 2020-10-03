package team.moxie;

import com.formdev.flatlaf.FlatDarculaLaf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.*;

public class App {

	public static void main(String[] args) {
		Properties props = getProperties("\\InventoryManager\\target\\classes\\config.properties");

		try {


			assert props != null;
			DbDriver driver = new DbDriver(
					props.getProperty("ip"),
					props.getProperty("port"),
					props.getProperty("dbname"),
					props.getProperty("username"),
					props.getProperty("pass")
			);

			System.out.println("Connection successful.");
			System.out.println("Running tests!");

			System.out.println("Searching for the ID: 2FT57YS7CM97");
			System.out.println(driver.searchById("2FT57YS7CM97"));
			System.out.println();

			//			dbEntry[] array = driver.searchBySupplier("HVFCRLHL");
			//			for (dbEntry entry : array) {
			//				System.out.println(entry);
			//			}
			//			System.out.println();

			System.out.println("Updating entry with ID: 2FT57YS7CM97");
			boolean r = driver.updateEntry("2FT57YS7CM97", 111, 111.0, 111.0, "YTCMSBPA");
			if (r) {
				System.out.println("Successful\n");
			} else System.out.println("Failed\n");

			System.out.println("Searching for the ID: 2FT57YS7CM97");
			System.out.println(driver.searchById("2FT57YS7CM97"));
			System.out.println();

			System.out.println("Deleting the ID: 2FT57YS7CM97");
			r = driver.deleteEntry("2FT57YS7CM97");
			if (r) {
				System.out.println("Successful\n");
			} else System.out.println("Failed\n");

			System.out.println("Searching for the ID: 2FT57YS7CM97");
			System.out.println(driver.searchById("2FT57YS7CM97"));
			System.out.println();

			System.out.println("Creating an entry for the ID: 2FT57YS7CM97");
			r = driver.createEntry("2FT57YS7CM97", 112, 112.0, 112.0, "YTCMSBPA");
			if (r) {
				System.out.println("Successful\n");
			} else System.out.println("Failed\n");

			System.out.println("Searching for the ID: 2FT57YS7CM97");
			System.out.println(driver.searchById("2FT57YS7CM97"));
			System.out.println();

			System.out.println("Tests have completed.");

			//Added by Shawn, Cody - option 1
			Scanner sysIn = new Scanner(System.in);
			System.out.println("Database Manager.\nPress 1 for Supplier.\nPress 2 for Buyer");
			int test = sysIn.nextInt();
			switch (test) {
				case 1:
					//Supplier();
				case 2:
					//Buyer();
					Scanner prodIn = new Scanner(System.in);
					System.out.println("Welcome to database Buyer Event.\n");
					System.out.println("Enter Product ID");
					String ID = prodIn.nextLine();
					dbEntry product = driver.searchById(ID);
					System.out.println("You've chosen: " + product);
					boolean a;
					do {
						a = true;
						System.out.println("Enter quantity to buy:");
						int quantity = prodIn.nextInt();
						if (quantity > product.getQuantity() || quantity < 0) {
							a = false;
							System.out.println("Reenter quantity!!!!");
						}
						else{
							product.setQuantity(product.getQuantity() - quantity);
						}
					} while ( !a );
					System.out.println("Product update: " + product);
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		try {
			FlatDarculaLaf.install();
			InventoryGUI gui = new InventoryGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void userInterface() {
		String userInput = "";
		String userIDInput;
		String[] userInputOptions = { "1", "2", "3", "4", "5", "Q" };
		String intro = "Welcome to Inventory Manager.\n";
		String optionQuestion = "What would you like to do?\n";
		String optionStatementOne = "Enter 1 to search by inventory ID\n";
		String optionStatementTwo = "Enter 2 to search by supplier ID\n";
		String optionStatementThree = "Enter 3 to update an inventory entry\n";
		String optionStatementFour = "Enter 4 to delete an inventory entry\n";
		String optionStatementFive = "Enter 5 to create an inventory entry\n";
		String optionStatementExit = "Enter quit to exit.";
		String optionsStatement =
			optionQuestion +
			optionStatementOne +
			optionStatementTwo +
			optionStatementThree +
			optionStatementFour +
			optionStatementFive +
			optionStatementExit;
		try {
			DbDriver testDriver = new DbDriver("50.116.26.153", "3306", "inv", "team", "GJ&8YahAh%kS#i");
			while (!Arrays.asList(userInputOptions).contains(userInput)) {
				userInput =
					JOptionPane
						.showInputDialog(JOptionPane.getRootFrame(), intro + optionsStatement)
						.toUpperCase()
						.substring(0, 1);
				if (!Arrays.asList(userInputOptions).contains(userInput)) {
					JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Not a valid option!!");
				}
			}
			if (userInput.equals("Q")) JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Goodbye!");
			while (!userInput.equals("Q")) {
				switch (userInput) {
					case "1":
						userIDInput = JOptionPane.showInputDialog("Enter inventory ID: ");
						Thread.sleep(250);
						String result = testDriver.searchById(userIDInput).toString().substring(8);
						result = result.substring(0, result.length() - 1);
						JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), result);
						Thread.sleep(1000);
						break;
					case "2":
						userIDInput = JOptionPane.showInputDialog("Enter supplier ID: ");
						LinkedList supplierSearchResults = testDriver.searchBySupplier(userIDInput);
						JOptionPane.showMessageDialog(
							JOptionPane.getRootFrame(),
							"\nThere are " + supplierSearchResults.size() + " results for supplier " + userIDInput + ".\n"
						);
						Thread.sleep(1000);
						String allResults = "";
						for (int i = 0; i < supplierSearchResults.size(); i++) {
							String outResults = supplierSearchResults.get(i).toString();
							outResults = outResults.substring(0, outResults.length() - 1);
							Thread.sleep(75);
							allResults += outResults + "\n";
							System.out.println(outResults);
						}
						break;
					case "3":
						String userConfirmation = "N";
						while (userConfirmation.equals("N")) {
							userIDInput = JOptionPane.showInputDialog("Enter inventory ID to update: ").toUpperCase();
							dbEntry tempArray = testDriver.searchById(userIDInput);
							JOptionPane.showMessageDialog(
								JOptionPane.getRootFrame(),
								"Current entry for " + userIDInput + " is \n" + tempArray
							);
							int quantity = Integer.parseInt(
								JOptionPane.showInputDialog(
									"Current quantity is " + tempArray.getQuantity() + "\n" + " Enter quantity: "
								)
							);
							double wholeSalePrice = Double.parseDouble(
								JOptionPane.showInputDialog(
									"Current wholesale price is " + tempArray.getWholesalePrice() + "\n" + "Enter wholesale price: "
								)
							);
							double salePrice = Double.parseDouble(
								JOptionPane.showInputDialog(
									"Current sale price is " + tempArray.getSalePrice() + "\n" + "Enter sale price: "
								)
							);
							String supplierID = JOptionPane
								.showInputDialog("Current supplier ID is " + tempArray.getSupplierId() + "\n" + "Enter supplier ID: ")
								.toUpperCase();
							JOptionPane.showMessageDialog(
								JOptionPane.getRootFrame(),
								"\nYou have chosen to update Supply ID " +
								userIDInput +
								" with the follow:\n" +
								"quantity = " +
								quantity +
								"\nwholesale price = " +
								wholeSalePrice +
								"\nsale price = " +
								salePrice +
								"\nsupplier id = " +
								supplierID
							);
							userConfirmation =
								JOptionPane.showInputDialog("Are these values correct? ").substring(0, 1).toUpperCase();
							if (userConfirmation.equals("Y")) {
								testDriver.updateEntry(userIDInput, quantity, wholeSalePrice, salePrice, supplierID);
								JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Updated supply ID " + userIDInput + ".");
								JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), testDriver.searchById(userIDInput));
							}
						}
						break;
					case "4":
						userConfirmation = "N";
						while (userConfirmation.equals("N")) {
							userIDInput = JOptionPane.showInputDialog("Enter inventory ID to delete: ").toUpperCase();
							JOptionPane.showMessageDialog(
								JOptionPane.getRootFrame(),
								"Current entry for " + userIDInput + " is \n" + testDriver.searchById(userIDInput)
							);
							userConfirmation =
								JOptionPane
									.showInputDialog("Are you sure you want to delete " + userIDInput + "? Yes or No ")
									.toUpperCase()
									.substring(0, 1);
							if (userConfirmation.equals("Y")) {
								testDriver.deleteEntry(userIDInput);
								JOptionPane.showMessageDialog(
									JOptionPane.getRootFrame(),
									"Database supply ID " + userIDInput + " has been deleted."
								);
							} else {
								JOptionPane.showMessageDialog(
									JOptionPane.getRootFrame(),
									"Database supply ID " + userIDInput + " was not deleted."
								);
								break;
							}
						}
						break;
					case "5":
						userConfirmation = "N";
						while (userConfirmation.equals("N")) {
							userIDInput = JOptionPane.showInputDialog("Enter inventory ID to create: ").toUpperCase();
							int quantity = Integer.parseInt(JOptionPane.showInputDialog(" Enter quantity: "));
							double wholeSalePrice = Double.parseDouble(JOptionPane.showInputDialog("Enter wholesale price: "));
							double salePrice = Double.parseDouble(JOptionPane.showInputDialog("Enter sale price: "));
							String supplierID = JOptionPane.showInputDialog("Enter supplier ID: ").toUpperCase();
							userConfirmation =
								JOptionPane
									.showInputDialog(
										"Inventory ID = " +
										userIDInput +
										"\nquantity = " +
										quantity +
										"\nwholesale price = " +
										wholeSalePrice +
										"\nsale price = " +
										salePrice +
										"\nsupplier id = " +
										supplierID +
										"\nAre these values correct? "
									)
									.toUpperCase()
									.substring(0, 1);
							if (userConfirmation.equals("Y")) {
								testDriver.createEntry(userIDInput, quantity, wholeSalePrice, salePrice, supplierID);
								JOptionPane.showMessageDialog(
									JOptionPane.getRootFrame(),
									"Created supply ID " + userIDInput + ".\n" + testDriver.searchById(userIDInput)
								);
							} else {
								JOptionPane.showMessageDialog(
									JOptionPane.getRootFrame(),
									"Aborted creating database entry for " + userIDInput + "."
								);
								break;
							}
						}
						break;
					case "Q":
						JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Goodbye!");
						break;
				}
				userInput = "";
				while (!Arrays.asList(userInputOptions).contains(userInput)) {
					userInput = JOptionPane.showInputDialog(optionsStatement).toUpperCase().substring(0, 1);
					if (!Arrays.asList(userInputOptions).contains(userInput)) {
						Thread.sleep(250);
						JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "\nNot a valid option!!");
						Thread.sleep(250);
					} else if (userInput.equals("Q")) JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Goodbye!");
				}
			}
		} catch (SQLException | InterruptedException throwables) {
			throwables.printStackTrace();
		}
	}

	public static Properties getProperties(String fileName) {
		String dir = System.getProperty("user.dir");
		System.out.println("Reading properties from: " + dir + fileName);
		InputStream tmpFile;
		try {
			tmpFile = new FileInputStream(dir+fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		Properties prop = new Properties();
		try {
			prop.load(tmpFile);
			return prop;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
