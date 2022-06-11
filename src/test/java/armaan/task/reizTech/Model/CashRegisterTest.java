package armaan.task.reizTech.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.NameAlreadyBoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Cash Register
 * Test Methods correspond to function names and are self explanatory
 */
class CashRegisterTest {

    private CashRegister cashRegister;
    private final Map<Double, Integer> cashRegisterMap = new HashMap<>();

    @BeforeEach
    void setup() {
        cashRegister = new CashRegister(cashRegisterMap);
    }

    @Test
    void getCashRegisterMap() {
        assertEquals(cashRegister.getCashRegisterMap(), cashRegisterMap);
    }

    @Test
    void addNewDenomination() throws NameAlreadyBoundException {
        //Valid addition of new Denomination
        cashRegister.addNewDenomination(0.1, 10);
        assertEquals(cashRegisterMap.get(0.1), 10);
        //Invalid addition of new Denomination because Denomination is already present in Cash Inventory
        assertThrows(NameAlreadyBoundException.class, () -> cashRegister.addNewDenomination(0.1, 10));
    }

    @Test
    void removeDenomination() throws NameAlreadyBoundException {
        //Valid removal of  Denomination
        cashRegister.addNewDenomination(0.1, 10);
        cashRegister.removeDenomination(0.1);
        assertNull(cashRegisterMap.get(0.1));
        //Invalid removal of  Denomination because Denomination is not present in Cash Inventory
        assertThrows(NullPointerException.class, () -> cashRegister.removeDenomination(0.1));
    }

    @Test
    void increaseCashDenominationQuantity() throws NameAlreadyBoundException {
        //Increase Amount is valid for denomination present in Cash Inventory
        cashRegister.addNewDenomination(0.1, 10);
        cashRegister.increaseCashDenominationQuantity(0.1, 10);
        assertEquals(cashRegisterMap.get(0.1), 20);
        //Increase amount is invalid because denomination is absent from Cash Inventory
        assertThrows(NullPointerException.class,
                () -> cashRegister.increaseCashDenominationQuantity(30.0, 55));
    }

    @Test
    void decreaseCashDenominationQuantity() throws NameAlreadyBoundException {
        //Decrease amount less than quantity in cash inventory
        cashRegister.addNewDenomination(25.0, 0);
        assertThrows(NullPointerException.class, () -> cashRegister.decreaseCashDenominationQuantity(25.0, 5));
        //Decrease amount more than quantity in cash inventory
        cashRegister.increaseCashDenominationQuantity(25.0, 5);
        cashRegister.decreaseCashDenominationQuantity(25.0, 3);
        assertEquals(2, cashRegisterMap.get(25.0));
    }
}