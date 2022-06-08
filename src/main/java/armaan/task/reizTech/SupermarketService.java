package armaan.task.reizTech;

import armaan.task.reizTech.Exception.NotEnoughChangeException;
import armaan.task.reizTech.Exception.PayNotAcceptedException;
import armaan.task.reizTech.Exception.SoldOutException;

public interface SupermarketService {

    void startSale();

    double getProductPrice(String input);

    void checkDenominationEntered(Double denomination) throws PayNotAcceptedException;

    int checkAmountPaid(String input, double total);

    boolean checkProductEntered(String productName);

    void isProductAvailableInProductInventory(String productName) throws SoldOutException;

    void updateProductInventory(String productName) throws SoldOutException;

    void updateCashRegister();

    void addCashDenomination(Double denomination, int quantity);

    void increaseCashDenominationQuantity(double denomination, int quantity);

    void decreaseCashDenominationQuantity(double denomination, int quantity);

    void addProductType(String name, double cost, int quantity);

    void increaseProductQuantity(String productName, int quantity);

    void decreaseProductQuantity(String productName, int quantity);

    void requestInputForProduct();

    void requestInputForPayment();

    void printSeparator();

    void calculateChange(double amount, double total) throws NotEnoughChangeException;
}
