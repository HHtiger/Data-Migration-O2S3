package com.tiger.config.datasource.filter;

import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.proxy.jdbc.CallableStatementProxy;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;

import java.util.ArrayList;

/**
 * Package: com.tiger.config.datasource.filter
 * ClassName: MyDruidLogFilter
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/6
 * Version: 1.0
 */
public class MyDruidLogFilter extends Slf4jLogFilter {

    private boolean showExecutTime = true;

    public boolean isShowExecutTime() {
        return showExecutTime;
    }

    public void setShowExecutTime(boolean showExecutTime) {
        this.showExecutTime = showExecutTime;
    }

    private String stmtId(StatementProxy statement) {
        StringBuffer buf = new StringBuffer();
        if(statement instanceof CallableStatementProxy) {
            buf.append("cstmt-");
        } else if(statement instanceof PreparedStatementProxy) {
            buf.append("pstmt-");
        } else {
            buf.append("stmt-");
        }

        buf.append(statement.getId());
        return buf.toString();
    }

    private void logExecutableSql(StatementProxy statement, String sql) {
        if(this.isStatementExecutableSqlLogEnable()) {
            int parametersSize = statement.getParametersSize();
            if(parametersSize == 0) {
                this.statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + this.stmtId(statement) + "} executed. \n" + sql);
            } else {
                ArrayList parameters = new ArrayList(parametersSize);

                for(int dbType = 0; dbType < parametersSize; ++dbType) {
                    JdbcParameter formattedSql = statement.getParameter(dbType);
                    parameters.add(formattedSql.getValue());
                }

                String var7 = statement.getConnectionProxy().getDirectDataSource().getDbType();
                String var8 = SQLUtils.format(sql, var7, parameters);
                this.statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + this.stmtId(statement) + "} executed. \n" + var8);
            }
        }
    }

    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
        this.logExecutableSql(statement, sql);
        if(showExecutTime){
            statement.setLastExecuteTimeNano();
            double nanos = (double)statement.getLastExecuteTimeNano();
            double millis = nanos / 1000000.0D;
            this.statementLog("{conn-" + statement.getConnectionProxy().getId() + ", " + this.stmtId(statement) + "} executed. " + millis + " millis.");
        }
    }
}
