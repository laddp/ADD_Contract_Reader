/*
 * Created on Jul 19, 2010 by pladd
 *
 */
package com.bottinifuel.ContractReader;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bottinifuel.AutoHelpParser;
import com.bottinifuel.FlatFileParser.FileFormatException;
import com.bottinifuel.FlatFileParser.FlatFile;
import com.bottinifuel.FlatFileParser.FlatFile.FileTypeEnum;
import com.bottinifuel.FlatFileParser.Records.DocumentRecord;
import com.bottinifuel.FlatFileParser.Records.Record;

/**
 * @author pladd
 *
 */
/*************************************************************************************
* Change Log:
* 
*   Date         Description                                        Pgmr
*  ------------  ------------------------------------------------   -----
*  Aug 22, 2014  Record length changed for ADDs 14 upgrade.        carlonc
*  version 1.1   Changes took place in ADD_FF_Parser
*                Look for comment 082214
***************************************************************************************/ 
public class ContractReader
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Error: missing argument");
            return;
        }
        
        AutoHelpParser argParser = new AutoHelpParser();

        AutoHelpParser.Option helpOption = argParser.addBooleanOption('h', "help");
        argParser.addHelp(helpOption, "Display program help");
        
        AutoHelpParser.Option dumpFileOption = argParser.addStringOption('d', "dump");
        argParser.addHelp(dumpFileOption, "Dump contents of first file to dump file");
        
        AutoHelpParser.Option contractFileOption = argParser.addStringOption('c', "contracts");
        argParser.addHelp(contractFileOption, "Contract file");
        
        AutoHelpParser.Option tankRentalFileOption = argParser.addStringOption('t', "rentals");
        argParser.addHelp(tankRentalFileOption, "Tank rental file");

        AutoHelpParser.Option dropItemsOption = argParser.addStringOption('x', "removeStatements");
        argParser.addHelp(dropItemsOption, "Remove contract for account(s) (requires -d)");

        AutoHelpParser.Option skipTrailerOption = argParser.addBooleanOption('q', "skipTrailer");
        argParser.addHelp(skipTrailerOption, "Ignore a missing trailer");

        AutoHelpParser.Option specialHandlingOption = argParser.addIntegerOption('s', "setSpecialHandling");
        argParser.addHelp(specialHandlingOption, "Set the special handling flag on set number of documents (0 = all) (requires -d)");

        List<Integer> dropStatements = new LinkedList<Integer>();
        FileWriter dumpFile       = null;
        FileWriter contractFile   = null;
        FileWriter tankRentalFile = null;

        String dumpFileName = null;
        String contractFileName = null;
        String tankRentalFileName = null;
        
        boolean skipTrailer = false;
        Integer setSpecialHandling = -1; // -1 : none; 0 : all; >0 set number
        
        try {
            argParser.parse(args);
            if (Boolean.TRUE.equals(argParser.getOptionValue(helpOption)))
            {
                argParser.printUsage();
                System.exit(0);
                return;
            }
            
            Object o = argParser.getOptionValue(dropItemsOption);
            if (o != null)
            {
                if (argParser.getRemainingArgs().length > 1)
                    throw new Exception("Multiple input files not permitted when removing statements");
                String dropItemList = (String)o;
                for (String item : dropItemList.split("[ ,]"))
                {
                    dropStatements.add(Integer.parseInt(item));
                }
            }

            o = argParser.getOptionValue(contractFileOption);
            if (o != null)
            {
                contractFileName = (String)o;
                contractFile = new FileWriter(contractFileName);
            }

            o = argParser.getOptionValue(tankRentalFileOption);
            if (o != null)
            {
                tankRentalFileName = (String)o;
                tankRentalFile = new FileWriter(tankRentalFileName);
            }
            
            if ((tankRentalFile != null && contractFile   == null) ||
                (contractFile   != null && tankRentalFile == null))
                throw new Exception("Tank Rental and contract files must be specified together.");

            o = argParser.getOptionValue(specialHandlingOption);
            if (o != null)
            	setSpecialHandling = (Integer)o;

            o = argParser.getOptionValue(dumpFileOption);
            if (o == null)
            {
                if (dropStatements.size() > 0)
                    throw new Exception("Dump file required when --removeStatements specified");
                if (setSpecialHandling.intValue() != -1)
                	throw new Exception("Dump file required when --setSpecialHandling specified");
            }
            else
            {
                if (tankRentalFile != null || contractFile != null)
                    throw new Exception("Dump file option cannot be specified when splitting contracts and tank rentals");
                dumpFileName = (String)o;
                dumpFile = new FileWriter(dumpFileName);
            }
            
            o = argParser.getOptionValue(skipTrailerOption);
            if (o != null)
            	skipTrailer = (Boolean)o;

        }
        catch (IllegalOptionValueException e)
        {
            System.out.println(e);
            argParser.printUsage();
            System.exit(2);
        }
        catch (UnknownOptionException e)
        {
            System.out.println(e);
            argParser.printUsage();
            System.exit(2);
        }
        catch (Exception e)
        {
            System.out.println(e);
            System.exit(2);
        }

        try
        {
            String[] files = argParser.getRemainingArgs();
            if (files.length != 1)
            {
                argParser.printUsage();
                System.exit(2);
            }
            
            FileReader fr = new FileReader(files[0]);
            FlatFile ff = new FlatFile(FileTypeEnum.CONTRACT_FILE, files[0], fr, System.out, 0, skipTrailer);

            System.out.println("Processed contract file " + files[0]);
            System.out.println("\tTotal contracts: " + ff.Trailer.TotalItems);
            System.out.println("\tSpecial Handling: " + ff.getSpecialHandlingCount());
            System.out.println("\tTotal Billed:    " + DecimalFormat.getCurrencyInstance().format(ff.Trailer.TotalAmountBilled));
            System.out.println("\tThis month's reference number: " + ContractFileInfo.GetContractReferenceNumber(ff));
            
            System.out.println("\nContract Counts:\n");
            Map<String, Integer> mainCounts = ContractFileInfo.PrintContractCounts(ff);
	        for (Entry<String, Integer> entry : mainCounts.entrySet())
	        {
	        	System.out.printf("%3s : %d\n", entry.getKey(), entry.getValue());
	        }

            if (dropStatements.size() != 0)
            {
                List<DocumentRecord> remove = new LinkedList<DocumentRecord>();
                for (Integer item : dropStatements)
                {
                    DocumentRecord stmt = ff.FindDocument(item.intValue());
                    if (stmt != null)
                        remove.add(stmt);
                    else
                        System.out.println("Remove failed for " + item + ": not found in file");
                }

                try
                {
                    ff.RemoveDocuments(remove);
                }
                catch (Exception e)
                {
                    System.out.println("Error removing duplicates - missing duplicate");
                }
            }
            
            if (setSpecialHandling >= 0)
            {
            	if (setSpecialHandling.intValue() == 0)
            		for (DocumentRecord doc : ff.Documents)
            			doc.setSpecialHandling(true);
            	else
            	{
                	int count = 0;
                	for (DocumentRecord doc : ff.Documents)
            		{
            			if (count++ < setSpecialHandling.intValue())
            				doc.setSpecialHandling(true);
            			else
            				break;
            		}
            	}
            }
            
            if (dumpFile != null)
            {
                for (Record r : ff.AllRecords)
                    dumpFile.write(r.dump() + "\n");
                dumpFile.close();
            }
            else if (contractFile != null && tankRentalFile != null)
            {
                FlatFile contracts   = (FlatFile)ff.clone();
                FlatFile tankRentals = (FlatFile)ff.clone();

                FilterTankRentalsOnly.fixIt(tankRentals, true);
                FilterTankRentalsOnly.fixIt(contracts, false);

                System.out.println("Wrote contracts to " + contractFileName);
                System.out.println("\tTotal contracts: " + contracts.Trailer.TotalItems);
                System.out.println("\tTotal Billed: " + DecimalFormat.getCurrencyInstance().format(contracts.getTotalDue()));

                System.out.println("\nContract Counts:\n");
                Map<String, Integer> contractCounts = ContractFileInfo.PrintContractCounts(contracts);
    	        for (Entry<String, Integer> entry : contractCounts.entrySet())
    	        {
    	        	System.out.printf("%3s : %d\n", entry.getKey(), entry.getValue());
    	        }
                System.out.println();
                
                for (Record r : contracts.AllRecords)
                    contractFile.write(r.dump() + "\n");
                contractFile.close();

                System.out.println("Wrote tank rentals to " + tankRentalFileName);
                System.out.println("\tTotal contracts: " + tankRentals.Trailer.TotalItems);
                System.out.println("\tTotal Billed: " + DecimalFormat.getCurrencyInstance().format(tankRentals.getTotalDue()));

                System.out.println("\nContract Counts:\n");
                Map<String, Integer> rentalCounts = ContractFileInfo.PrintContractCounts(tankRentals);
    	        for (Entry<String, Integer> entry : rentalCounts.entrySet())
    	        {
    	        	System.out.printf("%3s : %d\n", entry.getKey(), entry.getValue());
    	        }
                
                for (Record r : tankRentals.AllRecords)
                    tankRentalFile.write(r.dump() + "\n");
                tankRentalFile.close();
            }
        }
        catch (FileFormatException e)
        {
            System.out.println(e);
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return;
    }
}
