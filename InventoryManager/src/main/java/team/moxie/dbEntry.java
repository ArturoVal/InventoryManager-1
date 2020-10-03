package team.moxie;

/**
 * An object representing a single row from the inventory database
 *
 * @author Dustin
 */
public class dbEntry {
	private final String id;
	private  int quantity;
	private final double wholesalePrice;
	private final double salePrice;
	private final String supplierId;

	/**
	 * Constructor for the entry object
	 *
	 * @param id             Product ID
	 * @param quantity       Quantity of the product
	 * @param wholesalePrice Wholesale price of the product
	 * @param salePrice      Sale price of the product
	 * @param supplierId     ID of the supplier for the item
	 */
	public dbEntry(String id, int quantity, double wholesalePrice, double salePrice, String supplierId) {
		this.id = id;
		this.quantity = quantity;
		this.wholesalePrice = wholesalePrice;
		this.salePrice = salePrice;
		this.supplierId = supplierId;
	}

	/**
	 * Getter for id
	 *
	 * @return The ID of the entry
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter for quantity
	 *
	 * @return The quantity of the entry
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Getter for wholesalePrice
	 *
	 * @return The wholesale price of the entry
	 */
	public void setQuantity(int quantity){
		this.quantity = quantity;
	}
	public double getWholesalePrice() {
		return wholesalePrice;
	}

	/**
	 * Getter for salePrice
	 *
	 * @return The sale price of the entry
	 */
	public double getSalePrice() {
		return salePrice;
	}

	/**
	 * Getter for supplier ID
	 *
	 * @return The supplier ID of the entry
	 */
	public String getSupplierId() {
		return supplierId;
	}

	/**
	 * Override to create a string for the entry
	 *
	 * @return The generated string of the entry
	 */
	@Override
	public String toString() {
		return (
			"Data Base Entry\n" +
			"Id='" +
			id +
			'\'' +
			"\nQuantity=" +
			quantity +
			"\nWholesalePrice=" +
			wholesalePrice +
			"\nSalePrice=" +
			salePrice +
			"\nSupplierId='" +
			supplierId +
			'\''

		);
	}
}
