package armaan.task.reizTech;

import armaan.task.reizTech.Exception.NotEnoughChangeException;
import armaan.task.reizTech.Exception.PayNotAcceptedException;
import armaan.task.reizTech.Exception.SoldOutException;
import armaan.task.reizTech.Model.CashRegister;
import armaan.task.reizTech.Model.Product;
import armaan.task.reizTech.Model.ProductStorage;

import javax.naming.NameAlreadyBoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SupermarketServiceImpl implements SupermarketService {

    private static SupermarketServiceImpl Instance = null;
    private CashRegister cashRegister;
    private ProductStorage productStorage;
    private final CashRegister liveCashRegister = new CashRegister(new HashMap<>());
    private static double totalAmountProvided = 0;
    private static List<Double> denominationsProvided = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private String input = "", productDesired = "";
    private double price;

    public CashRegister getCashRegister() {
        return cashRegister;
    }

    public ProductStorage getProductStorage() {
        return productStorage;
    }

    private SupermarketServiceImpl() {
        this.cashRegister = new CashRegister(new HashMap<>());
        this.productStorage = new ProductStorage(new HashMap<>());
    }

    public static SupermarketServiceImpl getInstance() {
        if (Instance == null)
            Instance = new SupermarketServiceImpl();
        return Instance;
    }


    //Required for Tests
    //Setter for Cash Register
    @Override
    public void setCashRegister(CashRegister cashRegister) {
        this.cashRegister = cashRegister;
    }

    //Required for Tests
    //Setter for Product Storage
    @Override
    public void setProductStorage(ProductStorage productStorage) {
        this.productStorage = productStorage;
    }

    /**
     * Function to print initial cash inventory and product inventory
     */
    @Override
    public void printInitialCashRegisterAndProductInventory() {
        printSeparator();
        System.out.print("Initial Product Inventory");
        System.out.print(productStorage);
        printSeparator();
        System.out.print("Initial Cash Inventory");
        System.out.print(cashRegister);
    }

    /**
     * Function to start Sale service which runs an infinite nested
     * loops to check for product validity and amount entered.
     */
    @Override
    public void startSale() {
        System.out.println("\nType \"CANCEL\" to Cancel Transaction at any point.");
        printInitialCashRegisterAndProductInventory();
        //Infinite loop to Run Supermarket indefinitely.
        while (true) {
            int checkAmountFlag;
            boolean isCorrect;
            //If Cancel is entered then the money is entered by customer is returned
            // back to them and the message for cancellation of sale is displayed
            if (input.equals("CANCEL")) {
                if (!denominationsProvided.isEmpty()) {
                    Map<Double, Integer> returnMoney = new HashMap<>();
                    printSeparator();
                    System.out.println("Here is the money you provided initially.");
                    denominationsProvided.stream().sorted().forEach(denomination -> returnMoney.putIfAbsent(denomination, 0));
                    denominationsProvided.stream().sorted().forEach(denomination ->
                            returnMoney.replace(denomination, returnMoney.get(denomination) + 1));
                    returnMoney.forEach((denomination, value) -> System.out.printf("Value: %s, Quantity: %s\n", denomination, value));
                }
                System.out.println("Sale Cancelled");
                printInitialCashRegisterAndProductInventory();
                requestInputForProduct();
                input = "";
            } else
                requestInputForProduct();
            //While loop Check for Item availability
            input = scanner.next().toUpperCase(Locale.ROOT).trim();
            isCorrect = checkProductEntered(input);
            while (!isCorrect && !input.equals("CANCEL")) {
                input = scanner.next().toUpperCase(Locale.ROOT).trim();
                isCorrect = checkProductEntered(input);
            }
            if (isCorrect) {
                requestInputForPayment();
                input = scanner.next().toUpperCase(Locale.ROOT).trim();
                checkAmountFlag = checkAmountPaid(input, price);
                //Check if coin or bill provided is an accepted value.
                while (checkAmountFlag != 0 && !input.equals("CANCEL")) {
                    if (checkAmountFlag == 1) {
                        input = scanner.next().toUpperCase(Locale.ROOT).trim();
                        checkAmountFlag = checkAmountPaid(input, price);
                    } else if (checkAmountFlag == 2) {
                        input = scanner.next().toUpperCase(Locale.ROOT).trim();
                        checkAmountFlag = checkAmountPaid(input, price);
                    }
                }
            }
        }
    }

    /**
     * Function to get product price of entered product by user
     * PARAMS -
     * productName - Name of product
     */
    @Override
    public double getProductPrice(String productName) {
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        List<Product> product = productStream.filter(p -> p.getName().equals(productName)).collect(Collectors.toList());
        return product.get(0).getCost();
    }

    /**
     * Function to check if product by user is correct and available in product inventpry
     * PARAMS -
     * productName - Name of product
     */
    @Override
    public boolean checkProductEntered(String productName) {
        if (productName.equals("CANCEL")) return false;
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        List<Product> productList = productStream.filter(product -> product.getName().equals(productName)).collect(Collectors.toList());
        if (productList.size() != 0) {
            try {
                isProductAvailableInProductInventory(productName);
                return true;
            } catch (SoldOutException e) {
                System.out.println(e.getMessage());
                return false;
            }
        } else {
            System.out.println("Product is unavailable. Are you entering the right name of product from the list? Try again.");
            return false;
        }
    }

    /**
     * Function to check if product present in product inventory
     * PARAMS -
     * productName - Name of product
     */
    @Override
    public void isProductAvailableInProductInventory(String productName) throws SoldOutException {
        if (productName.equals("CANCEL")) return;
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        List<Product> productList = productStream.filter(p -> p.getName().equals(productName)).collect(Collectors.toList());
        if (productList.size() <= 0 || productStorage.getProductsMap().get(productList.get(0)) <= 0)
            throw new SoldOutException("We are sorry but the item is sold out. Buy another item.");
        else {
            System.out.printf("You are trying to buy %s. You need to pay %s.",
                    productList.get(0).getName(), productList.get(0).getCost());
            productDesired = productName;
            totalAmountProvided = 0;
            denominationsProvided = new ArrayList<>();
            price = getProductPrice(productDesired);
        }
    }

    /**
     * Function to check if denomination present in cash inventory
     * PARAMS -
     * denomination - denomination of coin or bill
     */
    @Override
    public void checkDenominationEntered(Double denomination) throws PayNotAcceptedException {
        Stream<Double> denominationsStream = cashRegister.getCashRegisterMap().keySet().stream();
        List<Double> denominationsList = denominationsStream.filter(denomination::equals).collect(Collectors.toList());
        if (denominationsList.size() == 0)
            throw new PayNotAcceptedException("We do not accept this value. Please provide accepted denomination values");
    }

    /**
     * Function to check if denomination present in cash inventory
     * PARAMS -
     * denominationEntered - denomination of coin or bill
     * total - cost of product desired
     */
    @Override
    public int checkAmountPaid(String denominationEntered, double total) {
        if (denominationEntered.equals("CANCEL")) return 0;
        try {
            checkPatternOfValueEntered(denominationEntered);
            double amount = Double.parseDouble(denominationEntered);
            try {
                checkDenominationEntered(amount);
                totalAmountProvided = new BigDecimal(amount + totalAmountProvided)
                        .setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                if (totalAmountProvided == total) {
                    denominationsProvided.add(amount);
                    denominationsProvided.forEach(denomination ->
                            cashRegister.increaseCashDenominationQuantity(denomination, 1));
                    System.out.printf("You have provided %s. Here is your product. %s\n", totalAmountProvided, productDesired);
                    totalAmountProvided = 0;
                    updateProductInventoryAfterSale(productDesired);
                    updateCashRegister();
                    return 0;
                } else if (totalAmountProvided > total) {
                    try {
                        denominationsProvided.add(amount);
                        calculateChange(totalAmountProvided, price);
                        System.out.printf("You have provided %s. Your change is %s. Here is your product. %s\n"
                                , totalAmountProvided, new BigDecimal(totalAmountProvided - total)
                                        .setScale(2, RoundingMode.HALF_DOWN).doubleValue(), productDesired);
                        totalAmountProvided = 0;
                        updateProductInventoryAfterSale(productDesired);
                        updateCashRegister();
                        return 0;
                    } catch (NotEnoughChangeException e) {
                        denominationsProvided.remove(denominationsProvided.size() - 1);
                        liveCashRegister.decreaseCashDenominationQuantity(amount, 1);
                        totalAmountProvided -= amount;
                        System.out.print(e.getMessage() + "\nCash Inventory with Coins or Bills provided by Customer");
                        System.out.println(liveCashRegister);
                        denominationsProvided.forEach(denomination ->
                                liveCashRegister.decreaseCashDenominationQuantity(denomination, 1));
                        requestInputForPayment();
                        return 1;
                    }
                } else {
                    denominationsProvided.add(amount);
                    System.out.printf("You have provided %s. You need to pay %s\n", totalAmountProvided,
                            new BigDecimal(total - totalAmountProvided)
                                    .setScale(2, RoundingMode.HALF_DOWN).doubleValue());
                    return 1;
                }
            } catch (PayNotAcceptedException e) {
                System.out.println(e.getMessage());
                requestInputForPayment();
                return 2;
            }
        } catch (PayNotAcceptedException e) {
            System.out.println(e.getMessage());
            requestInputForPayment();
            return 2;
        }
    }

    /**
     * Function to check if denomination is in correct format
     * PARAMS -
     * inputValue - Denomination entered by user
     */
    @Override
    public void checkPatternOfValueEntered(String inputValue) throws PayNotAcceptedException {
        Pattern p = Pattern.compile("\\d+(\\.\\d+)?");
        if (!p.matcher(inputValue).matches()) {
            throw new PayNotAcceptedException("Value not in correct format." +
                    " Please enter proper numerical value accepted by the store.");
        }
    }

    /**
     * Function to update cash inventory after sale
     * PARAMS -()
     */
    @Override
    public void updateCashRegister() {
        denominationsProvided = new ArrayList<>();
        printSeparator();
        System.out.print("Updated Cash Inventory");
        System.out.println(cashRegister);
    }

    /**
     * Function to update product inventory after sale
     * PARAMS -
     * productName - Name of product
     */
    @Override
    public void updateProductInventoryAfterSale(String productName) {
        productStorage.updateProductAfterSale(productName);
        printSeparator();
        productDesired = "";
        System.out.print("Updated Product Inventory");
        System.out.println(productStorage);
    }

    /**
     * Function to add denomination to cash registry
     * PARAMS -
     * denomination - denomination of coin or bill
     * quantity - Quantity to add
     */
    @Override
    public void addCashDenomination(Double denomination, int quantity) {
        try {
            cashRegister.addNewDenomination(denomination, quantity);
        } catch (NameAlreadyBoundException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to remove denomination from cash registry
     * PARAMS -
     * denomination - denomination of coin or bill
     */
    @Override
    public void removeDenomination(Double denomination) {
        try {
            cashRegister.removeDenomination(denomination);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to increase denomination quantity in cash registry
     * PARAMS -()
     * denomination - denomination of coin or bill
     * quantity - Quantity to increase by
     */
    @Override
    public void increaseCashDenominationQuantity(double denomination, int quantity) {
        try {
            cashRegister.increaseCashDenominationQuantity(denomination, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to decrease denomination quantity in cash registry
     * PARAMS -
     * denomination - denomination of coin or bill
     * quantity - Quantity to decrease by
     */
    @Override
    public void decreaseCashDenominationQuantity(double denomination, int quantity) {
        try {
            cashRegister.decreaseCashDenominationQuantity(denomination, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to add product to product storage
     * PARAMS -
     * productName - Name of product
     * cost - Cost of product
     * quantity - Quantity of product to be added
     */
    @Override
    public void addProductType(String productName, double cost, int quantity) {
        try {
            productStorage.addProductType(new Product(productName.toUpperCase(Locale.ROOT), cost), quantity);
        } catch (NameAlreadyBoundException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Function to remove product from product storage
     * PARAMS -
     * productName - Name of product
     */
    @Override
    public void removeProductType(String productName) {
        try {
            productStorage.removeProduct(productName);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Function to increase product quantity in product storage
     * PARAMS -
     * productName - Name of product
     * quantity - Quantity to increase by
     */
    @Override
    public void increaseProductQuantity(String productName, int quantity) {
        try {
            productStorage.increaseProductQuantity(productName.toUpperCase(Locale.ROOT), quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to decrease product quantity in product storage
     * PARAMS -
     * productName - Name of product
     * quantity - Quantity to decrease by
     */
    @Override
    public void decreaseProductQuantity(String productName, int quantity) {
        try {
            productStorage.decreaseProductQuantity(productName.toUpperCase(Locale.ROOT), quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to print products in Supermarket and request for input
     * PARAMS -()
     */
    @Override
    public void requestInputForProduct() {
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        printSeparator();
        System.out.println("What would you like to buy? Type in the name of desired product.");
        productStream.forEach(product -> System.out.printf("%s (price : %s) ", product.getName(), product.getCost()));
        printSeparator();
    }

    /**
     * Function to print denominations accepted by Supermarket and request for payment
     * PARAMS -()
     */
    @Override
    public void requestInputForPayment() {
        Stream<Double> cashRegisterStream = cashRegister.getCashRegisterMap().keySet().stream();
        printSeparator();
        System.out.print("Provide Bill or Coins (Accepted values: ");
        AtomicInteger counter = new AtomicInteger(0);
        cashRegisterStream.sorted().forEach(value -> {
            if (Double.parseDouble(Double.toString(value).split("\\.")[1]) == 0) {
                if (counter.get() == cashRegister.getCashRegisterMap().size() - 1)
                    System.out.print(value.intValue() + ")");
                else
                    System.out.print(value.intValue() + ", ");
            } else {
                if (counter.get() == cashRegister.getCashRegisterMap().size() - 1)
                    System.out.print(value + ")");
                else
                    System.out.print(value + ", ");
            }
            counter.set(counter.get() + 1);
        });
        printSeparator();
    }

    /**
     * Function to print a separator block
     * PARAMS -()
     */
    @Override
    public void printSeparator() {
        System.out.print("\n------------------------------------------------------------------------------\n");
    }

    /**
     * Calculate Change function to calculate the change returned or to throw an error for insufficient change
     * PARAMS -
     * amount - most recent amount, coin or bill, provided by customer
     * total - Total amount needed to be paid for desired product
     */
    @Override
    public void calculateChange(double amount, double total) throws NotEnoughChangeException {
        //calculating change needed to be returned
        double change = new BigDecimal(amount - total).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        //Map to store result of change returned
        Map<Double, Integer> result = new HashMap<>();
        //Map to store temporary copy of cashRegister object map
        Map<Double, Integer> cashRegisterMapCopy = new HashMap<>(cashRegister.getCashRegisterMap());
        //Adding current denominations provided to cash register map copy to provide change from it if needed
        denominationsProvided.forEach(denomination ->
                cashRegisterMapCopy.replace(denomination, cashRegisterMapCopy.get(denomination) + 1));
        //Changing value of live cash register
        liveCashRegister.setCashRegisterMap(new HashMap<>(cashRegisterMapCopy));
        //List of all denominations coins or bills in live cash register in descending order
        List<Double> denominationsList = liveCashRegister.getCashRegisterMap().keySet().stream().sorted((d1, d2) ->
                Double.compare(d2, d1)).collect(Collectors.toList());
        List<Double> denominations_Copy = new ArrayList<>(denominationsList);
        //Removing denominations with quantity 0
        denominationsList.forEach(denomination -> {
            if (cashRegisterMapCopy.get(denomination) < 1)
                denominations_Copy.remove(denomination);
        });
        //Changing value of result and counter based on the presence of enough change in the cash register
        // and the change to be returned not equal to zero
        for (Double denomination : denominations_Copy) {
            int counter = 0;
            double returnedMoney = 0;
            while (change >= denomination && cashRegisterMapCopy.get(denomination) > 0) {
                change = new BigDecimal(change - denomination).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                returnedMoney += denomination;
                cashRegisterMapCopy.replace(denomination, cashRegisterMapCopy.get(denomination) - 1);
                counter++;
            }
            //Checking if money will be returned and adding the denominations and quantities of those bills and coins to
            //result map
            if (returnedMoney > 0) {
                result.put(denomination, counter);
            }
        }
        //If block to check if change is 0 meaning the cash register has enough change and the
        // transaction will be completed else it throws an error for not enough change
        if (change > 0) {
            throw new NotEnoughChangeException("Insufficient change to complete transaction, Provide another bill or exact change.");
        } else {
            cashRegister.setCashRegisterMap(cashRegisterMapCopy);
            printSeparator();
            System.out.println("Change");
            result.keySet().forEach(
                    denomination -> System.out.printf("Value: %s, Quantity: %s\n", denomination, result.get(denomination)));
        }
    }
}
