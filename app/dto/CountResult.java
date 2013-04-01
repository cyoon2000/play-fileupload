package dto;

import java.lang.Integer;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountResult {

    private String fileId;

    private Map<String, Integer> finalResultMap = new HashMap<String, Integer>();

    public String getFileId() {
        return fileId;
    }

    public CountResult(String fileId) {
        this.fileId = fileId;
    }

    public Map<String, Integer> getFinalResultMap() {
        return finalResultMap;
    }

    public void setFinalResultMap(Map<String, Integer> finalResultMap) {
        this.finalResultMap = finalResultMap;
    }
}