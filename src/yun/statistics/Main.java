package yun.statistics;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		int outcomeIndex = 0; 
        String sourceCSV = "C:\\Users\\yun\\Desktop\\test1.csv";
        String outputDir = "C:\\Users\\yun\\Desktop\\DataResult";
         
        
        List<ChiSquareResult> fullTest = new ChiSquareTestManager()
                .setSourceCSV(sourceCSV)
                .setOutputDir(outputDir)
                .setOutcomeIndex(outcomeIndex)
                .setFinalCsvName("finalFantacy4")
                .run();
        /*
        List<String[]> data = Lib.parseCSV("C:\\Users\\yun\\Desktop\\test1.csv");
        // last one is  data.get(0).length -1
        List<ChiSquareResult> fullScan = Lib.fullChiSquareTest(data, outcomeIndex, outputDir);
        String content = "";
        for(int i=0; i< fullScan.size(); i++){
            content +=fullScan.get(i).toString() + "\n";
            //Lib.writeFile(outputDir+"overall.csv", fullScan.get(i).toString() + "\n",true);
            //System.out.println(fullScan.get(i).toString());
        }
        Lib.writeFile(outputDir+"overall.csv", content);*/

	}

}
