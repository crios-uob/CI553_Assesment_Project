package ci553.happyshop.client.warehouse;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.utility.WindowBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseModelTest {

    @Test
    void doSearchUpdatesProductList() throws SQLException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        StubDatabaseRW databaseRW = new StubDatabaseRW();
        ArrayList<Product> results = new ArrayList<>();
        results.add(new Product("0001", "TV", "0001.jpg", 10.0, 5));
        results.add(new Product("0002", "Radio", "0002.jpg", 5.0, 5));
        databaseRW.searchResults = results;

        model.view = view;
        model.databaseRW = databaseRW;
        view.searchKeywordText = "TV";

        model.doSearch();

        assertEquals(2, view.lastProductList.size());
    }

    @Test
    void doSearchWithEmptyKeywordClearsList() throws SQLException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        StubDatabaseRW databaseRW = new StubDatabaseRW();

        model.view = view;
        model.databaseRW = databaseRW;
        view.searchKeywordText = "";

        model.doSearch();

        assertEquals(0, view.lastProductList.size());
    }

    @Test
    void doChangeStockByAddsQuantity() throws SQLException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        TestAlertSimulator alertSimulator = new TestAlertSimulator();

        model.view = view;
        model.alertSimulator = alertSimulator;
        view.editStockText = "10";
        view.changeByText = "3";

        model.doChangeStockBy("add");

        assertEquals("13", view.lastStockEdit);
        assertEquals(0, alertSimulator.showCount);
    }

    @Test
    void doChangeStockByShowsErrorForInvalidValue() throws SQLException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        TestAlertSimulator alertSimulator = new TestAlertSimulator();

        model.view = view;
        model.alertSimulator = alertSimulator;
        view.editStockText = "10";
        view.changeByText = "abc";

        model.doChangeStockBy("add");

        assertEquals(1, alertSimulator.showCount);
    }

    @Test
    void doSummitNewShowsErrorWhenInputInvalid() throws SQLException, IOException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        StubDatabaseRW databaseRW = new StubDatabaseRW();
        TestAlertSimulator alertSimulator = new TestAlertSimulator();

        model.view = view;
        model.databaseRW = databaseRW;
        model.alertSimulator = alertSimulator;
        view.formMode = "NEW";
        view.newIdText = "12";
        view.newPriceText = "10.00";
        view.newStockText = "5";
        view.newDescriptionText = "New Item";
        view.newImageUri = "image.jpg";
        databaseRW.isProIdAvailable = true;

        model.doSummit();

        assertEquals(1, alertSimulator.showCount);
        assertEquals(0, databaseRW.insertCount);
    }

    @Test
    void doSummitEditShowsErrorWhenPriceInvalid() throws SQLException, IOException {
        WarehouseModel model = new WarehouseModel();
        TestWarehouseView view = new TestWarehouseView();
        StubDatabaseRW databaseRW = new StubDatabaseRW();
        TestAlertSimulator alertSimulator = new TestAlertSimulator();

        model.view = view;
        model.databaseRW = databaseRW;
        model.alertSimulator = alertSimulator;
        view.formMode = "EDIT";
        view.selectedProduct = new Product("0001", "TV", "0001.jpg", 10.0, 5);
        view.editPriceText = "abc";
        view.editStockText = "10";
        view.editDescriptionText = "Updated";
        view.changeByText = "";
        view.userSelectedImageEdit = false;

        model.doEdit();
        model.doSummit();

        assertEquals(1, alertSimulator.showCount);
        assertEquals(0, databaseRW.updateCount);
    }

    static class TestWarehouseView extends WarehouseView {
        String searchKeywordText = "";
        String formMode = "EDIT";
        Product selectedProduct;
        String editPriceText = "";
        String editStockText = "";
        String editDescriptionText = "";
        boolean userSelectedImageEdit = false;
        String userSelectedImageUriEdit = "";
        String changeByText = "";
        String newIdText = "";
        String newPriceText = "";
        String newStockText = "";
        String newDescriptionText = "";
        String newImageUri = null;

        ArrayList<Product> lastProductList = new ArrayList<>();
        String lastStockEdit = "";

        @Override
        public String getSearchKeywordText() {
            return searchKeywordText;
        }

        @Override
        public Product getSelectedProduct() {
            return selectedProduct;
        }

        @Override
        public String getFormMode() {
            return formMode;
        }

        @Override
        public String getEditPriceText() {
            return editPriceText;
        }

        @Override
        public String getEditStockText() {
            return editStockText;
        }

        @Override
        public String getEditDescriptionText() {
            return editDescriptionText;
        }

        @Override
        public boolean isUserSelectedImageEdit() {
            return userSelectedImageEdit;
        }

        @Override
        public String getUserSelectedImageUriEdit() {
            return userSelectedImageUriEdit;
        }

        @Override
        public String getChangeByText() {
            return changeByText;
        }

        @Override
        public String getNewIdText() {
            return newIdText;
        }

        @Override
        public String getNewPriceText() {
            return newPriceText;
        }

        @Override
        public String getNewStockText() {
            return newStockText;
        }

        @Override
        public String getNewDescriptionText() {
            return newDescriptionText;
        }

        @Override
        public String getNewImageUri() {
            return newImageUri;
        }

        @Override
        void updateObservableProductList(ArrayList<Product> productList) {
            lastProductList = productList;
        }

        @Override
        void updateBtnAddSub(String stock) {
            lastStockEdit = stock;
        }

        @Override
        void updateEditProductChild(String id, String price, String stock, String des, String imageUrl) {
        }

        @Override
        void resetEditChild() {
        }

        @Override
        void resetNewProChild() {
        }

        @Override
        WindowBounds getWindowBounds() {
            return new WindowBounds(0, 0, 0, 0);
        }
    }

    static class TestAlertSimulator extends AlertSimulator {
        int showCount = 0;

        @Override
        public void showErrorMsg(String errorMsg) {
            showCount++;
        }

        @Override
        public void closeAlertSimulatorWindow() {
        }
    }

    static class StubDatabaseRW implements DatabaseRW {
        ArrayList<Product> searchResults = new ArrayList<>();
        boolean isProIdAvailable = true;
        int updateCount = 0;
        int insertCount = 0;

        @Override
        public ArrayList<Product> searchProduct(String keyword) {
            return searchResults;
        }

        @Override
        public Product searchByProductId(String productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArrayList<Product> purchaseStocks(ArrayList<Product> proList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateProduct(String id, String des, double price, String imageName, int stock) {
            updateCount++;
        }

        @Override
        public void deleteProduct(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertNewProduct(String id, String des, double price, String image, int stock) {
            insertCount++;
        }

        @Override
        public boolean isProIdAvailable(String productId) {
            return isProIdAvailable;
        }
    }
}
