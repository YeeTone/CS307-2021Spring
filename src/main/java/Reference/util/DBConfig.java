package Reference.util;

import cn.edu.sustech.cs307.factory.ServiceFactory;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class DBConfig {
    private static final Properties properties=new Properties();
    private static ServiceFactory serviceFactoryInstance;
    private static String jdbcUrl;
    private static String username;
    private static String password;

    static {
        try {
            properties.load(new FileInputStream("config.properties"));
        }catch (Exception e){
            e.printStackTrace();
        }

        initializeAll();
    }

    private static void initializeAll(){
        try {
            serviceFactoryInstance=(ServiceFactory) Class.forName(properties.getProperty("serviceFactory"))
                    .getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            serviceFactoryInstance=null;
        }

        try {
            jdbcUrl=properties.getProperty("jdbcUrl");
        } catch (Exception e) {
            e.printStackTrace();
            jdbcUrl=null;
        }

        try {
            username=properties.getProperty("username");
        } catch (Exception e) {
            e.printStackTrace();
            username=null;
        }

        try {
            password=properties.getProperty("password");
        } catch (Exception e) {
            e.printStackTrace();
            password=null;
        }
    }

    public static ServiceFactory getServiceFactory() {
        return serviceFactoryInstance;
    }

    public static String getJdbcUrl(){
        return jdbcUrl;
    }

    public static String getSQLUsername(){
        return username;
    }

    public static String getSQLPassword(){
        return password;
    }
}
