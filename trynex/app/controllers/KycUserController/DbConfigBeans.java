package controllers.KycUserController;

public class DbConfigBeans {
	private String driverManager;
	private String dbUrl;
	private String dbUsername;
	private String dbPassword;

	public String getDriverManager() {
		return driverManager;
	}

	public void setDriverManager(String driverManager) {
		this.driverManager = driverManager;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUsername() {
		return dbUsername;
	}

	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

}
