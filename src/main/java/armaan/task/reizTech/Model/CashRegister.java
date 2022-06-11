package armaan.task.reizTech.Model;

import javax.naming.NameAlreadyBoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CashRegister {

    Map<Double, Integer> cashRegisterMap;

    public CashRegister(Map<Double, Integer> cashRegisterMap) {
        this.cashRegisterMap = cashRegisterMap;
    }

    public Map<Double, Integer> getCashRegisterMap() {
        return cashRegisterMap;
    }

    public void setCashRegisterMap(Map<Double, Integer> cashRegisterMap) {
        this.cashRegisterMap = cashRegisterMap;
    }

    public void addNewDenomination(Double denomination, int quantity) throws NameAlreadyBoundException {
        if (cashRegisterMap.get(denomination) == null)
            cashRegisterMap.put(denomination, quantity);
        else
            throw new NameAlreadyBoundException(denomination+" Denomination already present in Cash Inventory.");
    }

    public void removeDenomination(Double denomination) throws NullPointerException {
        if (cashRegisterMap.get(denomination) != null)
            cashRegisterMap.remove(denomination);
        else
            throw new NullPointerException(denomination+" Denomination not present in Cash Inventory.");
    }

    public void increaseCashDenominationQuantity(double denomination, int quantity) throws NullPointerException {
        List<Double> denominationList = cashRegisterMap.keySet().stream().filter(d -> d == denomination).collect(Collectors.toList());
        if (denominationList.size() != 0)
            cashRegisterMap.replace(denomination, cashRegisterMap.get(denomination) + quantity);
        else
            throw new NullPointerException(denomination + " Denomination not present in Cash Inventory. Try adding as new type.\n");
    }

    public void decreaseCashDenominationQuantity(double denomination, int quantity) throws NullPointerException {
        List<Double> denominationList = cashRegisterMap.keySet().stream().filter(d -> d == denomination).collect(Collectors.toList());
        if (denominationList.size() != 0) {
            if (cashRegisterMap.get(denomination) >= quantity)
                cashRegisterMap.replace(denomination, cashRegisterMap.get(denomination) - quantity);
            else
                throw new NullPointerException(denomination + " Denomination quantity("
                        + cashRegisterMap.get(denomination) + ") is less than decrease amount\n");
        } else
            throw new NullPointerException(denomination + " Denomination not present in Cash Inventory. Try adding as new type.\n");
    }

    @Override
    public String toString() {
        Stream<Double> stream = cashRegisterMap.keySet().stream();
        AtomicReference<String> returnString = new AtomicReference<>("");
        stream.sorted().forEach(denomination ->
                returnString.set(returnString.get() + "\n" +
                        "Value: " + denomination + ", Quantity: " + cashRegisterMap.get(denomination)));
        return returnString.get();
    }
}
