package yun.statistics;

public class ChiSquareResult {
    private String title;
    private double[] values;

    @Override
    public String toString() {
        String result = title;
        for(int i=0; i< values.length; i++){
            result += "," + values[i];
        }
        return result;
    }

    public ChiSquareResult(){
    }
    
    public ChiSquareResult(String title, double[] values) {
        this.title = title;
        this.values = values;
    }
    
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }
    
}
