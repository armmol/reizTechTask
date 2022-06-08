package armaan.task.reizTech.Model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductStorage {
    Map<Product, Integer> productsMap;

    public ProductStorage(Map<Product, Integer> productsMap) {
        this.productsMap = productsMap;
    }

    public Map<Product, Integer> getProductsMap() {
        return productsMap;
    }

    public void addProductType(Product product, int quantity) {
        productsMap.put(product, quantity);
    }

    public void updateProductAfterSale(String productName) {
        Stream<Product> productStream = productsMap.keySet().stream();
        Product product = productStream.filter(product1 -> product1.getName().equals(productName)).collect(Collectors.toList()).get(0);
        productsMap.replace(product, productsMap.get(product) - 1);
    }

    public void increaseProductQuantity(String productName, int quantity) throws NullPointerException {
        Stream<Product> productStream = productsMap.keySet().stream();
        List<Product> product = productStream.filter(product1 -> product1.getName().equals(productName)).collect(Collectors.toList());
        if (product.size() != 0)
            productsMap.replace(product.get(0), productsMap.get(product.get(0)) + quantity);
        else
            throw new NullPointerException(productName + " not present in Product Inventory. Try adding as new type.\n");
    }

    public void decreaseProductQuantity(String productName, int quantity) throws NullPointerException {
        Stream<Product> productStream = productsMap.keySet().stream();
        List<Product> product = productStream.filter(product1 -> product1.getName().equals(productName)).collect(Collectors.toList());
        if (product.size() != 0) {
            if (productsMap.get(product.get(0)) > quantity)
                productsMap.replace(product.get(0), productsMap.get(product.get(0)) - quantity);
            else
                throw new NullPointerException(productName + " quantity("
                        + productsMap.get(product.get(0)) + ") in Product Inventory is less that decrease amount.\n");
        } else
            throw new NullPointerException(productName + " not present in Product Inventory. Try adding as new type.\n");
    }

    @Override
    public String toString() {
        Stream<Product> stream = productsMap.keySet().stream();
        AtomicReference<String> returnString = new AtomicReference<>("");
        stream.forEach(product ->
                returnString.set(returnString.get() + "\n" +
                        product.getName() + " Quantity: " + productsMap.get(product)));
        return returnString.get();
    }
}
