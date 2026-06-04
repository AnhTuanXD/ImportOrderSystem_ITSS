package com.itss.importorder.service;

import com.itss.importorder.model.User;
import com.itss.importorder.repository.DataStore;
import java.util.Optional;

public class AuthService {
    private final DataStore store;

    public AuthService(DataStore store) {
        this.store = store;
    }

    public Optional<User> login(String username, String password) {
        return store.getUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username.trim()))
                .filter(user -> user.matchesPassword(password))
                .findFirst();
    }
}

