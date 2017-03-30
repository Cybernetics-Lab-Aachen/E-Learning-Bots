import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONObject;

import com.mysql.jdbc.PreparedStatement;

public class Test {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "");
		Statement statement = connection.createStatement();

		PreparedStatement sql = (PreparedStatement) connection
				.prepareStatement("INSERT INTO visits(visits) VALUES(?)");
		sql.setInt(1, 1);
		sql.executeUpdate();
	}
}
