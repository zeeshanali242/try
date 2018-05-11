package controllers.KycUserController;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import controllers.KycUserController.DbConfigBeans;

public class DbProperty {
	public DbConfigBeans getDbConnString() throws FileNotFoundException {
		// FileReader reader = null;
		InputStream inputStream;
		Properties prop = null;
		DbConfigBeans dbConfigBeans = null;
		try {

			prop = new Properties();
			inputStream = getClass().getClassLoader().getResourceAsStream("db_config.properties");
			dbConfigBeans = new DbConfigBeans();
			prop.load(inputStream);
			dbConfigBeans.setDriverManager(prop.getProperty("driverManager"));
			dbConfigBeans.setDbUrl(prop.getProperty("dbUrl"));
			dbConfigBeans.setDbUsername(prop.getProperty("dbUsername"));
			dbConfigBeans.setDbPassword(prop.getProperty("dbPassword"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dbConfigBeans;

	}
}
