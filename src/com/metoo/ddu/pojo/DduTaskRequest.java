package com.metoo.ddu.pojo;

import java.math.BigDecimal;

/**
 * <p>
 * Title: TaskRequest.java
 * </p>
 * 
 * <p>
 * Company: metoo
 * </p>
 * 
 * <p>
 * Description: 该实体类来自ddu提供的接口 
 * </p>
 * 
 * @author hu
 * 
 * @date 2019-10-10
 * 
 * @version metoo b2b2c 2019
 *
 */
public class DduTaskRequest {
	private String batchNumber;		
	private String fromCompany;			
	private String fromAddress;		
	private String fromLocation;		
	private String fromCountry;		
	private String fromCperson;		
	private String fromContactno;		 
	private String fromMobileno;		
	private String toCompany;		
	private String toAddress;		
	private String toLocation;		
	private String toCountry;		
	private String toCperson;		
	private String toContactno;		 
	private String toMobileno;		
	private String referenceNumber;		
	private String companyCode;		
	private int weight;			
	private int pieces;			
	private String packageType;		
	private String currencyCode;		
	private BigDecimal ncndAmount;		
	private String itemDescription;	
	private String specialInstruction;
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getFromCompany() {
		return fromCompany;
	}
	public void setFromCompany(String fromCompany) {
		this.fromCompany = fromCompany;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getFromLocation() {
		return fromLocation;
	}
	public void setFromLocation(String fromLocation) {
		this.fromLocation = fromLocation;
	}
	public String getFromCountry() {
		return fromCountry;
	}
	public void setFromCountry(String fromCountry) {
		this.fromCountry = fromCountry;
	}
	public String getFromCperson() {
		return fromCperson;
	}
	public void setFromCperson(String fromCperson) {
		this.fromCperson = fromCperson;
	}
	public String getFromContactno() {
		return fromContactno;
	}
	public void setFromContactno(String fromContactno) {
		this.fromContactno = fromContactno;
	}
	public String getFromMobileno() {
		return fromMobileno;
	}
	public void setFromMobileno(String fromMobileno) {
		this.fromMobileno = fromMobileno;
	}
	public String getToCompany() {
		return toCompany;
	}
	public void setToCompany(String toCompany) {
		this.toCompany = toCompany;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public String getToLocation() {
		return toLocation;
	}
	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}
	public String getToCountry() {
		return toCountry;
	}
	public void setToCountry(String toCountry) {
		this.toCountry = toCountry;
	}
	public String getToCperson() {
		return toCperson;
	}
	public void setToCperson(String toCperson) {
		this.toCperson = toCperson;
	}
	public String getToContactno() {
		return toContactno;
	}
	public void setToContactno(String toContactno) {
		this.toContactno = toContactno;
	}
	public String getToMobileno() {
		return toMobileno;
	}
	public void setToMobileno(String toMobileno) {
		this.toMobileno = toMobileno;
	}
	public String getReferenceNumber() {
		return referenceNumber;
	}
	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}
	public String getCompanyCode() {
		return companyCode;
	}
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getPieces() {
		return pieces;
	}
	public void setPieces(int pieces) {
		this.pieces = pieces;
	}
	public String getPackageType() {
		return packageType;
	}
	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public BigDecimal getNcndAmount() {
		return ncndAmount;
	}
	public void setNcndAmount(BigDecimal ncndAmount) {
		this.ncndAmount = ncndAmount;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public String getSpecialInstruction() {
		return specialInstruction;
	}
	public void setSpecialInstruction(String specialInstruction) {
		this.specialInstruction = specialInstruction;
	}
	
}
