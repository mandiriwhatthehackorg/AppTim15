package com.myans.mandirihackathon.model;

public class AccountModel {
    private String accountName;
    private String accountNumber;
    private String bankName;

    public AccountModel() {
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public AccountModel(String accountName, String accountNumber, String bankName) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
    }
}
