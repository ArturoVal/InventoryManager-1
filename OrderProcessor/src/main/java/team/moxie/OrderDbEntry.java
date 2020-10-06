package team.moxie;

import java.util.Date;

public class OrderDbEntry {
  private Date date;
  private String email;
  private String location;
  private String productID;
  private int quantity;
  private String status;

  private String[] statuses = { "ordered", "processing", "complete" };


  public OrderDbEntry(
          Date date,
          String email,
          String shippingAddress,
          String productID,
          int quantity,
          String status) {
    this.date = date;
    this.email = email;
    this.location = shippingAddress;
    this.productID = productID;
    this.quantity = quantity;
    this.status = status;
  }

  public OrderDbEntry(
          Date date,
          String email,
          String shippingAddress,
          String productID,
          int quantity) {
    this.date = date;
    this.email = email;
    this.location = shippingAddress;
    this.productID = productID;
    this.quantity = quantity;
    this.status = statuses[1];
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getProductID() {
    return productID;
  }

  public void setProductID(String productID) {
    this.productID = productID;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
  
  public String getStatus(){return status;}
  
  public void setStatus(String status){ this.status = status;}

  public String toString() {
    return (
      "Data Base Order Entry\n" +
      "Purchase Date: " +
      date +
      "\n" +
      "Email Address: " +
      email +
      "\n" +
      "Shipping Address: " +
              location +
      "\n" +
      "Product ID: " +
      productID +
      "\n" +
      "Quantity: " +
      quantity +
      "\n" +
      "Status: " +
      status +
      "\n"
    );
  }
  
}
