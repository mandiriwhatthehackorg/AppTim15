package com.myans.mandirihackathon.model;

public class TransferModel {
    private AccountModel sourceAccount;
    private AccountModel destAccount;
    private String amount;

    public TransferModel(AccountModel sourceAccount, AccountModel destAccount, String amount) {
        this.sourceAccount = sourceAccount;
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public TransferModel() {
    }

    public AccountModel getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(AccountModel sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public AccountModel getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(AccountModel destAccount) {
        this.destAccount = destAccount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
