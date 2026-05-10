package org.example.services;

import org.example.utils.BoursesDataBase;
import java.sql.Connection;

public abstract class BoursesAbstractService<T> implements BoursesIService<T> {
    protected Connection connection;

    public BoursesAbstractService() {
        this.connection = BoursesDataBase.getInstance().getConnection();
    }
}
