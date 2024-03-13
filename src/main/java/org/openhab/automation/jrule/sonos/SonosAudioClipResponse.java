package org.openhab.automation.jrule.sonos;
import com.google.gson.annotations.SerializedName;

public class SonosAudioClipResponse {
    @SerializedName("namespace")
    private String namespace;

    @SerializedName("householdId")
    private String householdId;

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("playerId")
    private String playerId;

    @SerializedName("response")
    private String response;

    @SerializedName("success")
    private boolean success;

    @SerializedName("type")
    private String type;

    @SerializedName("_objectType")
    private String objectType;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("appId")
    private String appId;

    @SerializedName("priority")
    private String priority;

    @SerializedName("clipType")
    private String clipType;

    @SerializedName("status")
    private String status;

    @SerializedName("clipLEDBehavior")
    private String clipLEDBehavior;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getClipType() {
        return clipType;
    }

    public void setClipType(String clipType) {
        this.clipType = clipType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClipLEDBehavior() {
        return clipLEDBehavior;
    }

    public void setClipLEDBehavior(String clipLEDBehavior) {
        this.clipLEDBehavior = clipLEDBehavior;
    }

    @Override
    public String toString() {
        return "MyClass{" +
                "namespace='" + namespace + '\'' +
                ", householdId='" + householdId + '\'' +
                ", locationId='" + locationId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", response='" + response + '\'' +
                ", success=" + success +
                ", type='" + type + '\'' +
                ", objectType='" + objectType + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", appId='" + appId + '\'' +
                ", priority='" + priority + '\'' +
                ", clipType='" + clipType + '\'' +
                ", status='" + status + '\'' +
                ", clipLEDBehavior='" + clipLEDBehavior + '\'' +
                '}';
    }
}
