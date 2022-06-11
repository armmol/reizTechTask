package armaan.task.reizTech;

import armaan.task.reizTech.Exception.NotEnoughChangeException;
import armaan.task.reizTech.Exception.PayNotAcceptedException;
import armaan.task.reizTech.Exception.SoldOutException;
import armaan.task.reizTech.Model.CashRegister;
import armaan.task.reizTech.Model.Product;
import armaan.task.reizTech.Model.ProductStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SupermarketServiceImplTest {

    private SupermarketServiceImpl instance;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private Product product;

    @BeforeEach
    void setup() {
        instance = SupermarketServiceImpl.getInstance();
        instance.addProductType("TEST", 2.1, 10);
        instance.addCashDenomination(0.1, 0);
        instance.addCashDenomination(0.5, 10);
        instance.addCashDenomination(1.0, 10);
        instance.addCashDenomination(2.0, 10);
        product = new Product("TEST", 2.1);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        instance = null;
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void printInitialCashRegisterAndProductInventory() {
        instance.printInitialCashRegisterAndProductInventory();
        assertEquals("------------------------------------------------------------------------------\n" +
                "Initial Product Inventory\n" +
                "TEST Quantity: 8\n" +
                "------------------------------------------------------------------------------\n" +
                "Initial Cash Inventory\n" +
                "Value: 0.1, Quantity: 2\n" +
                "Value: 0.5, Quantity: 10\n" +
                "Value: 1.0, Quantity: 10\n" +
                "Value: 2.0, Quantity: 11",
                outputStreamCaptor.toString().trim());
    }

    @Test
    void getProductPrice() {

        assertEquals(instance.getProductPrice("TEST"), 2.1);
    }

    @Test
    void checkProductEntered() {
        assertTrue(instance.checkProductEntered("TEST"));
        assertFalse(instance.checkProductEntered("Wrong Item"));
    }

    @Test
    void isProductAvailableInProductInventory() throws SoldOutException {
        instance.isProductAvailableInProductInventory("TEST");
        assertEquals(String.format("You are trying to buy %s. You need to pay %s.", product.getName(), product.getCost()), outputStreamCaptor.toString().trim());
        assertThrows(SoldOutException.class, () -> instance.isProductAvailableInProductInventory("Wrong Input"));
    }

    @Test
    void checkDenominationEntered() {
        assertThrows(PayNotAcceptedException.class, () -> instance.checkDenominationEntered(5.0));
        assertDoesNotThrow(() -> instance.checkDenominationEntered(0.1));
    }

    @Test
    void checkAmountPaid() {
        instance.checkProductEntered("TEST");
        //Amount Paid Equal to price
        assertEquals(1, instance.checkAmountPaid("2.0", 2.1));
        assertEquals(0, instance.checkAmountPaid("0.1", 2.1));
        instance.checkAmountPaid("CANCEL", 2.0);
        //Amount paid less than price
        assertEquals(1, instance.checkAmountPaid("1", 2.1));
        instance.checkAmountPaid("CANCEL", 2.0);
        //Amount paid more than price but Supermarket does not have enough change
        assertEquals(1, instance.checkAmountPaid("2", 2.1));
        assertEquals(1, instance.checkAmountPaid("2", 2.1));
        instance.checkAmountPaid("CANCEL", 2.0);
        //Amount is not in accepted by Supermarket
        assertEquals(2, instance.checkAmountPaid("5", 2.0));
        instance.checkAmountPaid("CANCEL", 2.0);
    }

    @Test
    void updateCashRegister() {
        instance.checkProductEntered("TEST");
        instance.checkAmountPaid("2.0", 2.1);
        instance.updateCashRegister();
        assertEquals(instance.getCashRegister().getCashRegisterMap().get(2.0), 11);
    }

    @Test
    void updateProductInventory() {
        instance.updateProductInventoryAfterSale("TEST");
        List<Product> productList = new ArrayList<>(instance.getProductStorage().getProductsMap().keySet());
        assertEquals(9, instance.getProductStorage().getProductsMap().get(productList.get(0)));
    }

    @Test
    void addCashDenomination() {
        //Valid addition of new Denomination
        instance.addCashDenomination(25.0, 0);
        assertEquals(0, instance.getCashRegister().getCashRegisterMap().get(25.0));
        //Invalid addition of new Denomination because Denomination is already present in Cash Inventory
        instance.addCashDenomination(25.0, 5);
        assertEquals("25.0 Denomination already present in Cash Inventory.", outputStreamCaptor.toString().trim());
        instance.removeDenomination(25.0);
    }

    @Test
    void increaseCashDenominationQuantity() {
        //Increase Amount is valid for denomination present in Cash Inventory
        instance.addCashDenomination(25.0, 0);
        instance.increaseCashDenominationQuantity(25.0, 5);
        assertEquals(5, instance.getCashRegister().getCashRegisterMap().get(25.0));
        //Increase amount is invalid because denomination is absent from Cash Inventory
        instance.increaseCashDenominationQuantity(30.0, 55);
        assertEquals("30.0 Denomination not present in Cash Inventory. Try adding as new type.", outputStreamCaptor.toString().trim());
        instance.removeDenomination(25.0);
    }

    @Test
    void decreaseCashDenominationQuantity() {
        //Decrease amount less than quantity in cash inventory
        instance.addCashDenomination(25.0, 0);
        instance.decreaseCashDenominationQuantity(25.0, 5);
        assertEquals("25.0 Denomination quantity(0) is less than decrease amount", outputStreamCaptor.toString().trim());
        //Decrease amount more than quantity in cash inventory
        instance.increaseCashDenominationQuantity(25.0, 5);
        instance.decreaseCashDenominationQuantity(25.0, 3);
        assertEquals(2, instance.getCashRegister().getCashRegisterMap().get(25.0));
        instance.removeDenomination(25.0);
    }

    @Test
    void addProductType() {
        //Valid addition of new Product
        List<Product> productList = instance.getProductStorage().getProductsMap().keySet()
                .stream().filter(p -> p.getName().equals("TEST")).collect(Collectors.toList());
        assertEquals(9, instance.getProductStorage().getProductsMap().get(productList.get(0)));
        //Invalid addition of new Denomination because Denomination is already present in Cash Inventory
        instance.addProductType("test", 5, 5);
        assertEquals("TEST Product already present in Product Inventory.", outputStreamCaptor.toString().trim());
    }

    @Test
    void increaseProductQuantity() {
        //Increase Amount is valid for product present in Product Inventory
        List<Product> productList = instance.getProductStorage().getProductsMap().keySet()
                .stream().filter(p -> p.getName().equals("TEST")).collect(Collectors.toList());
        assertEquals(10, instance.getProductStorage().getProductsMap().get(productList.get(0)));
        //Increase amount is invalid because Product is absent from Product Inventory
        instance.increaseProductQuantity("Wrong Input", 55);
        assertEquals("WRONG INPUT not present in Product Inventory. Try adding as new type.", outputStreamCaptor.toString().trim());
    }

    @Test
    void decreaseProductQuantity() {
        //Decrease amount less than quantity in Product Inventory
        instance.addProductType("TESTA",0,2);
        instance.decreaseProductQuantity("TESTA", 10);
        assertEquals("TESTA quantity(2) in Product Inventory is less that decrease amount.", outputStreamCaptor.toString().trim());
        //Decrease amount more than quantity in Product Inventory
        instance.increaseProductQuantity("TESTA", 5);
        instance.decreaseProductQuantity("TESTA", 3);
        List<Product> productList = instance.getProductStorage().getProductsMap().keySet()
                .stream().filter(p -> p.getName().equals("TESTA")).collect(Collectors.toList());
        assertEquals(4, instance.getProductStorage().getProductsMap().get(productList.get(0)));
        instance.removeProductType("TESTA");
    }

    @Test
    void checkPatternOfValueEntered() {
        assertThrows(PayNotAcceptedException.class, () -> instance.checkPatternOfValueEntered("sgsgs"));
    }

    @Test
    void requestInputForProduct() {
        instance.requestInputForProduct();
        assertTrue(outputStreamCaptor.toString().trim().contains("TEST (price : 2.1)"));
    }

    @Test
    void requestInputForPayment() {
        instance.requestInputForPayment();
        assertEquals("------------------------------------------------------------------------------\n" +
                "Provide Bill or Coins (Accepted values: 0.1, 0.5, 1, 2)\n" +
                "------------------------------------------------------------------------------", outputStreamCaptor.toString().trim());
    }

    @Test
    void printSeparator() {
        instance.printSeparator();
        assertEquals("------------------------------------------------------------------------------", outputStreamCaptor.toString().trim());
    }

    @Test
    void calculateChange() throws NotEnoughChangeException {
        //Adding cash denomination for change
        instance.increaseCashDenominationQuantity(0.1, 5);
        //Change available
        instance.calculateChange(2.5, 2.1);
        assertEquals(1, instance.getCashRegister().getCashRegisterMap().get(0.1));
        //Change unavailable because of absence of enough cash denominations of certain type in Cash Inventory
        assertThrows(NotEnoughChangeException.class, () -> instance.calculateChange(3, 2.1));
    }
}