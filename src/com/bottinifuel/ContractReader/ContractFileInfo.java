/*
 * Created on Aug 19, 2010 by pladd
 *
 */
package com.bottinifuel.ContractReader;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.bottinifuel.FlatFileParser.FileFormatException;
import com.bottinifuel.FlatFileParser.FlatFile;
import com.bottinifuel.FlatFileParser.FlatFile.FileTypeEnum;
import com.bottinifuel.FlatFileParser.Records.ContractLineRecord;
import com.bottinifuel.FlatFileParser.Records.Record;
import com.bottinifuel.FlatFileParser.Records.Record.RecordTypeEnum;

public class ContractFileInfo
{
    public static Map<String, Integer> PrintContractCounts(FlatFile ff) throws FileFormatException
    {
        if (ff.FileType != FileTypeEnum.CONTRACT_FILE)
            throw new FileFormatException("Can't count contracts for non-contract filetype " + ff.FileType);
        
        SortedMap<String, Integer> contractCounts = new TreeMap<String, Integer>();

        for (Record r : ff.AllRecords)
        {
            if (r.RecordType == RecordTypeEnum.CONTRACT_LINE)
            {
                ContractLineRecord line = (ContractLineRecord)r;
                String contractCode = "" + line.Letter + line.BasePrice;
                if (!contractCounts.containsKey(contractCode))
                {
                    Integer count = new Integer(1);
                    contractCounts.put(contractCode, count);
                }
                else
                {
                    Integer count = contractCounts.get(contractCode);
                    contractCounts.put(contractCode, ++count);
                }
            }
        }

        return contractCounts;
    }
    
    public static String GetContractReferenceNumber(FlatFile ff)
    {
        if (ff.FileType != FileTypeEnum.CONTRACT_FILE)
        {
            System.out.println("Can't process reference number for non-contract filetype " + ff.FileType);
            return "";
        }

        String refNum = null;
        for (Record r : ff.AllRecords)
        {
            if (r.RecordType == RecordTypeEnum.CONTRACT_LINE)
            {
                ContractLineRecord line = (ContractLineRecord)r;
                if (refNum == null)
                    refNum = line.RefNum;
                else if (!refNum.equals(line.RefNum))
                {
                    System.out.println("Warning: inconsistent record number in file:");
                    System.out.println("\tFirst found: " + refNum);
                    System.out.println("\tAlso found: " + line.RefNum + " at line " + line.RecordNum);
                }
            }
        }
        
        if (refNum == null)
            return "";
        else
            return refNum;
    }
}
