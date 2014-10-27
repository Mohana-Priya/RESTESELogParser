package com.oraclecorp.ese.util;

import java.sql.*;

public class DataCon
{
	public static Connection con;
	public static Statement st;	
	
	public static void loadDriver(){
		try{
			Class.forName("oracle.jdbc.OracleDriver");						
		}catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}							
	}
	
	public static Connection getConnnection() throws SQLException{
		return DriverManager.getConnection(System.getenv().get("IFARMMONITORING_DB_URL"),System.getenv().get("IFARMMONITORING_DB_UNAME"),System.getenv().get("IFARMMONITORING_DB_PWD"));
	}
}