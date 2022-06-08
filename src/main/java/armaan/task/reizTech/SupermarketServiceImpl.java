package armaan.task.reizTech;

import armaan.task.reizTech.Exception.NotEnoughChangeException;
import armaan.task.reizTech.Exception.PayNotAcceptedException;
import armaan.task.reizTech.Exception.SoldOutException;
import armaan.task.reizTech.Model.CashRegister;
import armaan.task.reizTech.Model.Product;
import armaan.task.reizTech.Model.ProductStorage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SupermarketServiceImpl implements SupermarketService {

    private static SupermarketServiceImpl Instance = null;
    private final CashRegister cashRegister;
    private final ProductStorage productStorage;
    private static double totalAmountProvided = 0;
    private static List<Double> denominationsProvided = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private String input = "", productDesired = "";
    private double price;
    private int checkAmountFlag;

    private SupermarketServiceImpl() {
        this.cashRegister = new CashRegister(new HashMap<>());
        this.productStorage = new ProductStorage(new HashMap<>());
    }

    public static SupermarketServiceImpl getInstance() {
        if (Instance == null)
            Instance = new SupermarketServiceImpl();
        return Instance;
    }

    @Override
    public void startSale() {
        System.out.println("\nType \"Cancel\" to Cancel Transaction at any point.");
        printSeparator();
        System.out.print("Initial Product Inventory");
        System.out.print(productStorage);
        printSeparator();
        System.out.print("Initial Cash Inventory");
        System.out.print(cashRegister);
        //Infinite loop to Run Supermarket indefinitely.
        while (true) {
            if (input.equals("Cancel")) {
                System.out.println("Sale Cancelled");
                requestInputForProduct();
                input = "";
            } else
                requestInputForProduct();
            boolean isCorrect = false;
            //Check for Item availability
            while (!isCorrect && !input.equals("Cancel")) {
                input = scanner.next();
                isCorrect = checkProductEntered(input);
                productDesired = input;
                if (isCorrect) {
                    totalAmountProvided = 0;
                    denominationsProvided = new ArrayList<>();
                    price = getProductPrice(productDesired);
                    requestInputForPayment();
                    input = scanner.next();
                    checkAmountFlag = checkAmountPaid(input, price);
                }
            }
            //Check if coin or bill provided is an accepted value.
            while (checkAmountFlag != 0 && !input.equals("Cancel")) {
                if (checkAmountFlag == 1) {
                    input = scanner.next();
                    checkAmountFlag = checkAmountPaid(input, price);
                } else if (checkAmountFlag == 2) {
                    input = scanner.next();
                    checkAmountFlag = checkAmountPaid(input, price);
                }
            }
        }
    }

    @Override
    public double getProductPrice(String input) {
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        List<Product> product = productStream.filter(p -> p.getName().equals(input)).collect(Collectors.toList());
        return product.get(0).getCost();
    }

    @Override
    public boolean checkProductEntered(String item) {
        if (item.equals("Cancel")) return false;
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        List<Product> productList = productStream.filter(product -> product.getName().equals(item)).collect(Collectors.toList());
        if (productList.size() != 0) {
            try {
                isProductAvailableInProductInventory(item);
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

    @Override
    public void isProductAvailableInProductInventory(String productName) throws SoldOutException {
        if (productName.equals("Cancel")) return;
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        Product product = productStream.filter(p -> p.getName().equals(productName)).collect(Collectors.toList()).get(0);
        if (productStorage.getProductsMap().get(product) <= 0)
            throw new SoldOutException("We are sorry but the item is sold out. Buy another item.");
        else {
            System.out.printf("You are trying to buy %s. You need to pay %s.", product.getName(), product.getCost());
        }
    }

    @Override
    public void checkDenominationEntered(Double denomination) throws PayNotAcceptedException {
        Stream<Double> denominationsStream = cashRegister.getCashRegisterMap().keySet().stream();
        List<Double> denominationsList = denominationsStream.filter(denomination::equals).collect(Collectors.toList());
        if (denominationsList.size() == 0)
            throw new PayNotAcceptedException("We do not accept this value. Please provide accepted denomination values");
    }

    @Override
    public int checkAmountPaid(String input, double total) {
        if (input.equals("Cancel")) return 0;
        Pattern p = Pattern.compile("\\d+(\\.\\d+)?");
        if (p.matcher(input).matches()) {
            double amount = Double.parseDouble(input);
            try {
                checkDenominationEntered(amount);
                totalAmountProvided = new BigDecimal(amount + totalAmountProvided).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                if (totalAmountProvided == total) {
                    denominationsProvided.add(amount);
                    System.out.printf("You have provided %s. Here is your product.\n", totalAmountProvided);
                    totalAmountProvided = 0;
                    updateProductInventory(productDesired);
                    updateCashRegister();
                    return 0;
                } else if (totalAmountProvided > total) {
                    try {
                        calculateChange(totalAmountProvided, price);
                        System.out.printf("You have provided %s. Your change is %s. Here is your product.\n", totalAmountProvided,
                                new BigDecimal(totalAmountProvided - total).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
                        denominationsProvided.add(amount);
                        totalAmountProvided = 0;
                        updateProductInventory(productDesired);
                        updateCashRegister();
                        return 0;
                    } catch (NotEnoughChangeException e) {
                        totalAmountProvided -= amount;
                        System.out.println(e.getMessage());
                        requestInputForPayment();
                        return 1;
                    }
                } else {
                    denominationsProvided.add(amount);
                    System.out.printf("You have provided %s. You need to pay %s\n", totalAmountProvided,
                            new BigDecimal(total - totalAmountProvided).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
                    return 1;
                }
            } catch (PayNotAcceptedException e) {
                System.out.println(e.getMessage());
                return 2;
            }
        } else {
            System.out.println("Value not in correct format. Please enter proper numerical value accepted by the store.");
            requestInputForPayment();
            return 2;
        }
    }

    @Override
    public void updateCashRegister() {
        denominationsProvided.forEach(denomination -> cashRegister.addToCashRegister(denomination, 1));
        denominationsProvided = new ArrayList<>();
        printSeparator();
        System.out.print("Updated Cash Inventory");
        System.out.println(cashRegister);
    }


    @Override
    public void updateProductInventory(String productName) {
        productStorage.updateProductAfterSale(productName);
        printSeparator();
        productDesired = "";
        System.out.print("Updated Product Inventory");
        System.out.println(productStorage);
    }

    @Override
    public void addCashDenomination(Double denomination, int quantity) {
        cashRegister.addNewDenomination(denomination, quantity);
    }

    @Override
    public void increaseCashDenominationQuantity(double denomination, int quantity) {
        try {
            cashRegister.increaseCashDenominationQuantity(denomination, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void decreaseCashDenominationQuantity(double denomination, int quantity) {
        try {
            cashRegister.decreaseCashDenominationQuantity(denomination, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addProductType(String name, double cost, int quantity) {
        productStorage.addProductType(new Product(name, cost), quantity);
    }

    @Override
    public void increaseProductQuantity(String productName, int quantity) {
        try {
            productStorage.increaseProductQuantity(productName, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void decreaseProductQuantity(String productName, int quantity) {
        try {
            productStorage.decreaseProductQuantity(productName, quantity);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void requestInputForProduct() {
        Stream<Product> productStream = productStorage.getProductsMap().keySet().stream();
        printSeparator();
        System.out.println("What would you like to buy? Type in the name of desired product.");
        productStream.forEach(product -> System.out.printf("%s (price : %s) ", product.getName(), product.getCost()));
        printSeparator();
    }

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

    @Override
    public void printSeparator() {
        System.out.print("\n------------------------------------------------------------------------------\n");
    }

    @Override
    public void calculateChange(double amount, double total) throws NotEnoughChangeException {
        Stream<Double> denominationStream = cashRegister.getCashRegisterMap().keySet().stream();
        double change = new BigDecimal(amount - total).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        Map<Double, Integer> result = new HashMap<>();
        Map<Double, Integer> cashRegisterMapCopy = new HashMap<>(cashRegister.getCashRegisterMap());
        List<Double> denominationsList = denominationStream.sorted((d1, d2) -> Double.compare(d2, d1)).collect(Collectors.toList());
        List<Double> denominations_Copy = new ArrayList<>(denominationsList);
        denominationsList.forEach(denomination -> {
            if (cashRegister.getCashRegisterMap().get(denomination) < 1)
                denominations_Copy.remove(denomination);
        });
        for (Double denomination : denominations_Copy) {
            int counter = 0;
            double returnedMoney = 0;
            while (change >= denomination && cashRegisterMapCopy.get(denomination) > 0) {
                change = new BigDecimal(change - denomination).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                returnedMoney += denomination;
                cashRegisterMapCopy.replace(denomination, cashRegisterMapCopy.get(denomination) - 1);
                counter++;
            }
            if (returnedMoney > 0) {
                result.put(denomination, counter);
            }
        }
        if (change > 0)
            throw new NotEnoughChangeException("Insufficient change to complete transaction, Provide another bill or exact change.");
        else {
            for (Double denomination : result.keySet()) {
                System.out.printf("Value: %s, Quantity: %s\n", denomination, result.get(denomination));
                cashRegister.removeFromCashRegister(denomination, result.get(denomination));
            }
        }
    }
}
