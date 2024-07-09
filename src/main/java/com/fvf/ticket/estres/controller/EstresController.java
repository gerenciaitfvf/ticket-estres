/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fvf.ticket.estres.controller;

import com.fvf.ticket.estres.model.Scanner;
import com.fvf.ticket.estres.service.BearerAuth;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author franciscogomezlopez
 */
public class EstresController {

    private String bearerToken;
    private List<Scanner> devices;

    public boolean initAuth(String username, String password) {

        try {
            
            
            final BearerAuth auth = new BearerAuth();
            auth.initConnection();
            String bearerResponse = auth.getBearerToken(username, password);

            this.bearerToken = (new JSONObject(bearerResponse)).getString("token");
            
            // for general purpose
            ExcelResults.getInstance().putParameter("bearerToken", this.bearerToken);
            ExcelResults.getInstance().putParameter("beareruser", username);
            ExcelResults.getInstance().putParameter("bearerpass", password);
            
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            //Logger.getLogger(EstresController.class.getName()).log(Level.SEVERE, null, ex);

        }

        return false;
    }
    
    public void createDevices(int qty) {
        this.devices = new ArrayList<Scanner>();
        
        for (int i = 0; i < qty; i++) {
        
            Scanner tmp = new Scanner();
            tmp.setId(i + 1);
            tmp.setBearerToken(this.bearerToken);
            this.devices.add(tmp);
        
        }
        
        // for general purpose
        ExcelResults.getInstance().putParameter("total_devices", String.valueOf(qty));
    }

    public void startTicketScanningProcess(String pathResultFile) {

        ExcelResults.getInstance().setPathResultFile(pathResultFile);
        ExcelResults.getInstance().setTotalThreads(this.devices.size());

        Thread nextthread = null; 
        for( Scanner actual : this.devices) {
            
            actual.initConnection();
            nextthread = new Thread(actual);
            nextthread.start();
            
        }
    
    } 
    
    
    public void loadDataInScanner(String pathTicketsFile) throws Exception {


        Workbook workbook = WorkbookFactory.create(new File(pathTicketsFile));
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        int totalTickets = 0;
        int totalBySheet = 0;
        int devicesSize = this.devices.size();
        int deviceIndex = 0;
        
        
        for (Sheet actual : workbook) {

            //System.out.println("sheet: " + actual.getSheetName());

            for (Row row : actual) {
                
                if(row.getRowNum() == 0) {
                    continue;
                }
                
                if(deviceIndex >= devicesSize) {
                    deviceIndex = 0;
                }
                
                for (Cell cell : row) {

                    CellValue cellValueCheck = evaluator.evaluate(cell);
                    String cellValue = null;
                    if (cellValueCheck != null) {
                        switch (cellValueCheck.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                //System.out.print(cellValueCheck.getStringValue());
                                cellValue = cellValueCheck.getStringValue();
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                //System.out.print(cellValueCheck.getBooleanValue());
                                cellValue = cellValueCheck.getBooleanValue() ? "1" : "0";
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                //System.out.print(cellValueCheck.getNumberValue());
                                cellValue = Double.toString(cellValueCheck.getNumberValue());
                                break;
                        }
                    }

                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        int property = cell.getColumnIndex();
                        switch (property) {
                            case 1: // ticket
                                totalTickets++;
                                totalBySheet++;
                                this.devices.get(deviceIndex).addTicket(cellValue);
                                //System.out.println("ticket: " + cellValue.trim());
                                break;
                        }
                    }
                }
                
                deviceIndex++;

            }
            
            System.out.println("Total sheet (" + actual.getSheetName() + "): " + totalBySheet );
            totalBySheet = 0;

        }

        System.out.println("Total tickets: " + totalTickets);
        ExcelResults.getInstance().putParameter("pathticketFile", pathTicketsFile);
    }

}
