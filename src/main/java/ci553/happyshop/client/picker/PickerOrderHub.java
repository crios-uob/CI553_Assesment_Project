package ci553.happyshop.client.picker;

import ci553.happyshop.orderManagement.OrderState;

import java.io.IOException;

public interface PickerOrderHub {
    void changeOrderStateMoveFile(int orderId, OrderState newState) throws IOException;
    String getOrderDetailForPicker(int orderId) throws IOException;
    void registerPickerModel(PickerModel pickerModel);
}
