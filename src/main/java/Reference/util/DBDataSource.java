package Reference.util;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public class DBDataSource {

    private static final HikariDataSource dataSource=new HikariDataSource();

    static {
        configureSQLServer();
    }

    private DBDataSource(){
        throw new RuntimeException();
    }

    private static void configureSQLServer(){

        dataSource.setJdbcUrl(DBConfig.getJdbcUrl());
        dataSource.setUsername(DBConfig.getSQLUsername());
        dataSource.setPassword(DBConfig.getSQLPassword());
        dataSource.setMaximumPoolSize(16);
    }

    public static void main(String[] args) {
        Connection c=getConnection();
        close(c);

    }


    public static Connection getConnection(){
        try {
            synchronized (dataSource){
                return dataSource.getConnection();
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void close(Connection conn){
        try{
            if(conn!=null){
                conn.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void rollbackTransaction(Connection conn){
        try {
            if(conn!=null){
                conn.rollback();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void commitTransaction(Connection conn){
        try {
            if(conn!=null){
                conn.commit();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void beginTransaction(Connection conn){
        try {
            if(conn!=null){
                conn.setAutoCommit(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
