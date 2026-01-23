package ci553.happyshop.client.orderTracker;

import ci553.happyshop.orderManagement.OrderState;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;;

class OrderTrackerTest {

    @Test
    void setOrderMapDisplaysOrders() {
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(1, OrderState.Ordered);
        orderMap.put(2, OrderState.Progressing);

        String text = OrderTracker.buildOrderMapText(orderMap);
        assertTrue(text.contains("1"));
        assertTrue(text.contains("Ordered"));
        assertTrue(text.contains("2"));
        assertTrue(text.contains("Progressing"));
    }

    @Test
    void buildOrderMapTextShowsCollectedState() {
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(7, OrderState.Collected);

        String text = OrderTracker.buildOrderMapText(orderMap);

        assertTrue(text.contains("7"));
        assertTrue(text.contains("Collected"));
    }

    @Test
    void buildOrderMapTextHandlesEmptyMap() {
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();

        String text = OrderTracker.buildOrderMapText(orderMap);

        assertEquals("", text);
    }

    @Test
    void buildOrderMapTextKeepsSortedOrder() {
        TreeMap<Integer, OrderState> orderMap = new TreeMap<>();
        orderMap.put(5, OrderState.Ordered);
        orderMap.put(1, OrderState.Progressing);
        orderMap.put(3, OrderState.Collected);

        String text = OrderTracker.buildOrderMapText(orderMap);

        int firstIndex = text.indexOf("1");
        int secondIndex = text.indexOf("3");
        int thirdIndex = text.indexOf("5");
        assertTrue(firstIndex < secondIndex);
        assertTrue(secondIndex < thirdIndex);
    }
}
