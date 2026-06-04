package com.itss.importorder.controller;

import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.repository.DataStore;
import java.util.Optional;

public class XacThucController {
    private final DataStore store;

    public XacThucController(DataStore store) {
        this.store = store;
    }

    public Optional<NguoiDung> login(String username, String password) {
        return store.getNguoiDungs().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                .filter(u -> u.matchesPassword(password))
                .findFirst();
    }
}
