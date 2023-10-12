package com.tuan1611pupu.vishort.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User2 extends User{

    @SerializedName("roleId")
    @Expose
    private int roleId;

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
