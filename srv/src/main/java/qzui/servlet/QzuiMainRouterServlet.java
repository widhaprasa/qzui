package qzui.servlet;

import restx.servlet.RestxMainRouterServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class QzuiMainRouterServlet extends RestxMainRouterServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {

        // JobStore properties
        String jobStoreEnable = System.getProperty("qzui.jobStore.enabled", "false");
        if ("true".equals(jobStoreEnable)) {
            String jobStoreMysqlHost = System.getProperty("qzui.jobStore.mysql.host", "localhost");
            int jobStoreMysqlPort = 3306;
            try {
                jobStoreMysqlPort = Integer.parseInt(System.getProperty("qzui.jobStore.mysql.port", "3306"));
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }
            String jobStoreMysqlDatabase = System.getProperty("qzui.jobStore.mysql.db", "quartz");
            String jobStoreMysqlUsername = System.getProperty("qzui.jobStore.mysql.user", "quartz");
            String jobStoreMysqlPassword = System.getProperty("qzui.jobStore.mysql.password", "quartz");

            System.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
            System.setProperty("org.quartz.jobStore.driverDelegateClass",
                    "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
            System.setProperty("org.quartz.jobStore.dataSource", "qzui");
            System.setProperty("org.quartz.dataSource.qzui.driver", "com.mysql.jdbc.Driver");
            System.setProperty("org.quartz.dataSource.qzui.URL", "jdbc:mysql://" + jobStoreMysqlHost + ':' +
                    jobStoreMysqlPort + '/' + jobStoreMysqlDatabase);
            if (!jobStoreMysqlUsername.isEmpty()) {
                System.setProperty("org.quartz.dataSource.qzui.user", jobStoreMysqlUsername);
                if (!jobStoreMysqlPassword.isEmpty()) {
                    System.setProperty("org.quartz.dataSource.qzui.password", jobStoreMysqlPassword);
                }
            }
        }
        super.init(config);
    }
}
