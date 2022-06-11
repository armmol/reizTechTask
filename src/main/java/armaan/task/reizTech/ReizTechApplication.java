package armaan.task.reizTech;

public class ReizTechApplication {

    public static void main(String[] args) {
        SupermarketServiceImpl supermarketServiceInstance = SupermarketServiceImpl.getInstance();
        supermarketServiceInstance.addCashDenomination(0.1, 0);
        supermarketServiceInstance.addCashDenomination(0.5, 0);
        supermarketServiceInstance.addCashDenomination(1.0, 50);
        supermarketServiceInstance.addCashDenomination(2.0, 50);
        supermarketServiceInstance.addProductType("SODA", 2.3, 10);
        supermarketServiceInstance.addProductType("BREAD", 1.1, 10);
        supermarketServiceInstance.addProductType("WINE", 2.7, 0);
        supermarketServiceInstance.increaseCashDenominationQuantity(0.1, 2);
        supermarketServiceInstance.increaseProductQuantity("SODA", 2);
        supermarketServiceInstance.decreaseCashDenominationQuantity(0.1, 1);
        supermarketServiceInstance.decreaseProductQuantity("SODA", 1);
        //Starting Supermarket Service for Sale of Products
        supermarketServiceInstance.startSale();
    }
}
