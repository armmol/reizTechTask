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
        supermarketServiceInstance.increaseProductQuantity("SODA", 2);
        supermarketServiceInstance.increaseCashDenominationQuantity(0.1, 10);
        supermarketServiceInstance.decreaseCashDenominationQuantity(0.1, 10);
        supermarketServiceInstance.increaseCashDenominationQuantity(0.5, 10);
        supermarketServiceInstance.decreaseProductQuantity("SODA", 1);
        //Additions and Removals of Model Class entity objects
        supermarketServiceInstance.addProductType("Removed",5,5);
        supermarketServiceInstance.removeProductType("Removed");
        supermarketServiceInstance.addCashDenomination(0.0,0);
        supermarketServiceInstance.removeDenomination(0.0);
        //Starting Supermarket Service for Sale of Products
        supermarketServiceInstance.startSale();
    }
}
