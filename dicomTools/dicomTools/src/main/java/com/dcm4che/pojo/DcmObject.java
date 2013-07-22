package com.dcm4che.pojo;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-10
 * Time: 下午3:04
 * To change this template use File | Settings | File Templates.
 */
public class DcmObject {
    public String classUID;    //0002,0002
    public String instanceUID;  //0002,0003
    public String transferSyntaxUID;  //0002,0010
    public String implementationClassUID; //0002,0012
    public String specificCharacteSet;  //0008,0005
    public String studyDate;                //0008,0020
    public String seriesDate;              // 0008,0021
    public String acquisitionDate;        // 0008,0022
    public String contentDate;             // 0008,0023
    public String studyTime;                //0008,0030
    public String seriesTime;              // 0008,0031
    public String acquisitionTime;        // 0008,0032
    public String contentTime;             // 0008,0033
    public String accessionNumber;        //0008,0050
    public String modaliType;              //0008,0060
    public String conversionType;         //0008,0064
    public String manufacturer;           //0008,0070
    public String institutionName;        //0008,0080
    public String stationName;             //0008,1010
    public String studyDescription;        //0008,1030
    public String patientName;              //0010,0010
    public String patientID ;               // 0010,0020
    public String birthDate;                //0010,0030
    public String patientSex;               //0010,0040
    public String patientAge;                // 0010,1010
    public String studyUID;                 // 0020,000D
    public String seriesUID;                // 0020,000E
    public String studyID;                   // 0020,0010
    public String seriesNumber;             //0020,0011
    public String instanceNumber;           //0020,0013

    public String getClassUID() {
        return classUID;
    }

    public void setClassUID(String classUID) {
        this.classUID = classUID;
    }

    public String getInstanceUID() {
        return instanceUID;
    }

    public void setInstanceUID(String instanceUID) {
        this.instanceUID = instanceUID;
    }

    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    public void setTransferSyntaxUID(String transferSyntaxUID) {
        this.transferSyntaxUID = transferSyntaxUID;
    }

    public String getImplementationClassUID() {
        return implementationClassUID;
    }

    public void setImplementationClassUID(String implementationClassUID) {
        this.implementationClassUID = implementationClassUID;
    }

    public String getSpecificCharacteSet() {
        return specificCharacteSet;
    }

    public void setSpecificCharacteSet(String specificCharacteSet) {
        this.specificCharacteSet = specificCharacteSet;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getSeriesDate() {
        return seriesDate;
    }

    public void setSeriesDate(String seriesDate) {
        this.seriesDate = seriesDate;
    }

    public String getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(String acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getContentDate() {
        return contentDate;
    }

    public void setContentDate(String contentDate) {
        this.contentDate = contentDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getSeriesTime() {
        return seriesTime;
    }

    public void setSeriesTime(String seriesTime) {
        this.seriesTime = seriesTime;
    }

    public String getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(String acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public String getContentTime() {
        return contentTime;
    }

    public void setContentTime(String contentTime) {
        this.contentTime = contentTime;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getModaliType() {
        return modaliType;
    }

    public void setModaliType(String modaliType) {
        this.modaliType = modaliType;
    }

    public String getConversionType() {
        return conversionType;
    }

    public void setConversionType(String conversionType) {
        this.conversionType = conversionType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }
}
