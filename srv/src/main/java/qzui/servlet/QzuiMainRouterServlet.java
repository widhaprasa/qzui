package qzui.servlet;

import restx.servlet.RestxMainRouterServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class QzuiMainRouterServlet extends RestxMainRouterServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {

        String enable = System.getProperty("qzui.jobstore.enabled", "false");
        if (!"true".equals(enable)) {
            super.init(config);
            return;
        }

        // Mysql properties
        String mysqlHost = System.getProperty("qzui.jobstore.mysql.host", "localhost");
        int mysqlPort = 3306;
        try {
            mysqlPort = Integer.parseInt(System.getProperty("qzui.jobstore.mysql.port", "3306"));
        } catch (NumberFormatException ignored) {
        }
        String mysqlDb = System.getProperty("qzui.jobstore.mysql.db", "quartz");
        String mysqlUsername = System.getProperty("qzui.jobstore.mysql.user", "quartz");
        String mysqlPassword = System.getProperty("qzui.jobstore.mysql.password", "quartz");

        // JobStore properties
        System.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        System.setProperty("org.quartz.jobStore.driverDelegateClass",
                "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        System.setProperty("org.quartz.jobStore.dataSource", "qzui");
        System.setProperty("org.quartz.dataSource.qzui.driver", "com.mysql.jdbc.Driver");
        System.setProperty("org.quartz.dataSource.qzui.URL", "jdbc:mysql://" + mysqlHost + ':' +
                mysqlPort + '/' + mysqlDb);
        if (!mysqlUsername.isEmpty()) {
            System.setProperty("org.quartz.dataSource.qzui.user", mysqlUsername);
            if (!mysqlPassword.isEmpty()) {
                System.setProperty("org.quartz.dataSource.qzui.password", mysqlPassword);
            }
        }

        super.init(config);
    }
}
