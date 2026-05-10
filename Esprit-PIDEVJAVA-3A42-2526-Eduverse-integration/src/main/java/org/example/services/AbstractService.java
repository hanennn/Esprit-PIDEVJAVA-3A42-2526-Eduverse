package org.example.services;

import java.sql.Connection;
import org.example.utils.DataBase;

public abstract class AbstractService<T> implements IService<T> {
    protected Connection connection;

    public AbstractService() {
        this.connection = DataBase.getInstance().getConnection();
    }
}
