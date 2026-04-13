package services;

import utils.DataBase;
import java.sql.Connection;

public abstract class AbstractService<T> implements IService<T> {
    protected Connection connection;

    public AbstractService() {
        this.connection = DataBase.getInstance().getConnection();
    }
}
