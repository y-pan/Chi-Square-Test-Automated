package yun.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yun
 */
public class Lib {

    public static void writeFile(String file, String content){
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
        } catch (IOException e) {
            System.out.println("Error in writing file: " + e.toString());
        }
    }
    public static void writeFile(String file, String content, boolean append){
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, append))) {
            bw.write(content);
        } catch (IOException e) {
            System.out.println("Error in writing file: " + e.toString());
        }
    }

    public static double[][] getExpectedTable(int[][] table, int[] total_right, int[] total_bottom, int grandTotal) {
        double[][] result = null;
        int d1Size = table.length;
        int d2Size = table[0].length;
        result = new double[d1Size][d2Size];
        // initialize with 0s
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                result[i][j] = 0d;
            }
        }
        // push expected value to cells: E[i][j] = (total_right[i] * total_bottom[j])/grandTotal
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                result[i][j] =(double)((total_right[i] * total_bottom[j]) * 1.0/ grandTotal);
            }
        }
        return result;
    }

    public static String[] getMapKeysArray(Map map) {

        String[] keys = new String[map.size()];
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            keys[Integer.parseInt(pair.getValue().toString())] = pair.getKey().toString();
            it.remove();
        }
        return keys;
    }

    public static int[] sumUpAlong(int[][] table, boolean isFirstDim) {
        // isFirstDim = true, which is first dimension, sum up along row, then the sums should be at the right side of table 
        int[] sums = null;
        if (isFirstDim) {
            sums = new int[table.length];
            for (int i = 0; i < table.length; i++) {
                int _sum = 0;
                for (int x : table[i]) {
                    _sum += x;
                }
                sums[i] = _sum;
            }
        } else {
            // isFirstDim = false, which is second dimension, sum up along column, then the sums should be at the bottom of table 
            sums = new int[table[0].length];
            for (int j = 0; j < table[0].length; j++) {
                int _sum = 0;
                for (int i = 0; i < table.length; i++) {
                    _sum += table[i][j];
                }
                sums[j] = _sum;
            }
        }
        return sums;

    }

    public static List<ChiSquareResult> fullChiSquareTest(List<String[]> data, int outcomeIndex, String outputDir){
    
        File dir = new File(outputDir);
        if(!dir.exists() || !dir.isDirectory()){
            dir.mkdirs();
        }
        
        List<ChiSquareResult> result = new ArrayList<>();
        int columnCount = data.get(0).length;
        
        for(int i = 0; i< columnCount; i++){
            // feature vs class
            if( i == outcomeIndex) continue;
            result.add(Lib.getPivotTable_ChiTest(data, i, outcomeIndex, outputDir + "chi_"+i+"_"+outcomeIndex+".csv"));
        }
        // feature vs feature
        for(int i = 0; i< columnCount; i++){
            if(i==outcomeIndex) continue;
            for(int j = 0; j < columnCount; j++){
                if(j==outcomeIndex || i==j) continue;

                result.add(Lib.getPivotTable_ChiTest(data, i, j, outputDir + "chi_"+i+"_"+j+".csv"));
            }
        }
        return result;
    }
    
    /* For missing value, ignore(not use the row has missing value) */
    public static List<String[]> parseCSV(String path) {

        List<String[]> data = new ArrayList<>();
        Path pathToFile = Paths.get(path);

        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
            String line = null;
            //int i = 0;
            while ((line = br.readLine()) != null) {
                //i++;
                String[] newArray =line.split(",");
                
                // excluding rows of missing values
                if(newArray.length < 2 || newArray.equals(null)) { continue; }
                if(newArray[0].trim().length() == 0 || newArray[1].trim().length() == 0) continue;
                
                //System.out.println("#"+i+": " + newArray.length + " | " + newArray[0] + ", " + newArray[1]);
                
                boolean hasMissingValue = false;
                for(String value : newArray){
                    if(value.length() == 0 || value.equals(null) || value.equals("")) { hasMissingValue = true; }
                }
                
                if(!hasMissingValue) data.add(newArray);
                
            }
            return data;
        } catch (Exception ex) {
            return null;
        }

    }
    
    /*
    *   data = List<String[]>()
        varIndex = index of column as feature your want to test
        outIndex = index of outcome column as outcome that you want to test feature against
        outputCsvFile = the result of CHI-SQUARE testing result
        asummptions: p_value < 0.05 then reject null hypothesis, and feature has relationship with outcome
    */
    
    
        public static ChiSquareResult getPivotTable_ChiTest(List<String[]> data, int varIndex, int outIndex, String outputCsvFile) {
        //                      0	1	2	3	4	5	6	7       Total        
        //                      A	B	M	P	C	S	J	O       Total
        //Not Retained(0)       0,0     0,1
        //Retained(1)           1,0     1,1
        //Total
        String[] firstLine = data.get(0);
        String title = VarConfig.TITLE_PREFIX + firstLine[varIndex] + " & " + firstLine[outIndex];
        //System.out.println(firstLine[varIndex] + " | " + firstLine[outIndex]);
        // map strings to nums, to be used in value locating
        Map varMap = new HashMap();
        Map outMap = new HashMap();
        int vNum = 0;
        int oNum = 0;
        try {
            for (int i = 1; i < data.size(); i++) {
                
                String[] line = data.get(i);                
                String v = line[varIndex];
                String o = line[outIndex];
                    
                if (!varMap.containsKey(v)) {
                    varMap.put(v, vNum++);
                }
                if (!outMap.containsKey(o)) {
                    outMap.put(o, oNum++);
                }
            }
        } catch (Exception ex) {
            System.out.println("CSV file data corrupted, fix data first!");
            return null;
        }

        // form pivot table structure, initialize with all zeros 
        int d2Size = vNum;
        int d1Size = oNum;
        int[][] table = new int[d1Size][d2Size];
        for (int i = 0; i < d1Size; i++) {
            //row
            for (int j = 0; j < d2Size; j++) {
                //column
                table[i][j] = 0;
            }
        }
        // push number(count of occurrance) to table cell
        for (int i = 1; i < data.size(); i++) {
            String[] line = data.get(i);
            String v = line[varIndex];
            String o = line[outIndex];

            int vi = Integer.parseInt(varMap.get(v).toString());
            int oi = Integer.parseInt(outMap.get(o).toString());
            table[oi][vi] = table[oi][vi] + 1;
        }

        // get Chi-square-critical value: 1. 
        int[] total_right = Lib.sumUpAlong(table, true);
        int[] total_bottom = Lib.sumUpAlong(table, false);
//        System.out.println("sumup to right: " + total_right.toString());
//        System.out.println("sumup to bottom: " + total_bottom.toString());

        // ----------------------- print out observed values table -----------------------
        // title of table 
        String[] columnNames = Lib.getMapKeysArray(varMap);  // horizontal headers, for column name
        String[] rowNames = Lib.getMapKeysArray(outMap);  // vertical headers, for row name
        // table[][] is the data

        StringBuilder sb = new StringBuilder();

        sb.append("[" + title + "]" + "\n");

        for (String columnName : columnNames) {
            sb.append("," + columnName);
        }
        sb.append(","+VarConfig.TOTAL);/*=== === === Total header at the right === === ===*/
        sb.append("\n");
        for (int i = 0; i < d1Size; i++) {
            //row
            sb.append(rowNames[i]);
            for (int j = 0; j < d2Size; j++) {
                //column
                sb.append("," + table[i][j]);
            }
            sb.append("," + total_right[i]);
            /*=== === === Total value at the right === === ===*/
            sb.append("\n");
        }
        sb.append(VarConfig.TOTAL);
        int grandTotal = 0;
        for (int x : total_bottom) {
            grandTotal += x;
            sb.append("," + x);
        }
        sb.append("," + grandTotal);
        sb.append("\n");

        // observed values table:
        // table[i][j],   total_right
        // total_bottom   grandTotal
        
        // ---------------------- calculate expected values table: ----------------------
        double[][] expectedTable = Lib.getExpectedTable(table, total_right, total_bottom, grandTotal);

        // ----------------------- print out expected values table -----------------------
        sb.append("\n"+VarConfig.EXPECTED+"\n");

        for (String columnName : columnNames) {
            sb.append("," + columnName);
        }
        //sb.append(",Total");/*=== === === Total header at the right === === ===*/
        sb.append("\n");

        for (int i = 0; i < d1Size; i++) {
            //row
            sb.append(rowNames[i]);
            for (int j = 0; j < d2Size; j++) {
                //column
                sb.append("," + expectedTable[i][j]);
            }
            //sb.append(","+total_right[i]); /*=== === === Total value at the right === === ===*/
            sb.append("\n");
        }
        //sb.append("Total");

        // calculate Chi-Square-Critical-value
        double chiSqValue = ChiSquare.getChiSquareCriticalValue(table, expectedTable);
        
        // calculate alpha
        int df = (oNum-1)*(vNum-1);
        double p_value = ChiSquare.getPValue(chiSqValue, df);
        sb.append("\n").append(VarConfig.CHI_SQUARE_VALUE).append(","+chiSqValue);
        sb.append("\n").append(VarConfig.DEGREE_OF_FREEDOM).append(","+df);
        sb.append("\n").append(VarConfig.P_VALUE).append(","+p_value);
        
        // optional to show if significant enough to reject H0
        String conclusion = "";
        if(p_value < 0.05){ conclusion = VarConfig.REJECT_NULL;}
        else{ conclusion = VarConfig.ACCEPT_NULL; }
        
        sb.append("\n").append(VarConfig.CONCLUSION).append(","+conclusion);

//        System.out.println();
//        System.out.println(sb);
        Lib.writeFile(outputCsvFile, sb.toString());

        return new ChiSquareResult(title, new double[]{chiSqValue, df, p_value});

        /*
var.key: [h1_1, h1_2, h1_3, h1_4]
var.val: [0,    1,    2,    3]
        
out.key: [h5_2, h5_3]
out.val: [1,    0]
        
h1_1, h1_2, h1_3, h1_4, 
-------------------------
h5_3: 4, 1, 3, 3, 
h5_2: 1, 0, 1, 2, 
         */
    }

    
    
    public static void getPivotTable_ChiTest_void(List<String[]> data, int varIndex, int outIndex, String outputCsvFile) {
        //                      0	1	2	3	4	5	6	7       Total        
        //                      A	B	M	P	C	S	J	O       Total
        //Not Retained(0)       0,0     0,1
        //Retained(1)           1,0     1,1
        //Total
        String[] firstLine = data.get(0);
        String title = VarConfig.TITLE_PREFIX + firstLine[varIndex] + " & " + firstLine[outIndex];

        // map strings to nums, to be used in value locating
        Map varMap = new HashMap();
        Map outMap = new HashMap();
        int vNum = 0;
        int oNum = 0;
        try {
            for (int i = 1; i < data.size(); i++) {
                String[] line = data.get(i);
                String v = line[varIndex];
                String o = line[outIndex];
                if (!varMap.containsKey(v)) {
                    varMap.put(v, vNum++);
                }
                if (!outMap.containsKey(o)) {
                    outMap.put(o, oNum++);
                }
            }
        } catch (Exception ex) {
            System.out.println("CSV file data corrupted, fix data first!");
            return;
        }

        // form pivot table structure, initialize with all zeros 
        int d2Size = vNum;
        int d1Size = oNum;
        int[][] table = new int[d1Size][d2Size];
        for (int i = 0; i < d1Size; i++) {
            //row
            for (int j = 0; j < d2Size; j++) {
                //column
                table[i][j] = 0;
            }
        }
        // push number(count of occurrance) to table cell
        for (int i = 1; i < data.size(); i++) {
            String[] line = data.get(i);
            String v = line[varIndex];
            String o = line[outIndex];

            int vi = Integer.parseInt(varMap.get(v).toString());
            int oi = Integer.parseInt(outMap.get(o).toString());
            table[oi][vi] = table[oi][vi] + 1;
        }

        // get Chi-square-critical value: 1. 
        int[] total_right = Lib.sumUpAlong(table, true);
        int[] total_bottom = Lib.sumUpAlong(table, false);
//        System.out.println("sumup to right: " + total_right.toString());
//        System.out.println("sumup to bottom: " + total_bottom.toString());

        // ----------------------- print out observed values table -----------------------
        // title of table 
        String[] columnNames = Lib.getMapKeysArray(varMap);  // horizontal headers, for column name
        String[] rowNames = Lib.getMapKeysArray(outMap);  // vertical headers, for row name
        // table[][] is the data

        StringBuilder sb = new StringBuilder();

        sb.append("[" + title + "]" + "\n");

        for (String columnName : columnNames) {
            sb.append("," + columnName);
        }
        sb.append(","+VarConfig.TOTAL);/*=== === === Total header at the right === === ===*/
        sb.append("\n");
        for (int i = 0; i < d1Size; i++) {
            //row
            sb.append(rowNames[i]);
            for (int j = 0; j < d2Size; j++) {
                //column
                sb.append("," + table[i][j]);
            }
            sb.append("," + total_right[i]);
            /*=== === === Total value at the right === === ===*/
            sb.append("\n");
        }
        sb.append(VarConfig.TOTAL);
        int grandTotal = 0;
        for (int x : total_bottom) {
            grandTotal += x;
            sb.append("," + x);
        }
        sb.append("," + grandTotal);
        sb.append("\n");

        // observed values table:
        // table[i][j],   total_right
        // total_bottom   grandTotal
        
        // ---------------------- calculate expected values table: ----------------------
        double[][] expectedTable = Lib.getExpectedTable(table, total_right, total_bottom, grandTotal);

        // ----------------------- print out expected values table -----------------------
        sb.append("\n"+VarConfig.EXPECTED+"\n");

        for (String columnName : columnNames) {
            sb.append("," + columnName);
        }
        //sb.append(",Total");/*=== === === Total header at the right === === ===*/
        sb.append("\n");

        for (int i = 0; i < d1Size; i++) {
            //row
            sb.append(rowNames[i]);
            for (int j = 0; j < d2Size; j++) {
                //column
                sb.append("," + expectedTable[i][j]);
            }
            //sb.append(","+total_right[i]); /*=== === === Total value at the right === === ===*/
            sb.append("\n");
        }
        //sb.append("Total");

        // calculate Chi-Square-Critical-value
        double chiSqValue = ChiSquare.getChiSquareCriticalValue(table, expectedTable);
        
        // calculate alpha
        int df = (oNum-1)*(vNum-1);
        double p_value = ChiSquare.getPValue(chiSqValue, df);
        sb.append("\n").append(VarConfig.CHI_SQUARE_VALUE).append(","+chiSqValue);
        sb.append("\n").append(VarConfig.DEGREE_OF_FREEDOM).append(","+df);
        sb.append("\n").append(VarConfig.P_VALUE).append(","+p_value);
        
        // optional to show if significant enough to reject H0
        String conclusion = "";
        if(p_value < 0.05){ conclusion = VarConfig.REJECT_NULL;}
        else{ conclusion = VarConfig.ACCEPT_NULL; }
        
        sb.append("\n").append(VarConfig.CONCLUSION).append(","+conclusion);

//        System.out.println();
//        System.out.println(sb);
        Lib.writeFile(outputCsvFile, sb.toString());


        /*
var.key: [h1_1, h1_2, h1_3, h1_4]
var.val: [0,    1,    2,    3]
        
out.key: [h5_2, h5_3]
out.val: [1,    0]
        
h1_1, h1_2, h1_3, h1_4, 
-------------------------
h5_3: 4, 1, 3, 3, 
h5_2: 1, 0, 1, 2, 
         */
    }
}
