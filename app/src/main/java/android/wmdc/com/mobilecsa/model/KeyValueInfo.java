package android.wmdc.com.mobilecsa.model;

/**
 * Created by wmdcprog on 12/28/2017.
 */

public class KeyValueInfo {

    private String key;
    private String value;

    public KeyValueInfo(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + ", " + value;
    }
}
