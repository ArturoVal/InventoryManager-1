package team.moxie;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InventoryGUI {
	private DbDriver driver;

	public InventoryGUI() {
		//////////////////////////////////////////////////////////////////////
		// This section is just setting up the elements of the GUI
		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		// Create the Panel that everything will be put in

		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());

		//////////////////////////////////////////////////////////////////////
		// Create the model for the table (this is represents the data in it)

		final DefaultTableModel tableModel = new DefaultTableModel() {

			@Override
			public Class getColumnClass(int column) {
				switch (column) {
					case 1:
						return Integer.class;
					case 2:
					case 3:
						return Double.class;
					default:
						return String.class;
				}
			}
		};

		//////////////////////////////////////////////////////////////////////
		// Create the table

		JTable table = new JTable(tableModel);

		tableModel.addColumn("ID");
		tableModel.addColumn("QUANTITY");
		tableModel.addColumn("WHOLESALE");
		tableModel.addColumn("SALE");
		tableModel.addColumn("SUPPLIER");
		table.setAutoCreateRowSorter(true);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		//////////////////////////////////////////////////////////////////////
		// Sizing of the table

		table.getColumnModel().getColumn(0).setPreferredWidth(120);
		table.getColumnModel().getColumn(1).setPreferredWidth(120);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(120);

		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);

		table.getColumnModel().getColumn(0).setMaxWidth(300);
		table.getColumnModel().getColumn(1).setMaxWidth(300);
		table.getColumnModel().getColumn(2).setMaxWidth(300);
		table.getColumnModel().getColumn(3).setMaxWidth(300);
		table.getColumnModel().getColumn(4).setMaxWidth(300);

		//////////////////////////////////////////////////////////////////////
		// Create the "connected" indicator

		JFormattedTextField NOTCONNECTEDFormattedTextField = new JFormattedTextField();
		NOTCONNECTEDFormattedTextField.setBackground(new Color(-65535));
		NOTCONNECTEDFormattedTextField.setColumns(20);
		NOTCONNECTEDFormattedTextField.setForeground(new Color(-16777216));
		NOTCONNECTEDFormattedTextField.setHorizontalAlignment(0);
		NOTCONNECTEDFormattedTextField.setText("NOT CONNECTED");

		//////////////////////////////////////////////////////////////////////
		// Create all the buttons

		JButton createButton = new JButton();
		createButton.setText("Create Entry");

		JButton searchButton = new JButton();
		searchButton.setText("Search ID");

		JButton searchSuppButton = new JButton();
		searchSuppButton.setText("Search Supplier");

		JButton updateButton = new JButton();
		updateButton.setText("Update Entry");

		JButton deleteButton = new JButton();
		deleteButton.setText("Delete Entry");

		//////////////////////////////////////////////////////////////////////
		// Place all the buttons

		JPanel east = new JPanel();
		GridLayout gridLayout = new GridLayout(5, 1);
		gridLayout.setHgap(10);
		gridLayout.setVgap(10);
		east.setLayout(gridLayout);
		east.add(searchButton);
		east.add(searchSuppButton);
		east.add(updateButton);
		east.add(createButton);
		east.add(deleteButton);

		//////////////////////////////////////////////////////////////////////
		// Setting up the menu bar

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		//JMenu calcMenu = new JMenu();

		//// calc menu stuff
		// TODO: 9/9/2020 create a calculation menu that will perform calculations on the current table data

		//// file menu stuff
		// exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(event -> System.exit(0));

		// reload

		JMenuItem refreshItem = new JMenuItem("Refresh DB");
		refreshItem.addActionListener(
			event -> {
				NOTCONNECTEDFormattedTextField.setBackground(new Color(-65535));
				driver = null;
				try {
					driver = new DbDriver("50.116.26.153", "3306", "inv", "team", "GJ&8YahAh%kS#i");
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
				NOTCONNECTEDFormattedTextField.setBackground(new Color(0x65FF5A));
			}
		);

		JMenuItem loadAll = new JMenuItem("Load All");
		loadAll.addActionListener(
			event -> {
				JPanel getInfoPanel = new JPanel();
				getInfoPanel.add(new JLabel("This operation may be slow."));

				int result1 = JOptionPane.showConfirmDialog(null, getInfoPanel, "WARNING", JOptionPane.OK_CANCEL_OPTION);
				if (result1 != JOptionPane.OK_OPTION) {
					return;
				}

				LinkedList<dbEntry> list = driver.returnAllEntries();

				// Clear the rows
				displayTable(tableModel, list);
			}
		);

		fileMenu.add(loadAll);
		fileMenu.add(refreshItem);
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);

		//////////////////////////////////////////////////////////////////////
		// Build the main panel and add the stuff to it

		JPanel north = new JPanel(new FlowLayout());
		north.add(NOTCONNECTEDFormattedTextField);

		JScrollPane center = new JScrollPane(table);
		center.setPreferredSize(new Dimension(500, 400));
		center.createHorizontalScrollBar();

		panel1.add(east, BorderLayout.EAST);
		panel1.add(center, BorderLayout.CENTER);
		panel1.add(north, BorderLayout.NORTH);

		//////////////////////////////////////////////////////////////////////
		// Created the actual window and add everything to it

		JFrame frame = new JFrame("Inventory Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1000, 450));
		frame.setLocationRelativeTo(null);
		frame.add(panel1);
		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setVisible(true);

		//////////////////////////////////////////////////////////////////////
		// Setting up the database

		// try to connect to the database
		String dir = System.getProperty("user.dir");
		System.out.println("Reading properties from: " + dir + "\\InventoryManager\\target\\classes\\config.properties");
		InputStream tmpFile;
		try {
			tmpFile = new FileInputStream(dir+"\\InventoryManager\\target\\classes\\config.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		Properties props = new Properties();
		try {
			props.load(tmpFile);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			driver = new DbDriver(
					props.getProperty("ip"),
					props.getProperty("port"),
					props.getProperty("dbname"),
					props.getProperty("username"),
					props.getProperty("pass")
			);
			NOTCONNECTEDFormattedTextField.setValue("CONNECTED");
			NOTCONNECTEDFormattedTextField.setBackground(new Color(0x65FF5A));
		} catch (Exception e) {
			e.printStackTrace();
		} // Try Catch DbDriver

		//////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		// The rest of this is where stuff is actually done.				//
		//////////////////////////////////////////////////////////////////////

		// TODO: 9/9/2020 Create a way for performing custom searches of the database

		searchButton.addActionListener(
			e -> {
				String PID;

				PID = IdDialog("Product ID", "Enter the ID of the Product");

				LinkedList<dbEntry> list = new LinkedList<>();
				if (PID == null) return;

				dbEntry entry = driver.searchById(PID);
				if (entry == null) {
					showConfirmationPopup("Invalid input entered.");
					return;
				}

				list.add(driver.searchById(PID));
				// display the info on the table
				displayTable(tableModel, list);
			}
		); // searchButton.addActionListener

		searchSuppButton.addActionListener(
			e -> {
				String supplierID;

				// Get the supplier ID with a dialog
				supplierID = IdDialog("Supplier ID", "Enter the ID of the Supplier");
				if (supplierID == null) return;

				// Put the results into a list
				LinkedList<dbEntry> list = driver.searchBySupplier(supplierID);

				// Display in the table
				displayTable(tableModel, list);
			}
		); // searchSuppButton.addActionListener

		updateButton.addActionListener(
			e -> {
				String PID;

				PID = IdDialog("Product ID", "Enter the ID of the Product");
				if (PID == null) return;

				dbEntry selectedEntry = driver.searchById(PID);

				// Show the values that are already there
				JTextField productIDField2 = new JTextField(selectedEntry.getId(), 20);
				JTextField quantityField = new JTextField(Integer.toString(selectedEntry.getQuantity()), 20);
				JTextField wholesalePriceField = new JTextField((Double.toString(selectedEntry.getWholesalePrice())), 20);
				JTextField salePriceField = new JTextField((Double.toString(selectedEntry.getSalePrice())), 20);
				JTextField supplierIDField = new JTextField(selectedEntry.getSupplierId(), 20);

				JPanel myPanel = new JPanel();
				myPanel.add(new JLabel("Product ID"));
				myPanel.add(productIDField2);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Quantity"));
				myPanel.add(quantityField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Wholesale Price"));
				myPanel.add(wholesalePriceField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Sale Price"));
				myPanel.add(salePriceField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Supplier ID"));
				myPanel.add(supplierIDField);

				int result = JOptionPane.showConfirmDialog(
					null,
					myPanel,
					"Please Enter row entry info:",
					JOptionPane.OK_CANCEL_OPTION
				);

				if (result == JOptionPane.OK_OPTION) {
					String productID = productIDField2.getText();
					int quantity;
					double wholesale;
					double sale;
					String supplier;

					try {
						quantity = Integer.parseInt(quantityField.getText());
						wholesale = Double.parseDouble(wholesalePriceField.getText());
						sale = Double.parseDouble(salePriceField.getText());
						supplier = supplierIDField.getText();
					} catch (Exception thrown) {
						showConfirmationPopup("Invalid values entered.");
						return;
					} // Try Catch

					boolean successful = driver.updateEntry(productID, quantity, wholesale, sale, supplier);

					if (!successful) {
						showConfirmationPopup("Invalid values entered.");
						return;
					} // if not successful

					showConfirmationPopup("The entry has been updated.");
					LinkedList<dbEntry> list = new LinkedList<>();

					// Display the newly updated entry
					list.add(driver.searchById(productID));

					// Clear the rows
					displayTable(tableModel, list);
				} // If OK_OPTION
			} // lambda e
		); // updateButton.addActionListener

		deleteButton.addActionListener(
			e -> {
				String PID;

				PID = IdDialog("Product ID", "Enter the ID of the Product");
				if (PID == null) return;

				String supplierID = driver.searchById(PID).getSupplierId();

				driver.deleteEntry(PID);

				showConfirmationPopup("The entry has been deleted.");

				//// Now show the changes by reloading the supplier search
				LinkedList<dbEntry> list = driver.searchBySupplier(supplierID);

				// Clear the rows
				displayTable(tableModel, list);
			} // lambda e
		); // deleteButton.addActionListener

		createButton.addActionListener(
			e -> {
				JTextField productIDField = new JTextField(20);
				JTextField quantityField = new JTextField(20);
				JTextField wholesalePriceField = new JTextField(20);
				JTextField salePriceField = new JTextField(20);
				JTextField supplierIDField = new JTextField(20);

				JPanel myPanel = new JPanel();
				myPanel.add(new JLabel("Product ID"));
				myPanel.add(productIDField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Quantity"));
				myPanel.add(quantityField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Wholesale Price"));
				myPanel.add(wholesalePriceField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Sale Price"));
				myPanel.add(salePriceField);

				myPanel.add(Box.createHorizontalStrut(5)); // a spacer
				myPanel.add(new JLabel("Supplier ID"));
				myPanel.add(supplierIDField);

				int result = JOptionPane.showConfirmDialog(
					null,
					myPanel,
					"Please Enter row entry info:",
					JOptionPane.OK_CANCEL_OPTION
				);
				if (result == JOptionPane.OK_OPTION) {
					String productID = productIDField.getText();

					boolean success = driver.createEntry(
						productID,
						Integer.parseInt(quantityField.getText()),
						Double.parseDouble(wholesalePriceField.getText()),
						Double.parseDouble(salePriceField.getText()),
						supplierIDField.getText()
					);

					if (!success) {
						showConfirmationPopup("Invalid input entered.");
						return;
					}

					showConfirmationPopup("The entry has been created.");

					LinkedList<dbEntry> list = new LinkedList<>();

					// Display the newly created entry
					list.add(driver.searchById(productID));
					displayTable(tableModel, list);
				} // If result
			} // lambda e
		); // createButton.addActionListener
	} // InventoryGUI - Constructor

	private String IdDialog(String title, String msg) {
		String supplierID;
		JTextField productIDField = new JTextField(20);

		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel(title));
		myPanel.add(productIDField);

		int result = JOptionPane.showConfirmDialog(null, myPanel, msg, JOptionPane.OK_CANCEL_OPTION);

		if (result == JOptionPane.OK_OPTION) {
			supplierID = productIDField.getText();
		} else return null;
		return supplierID;
	} // supplierIdDialog


	private void showConfirmationPopup(String msg) {
		JPanel confirmWindow = new JPanel();
		confirmWindow.add(new JLabel(msg));

		JOptionPane.showConfirmDialog(null, confirmWindow, "Info", JOptionPane.OK_CANCEL_OPTION);
	} // showConfirmationPopup

	// This changes what is displayed in the table
	private void displayTable(DefaultTableModel tableModel, LinkedList<dbEntry> list) {
		// Clear the rows
		int rowCount = tableModel.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--) {
			tableModel.removeRow(i);
		}

		// Add the rows back
		for (dbEntry entry : list) {
			tableModel.addRow(
				new Object[] {
					entry.getId(),
					entry.getQuantity(),
					entry.getWholesalePrice(),
					entry.getSalePrice(),
					entry.getSupplierId()
				} // New object[]
			);
		} // for loop - entry list
	} // displayTable
} // InventoryGUI Class
