package armaan.task.reizTech.Model;

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

    public void addNewDenomination(Double denomination, int quantity) {
        cashRegisterMap.put(denomination, quantity);
    }

    public void removeFromCashRegister(Double denomination, int quantity) {
        cashRegisterMap.put(denomination, cashRegisterMap.get(denomination) - quantity);
    }

    public void addToCashRegister(Double denomination, int quantity) {
        cashRegisterMap.replace(denomination, cashRegisterMap.get(denomination) + quantity);
    }

    public void increaseCashDenominationQuantity(double denomination, int quantity) throws NullPointerException {
        List<Double> denominationList = cashRegisterMap.keySet().stream().filter(d -> d == denomination).collect(Collectors.toList());
        if (denominationList.size() != 0)
            cashRegisterMap.replace(denomination, cashRegisterMap.get(denomination) + quantity);
        else
            throw new NullPointerException(denomination + " Denomination not present in Cash Register. Try adding as new type.\n");
    }

    public void decreaseCashDenominationQuantity(double denomination, int quantity) throws NullPointerException {
        List<Double> denominationList = cashRegisterMap.keySet().stream().filter(d -> d == denomination).collect(Collectors.toList());
        if (denominationList.size() != 0) {
            if (cashRegisterMap.get(denominationList.get(0)) > quantity)
                cashRegisterMap.replace(denomination, cashRegisterMap.get(denomination) - quantity);
            else
                throw new NullPointerException(denomination + " Denomination quantity("
                        + cashRegisterMap.get(denominationList.get(0)) + ") is less that decrease amount\n");
        } else
            throw new NullPointerException(denomination + " Denomination not present in Cash Register. Try adding as new type.\n");
    }

    @Override
    public String toString() {
        Stream<Double> stream = cashRegisterMap.keySet().stream();
        AtomicReference<String> returnString = new AtomicReference<>("");
        stream.forEach(denomination ->
                returnString.set(returnString.get() + "\n" +
                        "Value: " + denomination + ", Quantity: " + cashRegisterMap.get(denomination)));
        return returnString.get();
    }
}
