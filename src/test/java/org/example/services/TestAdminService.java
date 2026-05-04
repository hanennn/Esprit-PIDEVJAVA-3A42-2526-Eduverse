package org.example.services;

import org.example.services.AdminService;
import org.example.entities.Admin;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestAdminService {

    static AdminService adminService;
    @BeforeAll
    static void setup(){
        adminService  = new AdminService();
    }


    @Test
    @Order(1)
    void TestAjoutAdmin() throws SQLException, ClassNotFoundException {
        Admin admin = new Admin(
                "AdminTest",
                "TestAdmin",
                "Admin1",
                "Admin@gmail.com",
                "0000",
                true
        );
        adminService.AjouterAdmin(admin);
        List <Admin> ListAdmins = adminService.AfficherAdmin();
        assertFalse(ListAdmins.isEmpty());
        assertTrue(
                ListAdmins.stream().anyMatch(a -> a.getUserName().equals("AdminTest"))
        );
    }

    @Test
    @Order(2)
    void  TestModifierAdmin()throws SQLException {

        Admin admin = adminService.FindAdminByUsername("Admin1");
         assertNotNull(admin);

        admin.setFirstName("UpdatedTestAdmin");
        admin.setEmail("UpdatedEmail@gmail.com");
        adminService.ModifierAdmin(admin);

        Admin updatedAdmin = adminService.FindAdminByUsername("Admin1");
        assertNotNull(updatedAdmin);
        assertEquals("UpdatedEmail@gmail.com", updatedAdmin.getEmail());
        assertEquals("UpdatedTestAdmin", updatedAdmin.getFirstName());
    }

    @Test
    @Order(3)
    void TestSupprimerAdmin() throws SQLException {
        Admin admin = adminService.FindAdminByUsername("Admin1");
        assertNotNull(admin);

        adminService.SupprimerAdmin(admin.getId());
        Admin updatedAdmin = adminService.FindAdminByUsername("Admin1");
        assertNull(updatedAdmin);
    }



}
