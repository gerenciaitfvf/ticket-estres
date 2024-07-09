/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fvf.ticket.estres.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author franciscogomezlopez
 */
public class ExcelResults {
    
    private static ExcelResults single_instance = null;

    private String pathResultFile;
    
    private int totalThreads;
    private int finishThreads = 0;
    

    private long startTiming = Calendar.getInstance().getTimeInMillis();
    private HashMap<String, JSONObject> tickets = new LinkedHashMap<String, JSONObject>();
    private List<JSONObject> scanners = new ArrayList<JSONObject>() ;
    private List<JSONObject> parameters = new ArrayList<JSONObject>() ;
    public static ExcelResults getInstance() {
        if (single_instance == null) {
            single_instance = new ExcelResults();
        }

        return single_instance;
    }
    
    public void setTotalThreads(int threads) {
        this.totalThreads = threads;
    }
    
    public void setPathResultFile(String path) {
        this.pathResultFile = path;
        this.putParameter("pathResultFile", path);
    }
    
    public void putParameter(String param, String value) {
        try {
        
            JSONObject parametro = new JSONObject();
            parametro.put("parametro", param);
            parametro.put("valor", value);
            this.parameters.add(parametro);
            
        } catch (Exception e) {
            System.out.println("error cargando json en parametros");
            e.printStackTrace();
        }
    }
    
    public void resumeExcel(HashMap<String, JSONObject> ticketsloaded, JSONObject scannerInfo) {
    
        if(ticketsloaded.isEmpty())
            return;
        
        this.tickets.putAll(ticketsloaded);
        this.scanners.add(scannerInfo);
        this.finishThreads++;
        
        if(this.totalThreads != this.finishThreads) {
            return;
        }
        
        this.generateExcelResultFile();
        
    }
    
    private void generateExcelResultFile() {
        
        long longTimeFinish = Calendar.getInstance().getTimeInMillis() - this.startTiming;
        
        // scanner tickets info
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ticketsinfo");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("ticket");
        row.createCell(1).setCellValue("time_created");
        row.createCell(2).setCellValue("time_send");
        row.createCell(3).setCellValue("time_response");
        row.createCell(4).setCellValue("status_response");
        row.createCell(5).setCellValue("time_sleep");
        row.createCell(6).setCellValue("device");
        
        int indexRow = 1;
        
        for (String ticketref: this.tickets.keySet()) {
            
            JSONObject ticktobj = this.tickets.get(ticketref);
            
            row = sheet.createRow(indexRow);
            
            try {
                row.createCell(0).setCellValue(ticktobj.getString("ticket"));
                row.createCell(1).setCellValue(ticktobj.getString("time_created"));
                row.createCell(2).setCellValue(ticktobj.getString("time_send"));
                row.createCell(3).setCellValue(ticktobj.getString("time_response"));
                row.createCell(4).setCellValue(ticktobj.getString("status_response"));
                row.createCell(5).setCellValue(ticktobj.getString("time_sleep"));
                row.createCell(6).setCellValue(ticktobj.getString("device"));
            } catch (Exception e) {
                System.out.println("error creando fila excel");
                e.printStackTrace();
            }
            
            indexRow++;
        }
        
        // scanner info
        sheet = workbook.createSheet("scannerinfo");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("device");
        row.createCell(1).setCellValue("time_completed");
        row.createCell(2).setCellValue("total_tickets");
        
        indexRow = 1;
        
        for (JSONObject ticktobj: this.scanners) {
            
            row = sheet.createRow(indexRow);
            
            try {
                row.createCell(0).setCellValue(ticktobj.getString("device"));
                row.createCell(1).setCellValue(ticktobj.getString("time_completed"));
                row.createCell(2).setCellValue(ticktobj.getString("total_tickets"));
            } catch (Exception e) {
                System.out.println("error creando fila scanner info excel");
                e.printStackTrace();
            }
            
            indexRow++;
        }
        
        //parametros info
        sheet = workbook.createSheet("parametersinfo");
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("parametro");
        row.createCell(1).setCellValue("valor");
        
        indexRow = 1;
        
        this.putParameter("total_execution_time", "Todos los tickets validados fue en: " + (longTimeFinish / 1000) + " secs");
        
        for (JSONObject ticktobj: this.parameters) {
            
            row = sheet.createRow(indexRow);
            
            try {
                row.createCell(0).setCellValue(ticktobj.getString("parametro"));
                row.createCell(1).setCellValue(ticktobj.getString("valor"));
            } catch (Exception e) {
                System.out.println("error creando fila parameter info excel");
                e.printStackTrace();
            }
            
            indexRow++;
        }
        
        
        try {
            FileOutputStream out = new FileOutputStream(
            new File(this.pathResultFile));
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Todos los tickets validados fue en: " + (longTimeFinish / 1000) + " secs");
    
    }
    
}
