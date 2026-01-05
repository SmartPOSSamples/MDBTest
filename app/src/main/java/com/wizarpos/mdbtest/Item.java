package com.wizarpos.mdbtest;

import java.util.Locale;

public class Item {
	private String itemId;
	private String itemAmount;//Before calculate
	private String priceX;
	private String priceY;
	private String itemPrice;//After calculate

	public Item(){
		itemId = "";
		itemPrice = "";
		itemAmount = "";
		priceX = "01";
		priceY = "02";
	}

	//金额计算公式 ActualPrice = P *X*10 ^(-Y) p:price,amount
	//MDB resp:0101020086"0101"00048f 这里x= 01的十进制1, y= 01的十进制1
	//0101021156"0102"590dd3
	public void calculatePrice(){
		int p = Integer.parseInt(itemAmount, 16);
		int x = Integer.parseInt(priceX, 16);
		int y = Integer.parseInt(priceY, 16);
		double yy = Math.pow(10, -y);
		double actualPrice = p * x * yy;
        /*Log.d("aaa", "amounthex= " + amounthex
                         +" p= " + p
                         +" x= " + x
                         +" y= " + y
                         +" yy = " + yy
                         +" actualPrice = " + String.format(Locale.CHINA,"%.2f",actualPrice));*/
		itemPrice = String.format(Locale.CHINA,"%.2f",actualPrice);
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(String itemPrice) {
		this.itemPrice = itemPrice;
	}

	public String getItemAmount() {
		return itemAmount;
	}

	public void setItemAmount(String itemAmount) {
		this.itemAmount = itemAmount;
	}

	public String getPriceX() {
		return priceX;
	}

	public void setPriceX(String priceX) {
		this.priceX = priceX;
	}

	public String getPriceY() {
		return priceY;
	}

	public void setPriceY(String priceY) {
		this.priceY = priceY;
	}

	@Override
	public String toString() {
		return "Item{" +
				"itemId='" + itemId + '\'' +
				", itemAmount='" + itemAmount + '\'' +
				", priceX='" + priceX + '\'' +
				", priceY='" + priceY + '\'' +
				", itemPrice='" + itemPrice + '\'' +
				'}';
	}
}
