package armaan.task.reizTech;

import armaan.task.reizTech.Exception.NotEnoughChangeException;
import armaan.task.reizTech.Exception.PayNotAcceptedException;
import armaan.task.reizTech.Exception.SoldOutException;
import armaan.task.reizTech.Model.CashRegister;
import armaan.task.reizTech.Model.ProductStorage;

public interface SupermarketService {

    //Required for Tests
    void setCashRegister(CashRegister cashRegister);

    //Required for Tests
    void setProductStorage(ProductStorage productStorage);

    void printInitialCashRegisterAndProductInventory();

    void startSale();

    double getProductPrice(String productName);

    void checkDenominationEntered(Double denomination) throws PayNotAcceptedException;

    int checkAmountPaid(String input, double total);

    void checkPatternOfValueEntered(String inputValue) throws PayNotAcceptedException;

    boolean checkProductEntered(String productName);

    void isProductAvailableInProductInventory(String productName) throws SoldOutException;

    void updateProductInventoryAfterSale(String productName) throws SoldOutException;

    void updateCashRegister();

    void addCashDenomination(Double denomination, int quantity);

    void removeDenomination(Double denomination);

    void increaseCashDenominationQuantity(double denomination, int quantity);

    void decreaseCashDenominationQuantity(double denomination, int quantity);

    void addProductType(String productName, double cost, int quantity);

    void removeProductType(String productName);

    void increaseProductQuantity(String productName, int quantity);

    void decreaseProductQuantity(String productName, int quantity);

    void requestInputForProduct();

    void requestInputForPayment();

    void printSeparator();

    void calculateChange(double amount, double total) throws NotEnoughChangeException;
}
