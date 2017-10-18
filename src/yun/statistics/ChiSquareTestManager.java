package yun.statistics;

import java.util.List;

public class ChiSquareTestManager {

	private String sourceCSV;
    private String outputDir;
    private int outcomeIndex;
    private String finalResultCsvName;

    public String getFinalCsvName() {
    	if(finalResultCsvName.isEmpty() || finalResultCsvName.equals(null) ||finalResultCsvName.equals("")) {finalResultCsvName = "overallChiSq.csv";}
        return finalResultCsvName;
    }

    public ChiSquareTestManager setFinalCsvName(String finalCsvName) {
        this.finalResultCsvName = finalCsvName;
        if(!this.finalResultCsvName.toLowerCase().endsWith(".csv")) { this.finalResultCsvName = this.finalResultCsvName + ".csv"; }
        return this;
    }
    public ChiSquareTestManager(){}
    
    public String getSourceCSV() {
        return sourceCSV;
    }

    public ChiSquareTestManager setSourceCSV(String sourceCSV) {
        this.sourceCSV = sourceCSV;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public ChiSquareTestManager setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        if(!this.outputDir.endsWith("\\")){
            this.outputDir = this.outputDir + "\\";
        }
        return this;
    }

    public int getOutcomeIndex() {
        return outcomeIndex;
    }

    public ChiSquareTestManager setOutcomeIndex(int outcomeIndex) {
        this.outcomeIndex = outcomeIndex;
        return this;
    }
    
    public List<ChiSquareResult> run(){
    
        List<String[]> data = Lib.parseCSV(this.getSourceCSV());
       
        List<ChiSquareResult> fullScan = Lib.fullChiSquareTest(data, this.getOutcomeIndex(), this.getOutputDir());
        
        // output to file and console, and to caller program
        StringBuilder sb = new StringBuilder();
        sb.append(","+VarConfig.CHI_SQUARE_VALUE).append(","+VarConfig.DEGREE_OF_FREEDOM).append(","+VarConfig.P_VALUE).append(","+VarConfig.INDEPENDENT).append("\n");
        
        for(int i=0; i< fullScan.size(); i++){
            double p_value = fullScan.get(i).getValues()[2]; // assume it is double[Chi-Square-Value, Degree of freedom, P-Value]
            boolean isIndependent;
            if(p_value < 0.05){ isIndependent = false;}
            else{ isIndependent = true; }
            
            sb.append(fullScan.get(i).toString()).append(","+isIndependent+"\n");
        
        }
        
        Lib.writeFile(outputDir+this.getFinalCsvName(), sb.toString());
        System.out.println("\n=== === === === === ===\n"+sb.toString() + "\n=== === === === === ===\n");

        return fullScan;
    }
}
