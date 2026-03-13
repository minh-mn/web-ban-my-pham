import com.mycosmeticshop.utils.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDBConnection {

    public static void main(String[] args) {

        try (Connection conn = DBConnection.getConnection()) {

            System.out.println("✅ CONNECTED: " + conn);

            // Test query đơn giản
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM store_product");

            if (rs.next()) {
                System.out.println("📦 Total products = " + rs.getInt(1));
            }

            System.out.println("✅ DB TEST PASSED");

        } catch (Exception e) {
            System.err.println("❌ DB TEST FAILED");
            e.printStackTrace();
        }
    }
}
