/*************************************************************************************
* Change Log:
* 
*   Date         Description                                        Pgmr
*  ------------  ------------------------------------------------   -----
*  Aug 22, 2014  Record length changed for ADDs 14 upgrade.        carlonc
*  version 1.1   Changes took place in ADD_FF_Parser
*                Look for comment 082214
***************************************************************************************/ 
package com.bottinifuel.ContractReader;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bottinifuel.FlatFileParser.FileFormatException;
import com.bottinifuel.FlatFileParser.FlatFile;
import com.bottinifuel.FlatFileParser.FlatFile.FileTypeEnum;

public class ContractReaderGUI {

	private JFrame mainFrame;

	private JTextArea outputText;
	private JButton runButton;
	private JTextField contractField;
	private JTextField outputContractField;
	private JTextField tankRentalField;

	private JFileChooser contractChooser;
	private JFileChooser outputContractChooser;
	private JFileChooser tankRentalChooser;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ContractReaderGUI window = new ContractReaderGUI();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ContractReaderGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the mainFrame.
	 */
	private void initialize() {
		String version = "1.1";
		mainFrame = new JFrame();
		mainFrame.setTitle("ADD Contract Reader-Version: "+version);
		mainFrame.setBounds(100, 100, 800, 650);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel centerPanel = new JPanel();
		mainFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		GridBagLayout gbl_centerPanel = new GridBagLayout();
		gbl_centerPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_centerPanel.columnWidths = new int[]{0, 0, 0};
		gbl_centerPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_centerPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		centerPanel.setLayout(gbl_centerPanel);

		JLabel contractLabel = new JLabel("Contract File:");
		GridBagConstraints gbc_contractLabel = new GridBagConstraints();
		gbc_contractLabel.anchor = GridBagConstraints.EAST;
		gbc_contractLabel.insets = new Insets(5, 5, 5, 5);
		gbc_contractLabel.gridx = 0;
		gbc_contractLabel.gridy = 0;
		centerPanel.add(contractLabel, gbc_contractLabel);
		
		contractField = new JTextField();
		GridBagConstraints gbc_contractField = new GridBagConstraints();
		gbc_contractField.insets = new Insets(5, 5, 5, 5);
		gbc_contractField.fill = GridBagConstraints.HORIZONTAL;
		gbc_contractField.gridx = 1;
		gbc_contractField.gridy = 0;
		centerPanel.add(contractField, gbc_contractField);
		contractField.setColumns(10);
		
		JButton contractBrowse = new JButton("Browse...");
		contractBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (contractChooser == null)
				{
					contractChooser = new JFileChooser("S:/Accounts Receivable/Billing/ServiceContracts/");
					FileNameExtensionFilter mfaFilter = new FileNameExtensionFilter("Statements (.MFA)", "MFA");
					contractChooser.setFileFilter(mfaFilter);
				}
				int rc = contractChooser.showOpenDialog(mainFrame);
				if (rc == JFileChooser.APPROVE_OPTION)
				{
					contractField.setText(contractChooser.getSelectedFile().getAbsolutePath());
					String path = contractChooser.getSelectedFile().getParent() + File.separator;
					String name = contractChooser.getSelectedFile().getName();
					String fullName = path + name.substring(0, name.lastIndexOf('.'));
					outputContractField.setText(fullName + "_contracts.MFA");
					tankRentalField.setText(fullName + "_tankrentals.MFA");
					runButton.setEnabled(true);
				}
			}
		});
		GridBagConstraints gbc_contractBrowse = new GridBagConstraints();
		gbc_contractBrowse.gridy = 0;
		gbc_contractBrowse.insets = new Insets(5, 5, 5, 5);
		gbc_contractBrowse.gridx = 2;
		centerPanel.add(contractBrowse, gbc_contractBrowse);
		
		JLabel lblContractOutputFile = new JLabel("Contract output file:");
		GridBagConstraints gbc_lblContractOutputFile = new GridBagConstraints();
		gbc_lblContractOutputFile.anchor = GridBagConstraints.EAST;
		gbc_lblContractOutputFile.insets = new Insets(5, 5, 5, 5);
		gbc_lblContractOutputFile.gridx = 0;
		gbc_lblContractOutputFile.gridy = 1;
		centerPanel.add(lblContractOutputFile, gbc_lblContractOutputFile);
		
		outputContractField = new JTextField();
		GridBagConstraints gbc_outputContractField = new GridBagConstraints();
		gbc_outputContractField.insets = new Insets(5, 5, 5, 5);
		gbc_outputContractField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputContractField.gridx = 1;
		gbc_outputContractField.gridy = 1;
		centerPanel.add(outputContractField, gbc_outputContractField);
		outputContractField.setColumns(10);
		
		JButton outputContractBrowse = new JButton("Browse...");
		outputContractBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (outputContractChooser == null)
				{
					outputContractChooser = new JFileChooser("S:/Accounts Receivable/Billing/Statements/");
					FileNameExtensionFilter mfaFilter = new FileNameExtensionFilter("Statements (.MFA)", "MFA");
					outputContractChooser.setFileFilter(mfaFilter);
				}
				int rc = outputContractChooser.showOpenDialog(mainFrame);
				if (rc == JFileChooser.APPROVE_OPTION)
				{
					outputContractField.setText(outputContractChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_outputContractBrowse = new GridBagConstraints();
		gbc_outputContractBrowse.insets = new Insets(5, 5, 5, 5);
		gbc_outputContractBrowse.gridx = 2;
		gbc_outputContractBrowse.gridy = 1;
		centerPanel.add(outputContractBrowse, gbc_outputContractBrowse);
		
		JLabel tankRentalLabel = new JLabel("Tank Rental output file:");
		GridBagConstraints gbc_tankRentalLabel = new GridBagConstraints();
		gbc_tankRentalLabel.anchor = GridBagConstraints.EAST;
		gbc_tankRentalLabel.insets = new Insets(5, 5, 5, 5);
		gbc_tankRentalLabel.gridx = 0;
		gbc_tankRentalLabel.gridy = 2;
		centerPanel.add(tankRentalLabel, gbc_tankRentalLabel);
		
		tankRentalField = new JTextField();
		GridBagConstraints gbc_tankRentalField = new GridBagConstraints();
		gbc_tankRentalField.insets = new Insets(5, 5, 5, 5);
		gbc_tankRentalField.fill = GridBagConstraints.HORIZONTAL;
		gbc_tankRentalField.gridx = 1;
		gbc_tankRentalField.gridy = 2;
		centerPanel.add(tankRentalField, gbc_tankRentalField);
		tankRentalField.setColumns(10);
		
		JButton tankRentalBrowse = new JButton("Browse...");
		tankRentalBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tankRentalChooser == null)
				{
					tankRentalChooser = new JFileChooser("S:/Accounts Receivable/Billing/Statements/");
					FileNameExtensionFilter mfaFilter = new FileNameExtensionFilter("Statements (.MFA)", "MFA");
					tankRentalChooser.setFileFilter(mfaFilter);
				}
				int rc = tankRentalChooser.showOpenDialog(mainFrame);
				if (rc == JFileChooser.APPROVE_OPTION)
				{
					tankRentalField.setText(tankRentalChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_tankRentalBrowse = new GridBagConstraints();
		gbc_tankRentalBrowse.insets = new Insets(5, 5, 5, 5);
		gbc_tankRentalBrowse.gridx = 2;
		gbc_tankRentalBrowse.gridy = 2;
		centerPanel.add(tankRentalBrowse, gbc_tankRentalBrowse);
		
		JScrollPane outputScrollPane = new JScrollPane();
		GridBagConstraints gbc_outputScrollPane = new GridBagConstraints();
		gbc_outputScrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_outputScrollPane.weighty = 5.0;
		gbc_outputScrollPane.fill = GridBagConstraints.BOTH;
		gbc_outputScrollPane.gridwidth = 3;
		gbc_outputScrollPane.gridx = 0;
		gbc_outputScrollPane.gridy = 4;
		centerPanel.add(outputScrollPane, gbc_outputScrollPane);
		
		outputText = new JTextArea();
		outputText.setFont(new Font("Courier New", Font.PLAIN, 11));
		outputScrollPane.setViewportView(outputText);
		
		JLabel messagesLabel = new JLabel("Messages:");
		GridBagConstraints gbc_messagesLabel = new GridBagConstraints();
		gbc_messagesLabel.weighty = -1.0;
		gbc_messagesLabel.weightx = -1.0;
		gbc_messagesLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_messagesLabel.insets = new Insets(5, 5, 5, 5);
		gbc_messagesLabel.gridx = 0;
		gbc_messagesLabel.gridy = 3;
		centerPanel.add(messagesLabel, gbc_messagesLabel);
		JPanel bottomPanel = new JPanel();
		mainFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		runButton = new JButton("Run");
		runButton.setEnabled(false);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRun();
			}
		});
		bottomPanel.add(runButton);
		
		JButton saveButton = new JButton("Save...");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSave();
			}
		});
		bottomPanel.add(saveButton);
	}

	private void doRun() {
        outputText.setText(null);

		try {
	        FileReader fr = new FileReader(contractField.getText());
            ByteArrayOutputStream messages = new ByteArrayOutputStream();

            FlatFile ff = new FlatFile(FileTypeEnum.CONTRACT_FILE, contractField.getText(), fr, new PrintStream(messages), 0, false);

	        outputText.append("Processed contract file " + contractField.getText() + '\n');
	        outputText.append("\tTotal contracts: " + ff.Trailer.TotalItems + '\n');
	        outputText.append("\tTotal Billed:    " + DecimalFormat.getCurrencyInstance().format(ff.Trailer.TotalAmountBilled) + '\n');
	        outputText.append("\tThis month's reference number: " + ContractFileInfo.GetContractReferenceNumber(ff) + "\n\n");
	        
	        outputText.append(messages.toString());
	        
	        @SuppressWarnings("unused")
			FileWriter contractFile   = null;
	        @SuppressWarnings("unused")
			FileWriter tankRentalFile = null;
	        
	        Map<String, Integer> contractCounts = ContractFileInfo.PrintContractCounts(ff);
	        
	        Formatter fmt = new Formatter();
	        
	        for (Entry<String, Integer> entry : contractCounts.entrySet())
	        {
	        	fmt.format("%3s : %d\n", entry.getKey(), entry.getValue());
	        }
	        
	        outputText.append("\nContract Counts:\n\n");
	        outputText.append(fmt.toString());
	        
	        fmt.close();
	        
	    } catch (FileNotFoundException e) {
			outputText.append(e.toString());
		} catch (FileFormatException e) {
			outputText.append(e.toString());
		} catch (IOException e) {
			outputText.append(e.toString());
		}
	}

	private void doSave() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		JFileChooser outputFileChooser = new JFileChooser("S:/Accounts Receivable/Billing/Statements/");
		 outputFileChooser.setSelectedFile(new File(df.format(new Date()) + ".txt"));
		int rc = outputFileChooser.showSaveDialog(mainFrame);
		if (rc == JFileChooser.APPROVE_OPTION)
		{
			try {
				FileOutputStream out = new FileOutputStream(outputFileChooser.getSelectedFile());
				PrintStream printout = new PrintStream(out);
				printout.append(outputText.getText());
				out.close();
			} catch (FileNotFoundException e) {
				outputText.append(e.toString());
			} catch (IOException e) {
				outputText.append(e.toString());
			}
		}
	}
}
