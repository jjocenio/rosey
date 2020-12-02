package jjocenio.rosey.service;

import jjocenio.rosey.persistence.Row;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

public abstract class DataChanger {

    protected final ObjectProvider<DataListener> dataListenerObjectProvider;

    protected DataChanger(ObjectProvider<DataListener> dataListenerObjectProvider) {
        this.dataListenerObjectProvider = dataListenerObjectProvider;
    }

    protected void callDataListeners() {
        Map<Row.Status, Long> count = countGroupByStatus();
        dataListenerObjectProvider.forEach(dl -> {
            dl.dataStatusChanged(count);
        });
    }

    protected abstract Map<Row.Status, Long> countGroupByStatus();
}
