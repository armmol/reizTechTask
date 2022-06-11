package armaan.task.reizTech.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.NameAlreadyBoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Product Storage
 * Test Methods correspond to function names and are self explanatory
 */
class ProductStorageTest {

    private ProductStorage productStorage;
    private final Map<Product, Integer> productStorageMap = new HashMap<>();
    private Product product;

    @BeforeEach
    void setup() {
        productStorage = new ProductStorage(productStorageMap);
        product = new Product("TEST", 2.1);
    }

    @Test
    void getProductsMap() {
        assertEquals(productStorage.getProductsMap(), productStorageMap);
    }

    @Test
    void addProductType() throws NameAlreadyBoundException {
        //Valid addition of new Product
        productStorage.addProductType(product, 10);
        assertEquals(productStorageMap.get(product), 10);
        //Invalid addition of new Product because product is already present in Product Inventory
        assertThrows(NameAlreadyBoundException.class, () -> productStorage.addProductType(product, 10));
    }

    @Test
    void removeProduct() throws NameAlreadyBoundException {
        //Valid removal of  Product
        productStorage.addProductType(product, 10);
        productStorage.removeProduct(product.getName());
        assertNull(productStorageMap.get(product));
        //Invalid removal of  Product because product is not present in Product Inventory
        assertThrows(NullPointerException.class, () -> productStorage.removeProduct(product.getName()));
    }

    @Test
    void updateProductAfterSale() throws NameAlreadyBoundException {
        productStorage.addProductType(product, 10);
        productStorage.updateProductAfterSale(product.getName());
        assertEquals(9, productStorageMap.get(product));
    }

    @Test
    void increaseProductQuantity() throws NameAlreadyBoundException {
        //Increase Amount is valid for denomination present in Cash Inventory
        productStorage.addProductType(product, 10);
        productStorage.increaseProductQuantity(product.getName(), 10);
        assertEquals(productStorageMap.get(product), 20);
        //Invalid increase of  Product because product is not present in Product Inventory
        assertThrows(NullPointerException.class, () -> productStorage.increaseProductQuantity("wrong input", 0));

    }

    @Test
    void decreaseProductQuantity() throws NameAlreadyBoundException {
        //Decrease Amount is valid for denomination present in Cash Inventory
        productStorage.addProductType(product, 10);
        productStorage.decreaseProductQuantity(product.getName(), 2);
        assertEquals(productStorageMap.get(product), 8);
        //Invalid decrease of  Product because product is not present in Product Inventory
        assertThrows(NullPointerException.class, () -> productStorage.increaseProductQuantity("wrong input", 0));
    }
}