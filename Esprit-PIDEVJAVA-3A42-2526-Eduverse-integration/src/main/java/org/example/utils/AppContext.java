package org.example.utils;

import org.example.controllers.MainController;
import org.example.entities.User;

public final class AppContext {
    private static User currentUser;
    private static MainController mainController;

    private AppContext() {}

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User currentUser) { AppContext.currentUser = currentUser; }
    public static MainController getMainController() { return mainController; }
    public static void setMainController(MainController mainController) { AppContext.mainController = mainController; }
}