package com.agrimatch.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VehicleCreateRequest {
    @NotBlank(message = "司机姓名不能为空")
    @Size(max = 50, message = "司机姓名最多50字符")
    private String driverName;

    @NotBlank(message = "身份证号不能为空")
    @Size(max = 20, message = "身份证号最多20字符")
    private String driverIdCard;

    @NotBlank(message = "车牌号不能为空")
    @Size(max = 20, message = "车牌号最多20字符")
    private String plateNumber;

    @NotBlank(message = "司机电话不能为空")
    @Size(max = 20, message = "司机电话最多20字符")
    private String driverPhone;

    @Size(max = 30, message = "车辆类型最多30字符")
    private String vehicleType;

    @Size(max = 200, message = "备注最多200字符")
    private String remark;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverIdCard() {
        return driverIdCard;
    }

    public void setDriverIdCard(String driverIdCard) {
        this.driverIdCard = driverIdCard;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

