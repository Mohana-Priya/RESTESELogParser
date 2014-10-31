package com.oraclecorp.ese.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataCon
{
	public static Connection con;
	public static Statement st;	
	
	static {
		try{
			Class.forName("oracle.jdbc.OracleDriver");						
		}catch(Exception e){			
			e.printStackTrace();
		}							
	}
	
	public static Connection getConnnection() throws SQLException{
		return DriverManager.getConnection(System.getenv().get("ESELOGPARSER_DB_URL"),System.getenv().get("ESELOGPARSER_DB_UNAME"),System.getenv().get("ESELOGPARSER_DB_PWD"));
	}
}