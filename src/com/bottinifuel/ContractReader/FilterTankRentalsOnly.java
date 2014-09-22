/*
 * Created on Jun 14, 2010 by pladd
 *
 */
package com.bottinifuel.ContractReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bottinifuel.FlatFileParser.FileFormatException;
import com.bottinifuel.FlatFileParser.FlatFile;
import com.bottinifuel.FlatFileParser.Records.ContractLineRecord;
import com.bottinifuel.FlatFileParser.Records.DocumentRecord;
import com.bottinifuel.FlatFileParser.Records.Record;
import com.bottinifuel.FlatFileParser.Records.Record.RecordTypeEnum;

/**
 * @author pladd
 *
 */
public class FilterTankRentalsOnly
{
    public static void fixIt(FlatFile ff, boolean tankRentalsOnly) throws FileFormatException
    {
        List<DocumentRecord> removeStatements = new ArrayList<DocumentRecord>();
        
        for (DocumentRecord statement : ff.Documents)
        {
            boolean hasTankRental = false;
            boolean hasContract = false;

            for (Record r : statement.Records)
            {
                if (r.RecordType == RecordTypeEnum.CONTRACT_LINE)
                {
                    ContractLineRecord contract = (ContractLineRecord)r;
                    if (contract.Letter == 'H')
                        hasTankRental = true;
                    else
                        hasContract = true;
                }
            }
            
            if (!hasTankRental && !hasContract)
                throw new FileFormatException(statement.RecordNum, "Contract with no contract lines!");

            if (tankRentalsOnly && hasContract)
                removeStatements.add(statement);
            if (!tankRentalsOnly && hasTankRental && !hasContract)
                removeStatements.add(statement);
        }
        
        ff.AllRecords.removeAll(removeStatements);
        ff.Documents.removeAll(removeStatements);
        BigDecimal totalRemoved = new BigDecimal(0);
        for (DocumentRecord statement : removeStatements)
        {
            ff.AllRecords.removeAll(statement.Records);
            totalRemoved = totalRemoved.add(statement.getTotalDue());
        }
        
        ff.Trailer.setFileDateTime(new Date());
        ff.Trailer.setTotalItems(ff.Documents.size());
        ff.Trailer.setTotalAmountBilled(ff.Trailer.TotalAmountBilled.subtract(totalRemoved));
    }
}
