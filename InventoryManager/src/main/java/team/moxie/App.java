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

/**
 * @author Dustin
 * @author Rey
 * @author Shawn
 * @author Edom
 * @author Cody
 * @author Arturo
 */
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

			try {
				FlatDarculaLaf.install();
				InventoryGUI gui = new InventoryGUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException throwables) {
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
