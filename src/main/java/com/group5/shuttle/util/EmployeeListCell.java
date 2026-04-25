package com.group5.shuttle.util;

import com.group5.shuttle.model.Employee;
import javafx.scene.control.ListCell;

public final class EmployeeListCell extends ListCell<Employee> {

    @Override
    protected void updateItem(Employee emp, boolean empty) {
        super.updateItem(emp, empty);
        setText(empty || emp == null ? null : EmployeeStringConverter.INSTANCE.toString(emp));
    }
}
