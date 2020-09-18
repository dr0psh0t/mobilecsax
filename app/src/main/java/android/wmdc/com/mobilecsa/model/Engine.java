package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 4/30/2018.
 */

public class Engine {

    private int makeId;
    private int modelId;
    private int year;
    private String model;
    private String category;
    private String make;

    public Engine(int makeId, int modelId, int year, String model, String category,
                  String make) {

        this.makeId = makeId;
        this.modelId = modelId;
        this.year = year;
        this.model = model;
        this.category = category;
        this.make = make;
    }

    public int getMakeId() {
        return makeId;
    }

    public int getModelId() {
        return modelId;
    }

    public int getYear() {
        return year;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public String getMake() {
        return make;
    }
}
