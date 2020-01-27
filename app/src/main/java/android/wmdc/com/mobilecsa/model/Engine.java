package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 4/30/2018.
 */

public class Engine {

    private String makeId;
    private String modelId;
    private String year;
    private String model;
    private String category;
    private String make;

    public Engine(String makeId, String modelId, String year, String model, String category,
                  String make) {

        this.makeId = makeId;
        this.modelId = modelId;
        this.year = year;
        this.model = model;
        this.category = category;
        this.make = make;
    }

    public String getMakeId() {
        return makeId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getYear() {
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
