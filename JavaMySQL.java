import java.sql.*;

public class JavaMySQL {
    public static void main(String[] args) {
        String url = "";
        String username = "";
        String password = "";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);

            String sql = "INSERT INTO orders (hashemail, custname) VALUES (aes_encrypt(?, 'key'), ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "JohnDoe@fake.com" );
            statement.setString(2, "John Doe");

            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("A row has been inserted to orders");
            }

            String sql2 = "INSERT INTO emails (hashemail, email) VALUES (aes_encrypt(?, 'key'), ?)";
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            statement2.setString(1, "JohnDoe@fake.com" );
            statement2.setString(2, "JohnDoe@fake.com");

            int rows2 = statement2.executeUpdate();
            if (rows2 > 0) {
                System.out.println("A row has been inserted to emails");
            }

            statement.close();
            statement2.close();
            connection.close();
            
        } catch (SQLException ex) {
            System.out.println("Oops, error!");
            ex.printStackTrace();
        }
    }
}
