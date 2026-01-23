package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.utility.WindowBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerModelTest {

    @Test
    void checkOutWithInsufficientStockShowsNotifierAndRemovesItems() throws IOException, SQLException {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();
        TestRemoveProductNotifier notifier = new TestRemoveProductNotifier();
        ArrayList<Product> insufficient = new ArrayList<>();
        Product insufficientProduct = new Product("0001", "TV", "0001.jpg", 10.0, 1);
        insufficientProduct.setOrderedQuantity(2);
        insufficient.add(insufficientProduct);

        model.cusView = view;
        model.removeProductNotifier = notifier;
        model.databaseRW = new StubDatabaseRW(insufficient);
        model.getTrolley().add(new Product("0001", "TV", "0001.jpg", 10.0, 1));
        model.getTrolley().add(new Product("0001", "TV", "0001.jpg", 10.0, 1));

        model.checkOut();

        assertEquals(1, notifier.showCount);
        assertTrue(notifier.lastMessage.contains("0001"));
        assertEquals(0, model.getTrolley().size());
    }

    @Test
    void cancelClosesNotifierWindow() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();
        TestRemoveProductNotifier notifier = new TestRemoveProductNotifier();

        model.cusView = view;
        model.removeProductNotifier = notifier;

        model.cancel();

        assertEquals(1, notifier.closeCount);
    }

    @Test
    void checkOutWithInsufficientStockFallsBackToSearchMessageWhenNotifierMissing() throws IOException, SQLException {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();
        ArrayList<Product> insufficient = new ArrayList<>();
        Product insufficientProduct = new Product("0002", "Radio", "0002.jpg", 5.0, 1);
        insufficientProduct.setOrderedQuantity(2);
        insufficient.add(insufficientProduct);

        model.cusView = view;
        model.databaseRW = new StubDatabaseRW(insufficient);
        model.getTrolley().add(new Product("0002", "Radio", "0002.jpg", 5.0, 1));
        model.getTrolley().add(new Product("0002", "Radio", "0002.jpg", 5.0, 1));

        model.checkOut();

        assertTrue(view.lastSearchResult.contains("Checkout failed"));
        assertTrue(view.lastSearchResult.contains("0002"));
    }

    @Test
    void addToTrolleyUsesEnteredQuantity() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();

        model.cusView = view;
        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "3";

        model.addToTrolley();

        assertEquals(1, model.getTrolley().size());
        assertEquals(3, model.getTrolley().get(0).getOrderedQuantity());
    }

    @Test
    void addToTrolleyShowsMessageForInvalidQuantity() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();

        model.cusView = view;
        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "abc";

        model.addToTrolley();

        assertTrue(view.lastSearchResult.contains("valid quantity"));
        assertEquals(0, model.getTrolley().size());
    }

    @Test
    void addToTrolleyShowsMessageForZeroQuantity() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();

        model.cusView = view;
        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "0";

        model.addToTrolley();

        assertTrue(view.lastSearchResult.contains("at least 1"));
        assertEquals(0, model.getTrolley().size());
    }

    @Test
    void addToTrolleyMergesDuplicateProductsAcrossAdds() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();

        model.cusView = view;

        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "2";
        model.addToTrolley();

        model.setCurrentProduct(new Product("0002", "Radio", "0002.jpg", 5.0, 5));
        view.quantityText = "1";
        model.addToTrolley();

        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "3";
        model.addToTrolley();

        int tvQuantity = 0;
        for (Product product : model.getTrolley()) {
            if (product.getProductId().equals("0001")) {
                tvQuantity = product.getOrderedQuantity();
                break;
            }
        }

        assertEquals(2, model.getTrolley().size());
        assertEquals(5, tvQuantity);
    }

    @Test
    void addToTrolleyDoesNotMergeDifferentProducts() {
        CustomerModel model = new CustomerModel();
        TestCustomerView view = new TestCustomerView();

        model.cusView = view;

        model.setCurrentProduct(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        view.quantityText = "1";
        model.addToTrolley();

        model.setCurrentProduct(new Product("0002", "Radio", "0002.jpg", 5.0, 5));
        view.quantityText = "1";
        model.addToTrolley();

        assertEquals(2, model.getTrolley().size());
    }

    static class TestCustomerView extends CustomerView {
        String lastSearchResult = "";
        String lastTrolley = "";
        String lastReceipt = "";
        String quantityText = "1";

        @Override
        public void update(String imageName, String searchResult, String trolley, String receipt) {
            lastSearchResult = searchResult;
            lastTrolley = trolley;
            lastReceipt = receipt;
        }

        @Override
        public String getQuantityText() {
            return quantityText;
        }

        @Override
        WindowBounds getWindowBounds() {
            return new WindowBounds(0, 0, 0, 0);
        }
    }

    static class TestRemoveProductNotifier extends RemoveProductNotifier {
        String lastMessage = "";
        int showCount = 0;
        int closeCount = 0;

        @Override
        public void showRemovalMsg(String removalMsg) {
            lastMessage = removalMsg;
            showCount++;
        }

        @Override
        public void closeNotifierWindow() {
            closeCount++;
        }
    }

    static class StubDatabaseRW implements DatabaseRW {
        private final ArrayList<Product> insufficientProducts;

        StubDatabaseRW(ArrayList<Product> insufficientProducts) {
            this.insufficientProducts = insufficientProducts;
        }

        @Override
        public ArrayList<Product> searchProduct(String keyword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product searchByProductId(String productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayList<Product> purchaseStocks(ArrayList<Product> proList) {
            return insufficientProducts;
        }

        @Override
        public void updateProduct(String id, String des, double price, String imageName, int stock) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteProduct(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertNewProduct(String id, String des, double price, String image, int stock) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isProIdAvailable(String productId) {
            throw new UnsupportedOperationException();
        }
    }
}
