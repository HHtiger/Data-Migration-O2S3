package com.tiger.mapper.oldDb;

import com.tiger.model.BRyzp;
import com.tiger.model.BZaFjxx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * Package: com.tiger.mapper.newDb
 * ClassName: BatchInsertBuilder
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/6
 * Version: 1.0
 */
public class BatchInsertBuilder {

    private static Logger logger = LoggerFactory.getLogger(BatchInsertBuilder.class);

    public String buildInsertBRyzp(List<BRyzp> list){

        StringBuilder sql = new StringBuilder("insert all ");
        MessageFormat messageFormat = new MessageFormat("into B_RYZP (SYSTEMID, PHOTO) values (#'{'list[{0,number,#}].systemid},#'{'list[{0,number,#}].photo}) ");
        for (int i = 0; i < list.size(); i++) {
            sql.append(messageFormat.format(new Integer[]{i}));
        }
        sql.append("select 1 from dual");
        return sql.toString();
    }

    public String buildInsertBZaFjxx(List<BZaFjxx> list){

        StringBuilder sql = new StringBuilder("insert all ");
        MessageFormat messageFormat = new MessageFormat("into B_ZA_FJXX (SYSTEMID, FJNR) values (#'{'list[{0,number,#}].systemid},#'{'list[{0,number,#}].fjnr}) ");
        for (int i = 0; i < list.size(); i++) {
            sql.append(messageFormat.format(new Integer[]{i}));
        }
        sql.append("select 1 from dual");
        return sql.toString();
    }

}
