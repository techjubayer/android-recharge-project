package com.FreeRechargeApp.android.Modules;

import com.google.gson.annotations.SerializedName;

public class ModuleUserDetails {

    @SerializedName("slNo")
    String slNo;

    @SerializedName("id")
    String id;

    @SerializedName("phone")
    String phone;

    @SerializedName("email")
    String email;

    @SerializedName("name")
    String name;

    @SerializedName("type")
    String type;

    @SerializedName("biometric")
    String biometric;

    @SerializedName("gender")
    String gender;

    @SerializedName("balance")
    String balance;

    @SerializedName("status")
    String status;

    @SerializedName("token")   // ✅ JWT token
    String token;

    // ✅ NEW FIELD
    @SerializedName("simSlotIndex")
    int simSlotIndex;

    public ModuleUserDetails(){

    }

    public ModuleUserDetails(String slNo, String id, String phone, String email, String name,
                             String type, String biometric, String gender, String balance,
                             String status, String token) {
        this.slNo = slNo;
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.name = name;
        this.type = type;
        this.biometric = biometric;
        this.gender = gender;
        this.balance = balance;
        this.status = status;
        this.token = token;
    }

    // Getters
    public String getSlNo() { return slNo; }
    public String getId() { return id; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getBiometric() { return biometric; }
    public String getGender() { return gender; }
    public String getBalance() { return balance; }
    public String getStatus() { return status; }
    public String getToken() { return token; }

    // Setters
    public void setSlNo(String slNo) { this.slNo = slNo; }

    public void setId(String id) { this.id = id; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBiometric(String biometric) { this.biometric = biometric; }
    public void setGender(String gender) { this.gender = gender; }
    public void setBalance(String balance) { this.balance = balance; }
    public void setStatus(String status) { this.status = status; }
    public void setToken(String token) { this.token = token; }
}
