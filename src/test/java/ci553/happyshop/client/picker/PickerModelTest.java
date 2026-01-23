package ci553.happyshop.client.picker;

import ci553.happyshop.orderManagement.OrderState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickerModelTest {

    @Test
    void setOrderMapUpdatesViewText() {
        PickerModel model = new PickerModel();
        TestPickerDisplay view = new TestPickerDisplay();
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(2, OrderState.Ordered);
        orderMap.put(5, OrderState.Progressing);

        model.pickerView = view;
        model.setOrderMap(orderMap);

        assertTrue(view.lastOrderMap.contains("2"));
        assertTrue(view.lastOrderMap.contains("Ordered"));
        assertTrue(view.lastOrderMap.contains("5"));
        assertTrue(view.lastOrderMap.contains("Progressing"));
    }

    @Test
    void doProgressingNotifiesOrderHubAndShowsDetail() throws IOException {
        PickerModel model = new PickerModel();
        TestPickerDisplay view = new TestPickerDisplay();
        TestPickerOrderHub orderHub = new TestPickerOrderHub();
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(1, OrderState.Ordered);
        orderMap.put(2, OrderState.Ordered);

        model.pickerView = view;
        model.setOrderHub(orderHub);
        model.resetLocksForTest();
        model.setOrderMap(orderMap);

        model.doProgressing();

        assertEquals(1, orderHub.lastOrderId);
        assertEquals(OrderState.Progressing, orderHub.lastState);
        assertEquals("detail-1", view.lastOrderDetail);
    }

    @Test
    void doCollectedNotifiesOrderHub() throws IOException {
        PickerModel model = new PickerModel();
        TestPickerDisplay view = new TestPickerDisplay();
        TestPickerOrderHub orderHub = new TestPickerOrderHub();
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(1, OrderState.Ordered);

        model.pickerView = view;
        model.setOrderHub(orderHub);
        model.resetLocksForTest();
        model.setOrderMap(orderMap);

        model.doProgressing();
        model.doCollected();

        assertEquals(1, orderHub.lastOrderId);
        assertEquals(OrderState.Collected, orderHub.lastState);
        assertEquals("", view.lastOrderDetail);
    }

    static class TestPickerDisplay implements PickerDisplay {
        String lastOrderMap = "";
        String lastOrderDetail = "";

        @Override
        public void update(String orderMap, String orderDetail) {
            lastOrderMap = orderMap;
            lastOrderDetail = orderDetail;
        }
    }

    static class TestPickerOrderHub implements PickerOrderHub {
        int lastOrderId = 0;
        OrderState lastState;

        @Override
        public void changeOrderStateMoveFile(int orderId, OrderState newState) {
            lastOrderId = orderId;
            lastState = newState;
        }

        @Override
        public String getOrderDetailForPicker(int orderId) {
            return "detail-" + orderId;
        }

        @Override
        public void registerPickerModel(PickerModel pickerModel) {
        }
    }
}
