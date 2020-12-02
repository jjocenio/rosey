package jjocenio.rosey.service;

import jjocenio.rosey.persistence.Row;

import java.util.Map;

public interface DataListener {

    void dataStatusChanged(Map<Row.Status, Long> count);
}
