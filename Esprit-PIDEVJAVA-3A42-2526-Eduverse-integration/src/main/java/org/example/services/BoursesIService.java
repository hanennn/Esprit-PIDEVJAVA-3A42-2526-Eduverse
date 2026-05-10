package org.example.services;

import java.util.List;

public interface BoursesIService<T> {
    void add(T t);
    void update(T t);
    void delete(int id);
    T getById(int id);
    List<T> getAll();
}
