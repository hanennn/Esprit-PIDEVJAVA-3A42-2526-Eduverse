import entities.Admin;
import entities.Student;
import entities.Professor;


import Services.AdminService;
import Services.StudentService;
import Services.ProfessorService;
import utils.DataBase;


import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


class main {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        AdminService adminService = new AdminService();
        StudentService studentService = new StudentService();
        ProfessorService professorService = new ProfessorService();

        boolean ok = true;
        while (ok) {
            System.out.println("1 Admin");
            System.out.println("2 Student");
            System.out.println("3 Professor");
            System.out.println("Quitte");
            int mainChoice = scanner.nextInt();

            switch (mainChoice) {

                case 1 -> {
                    boolean MenuAdmin = true;
                    while (MenuAdmin) {
                        System.out.println("1 Ajouter Admin");
                        System.out.println("2 Afficher Admins");
                        System.out.println("3 Modifier Admin");
                        System.out.println("4 Supprimer Admin");
                        System.out.println("Quitter");
                        int choix = scanner.nextInt();
                        scanner.nextLine();

                        switch (choix) {
                            case 1 -> {
                                System.out.print("FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("LastName: ");
                                String lastName = scanner.nextLine();
                                System.out.print("UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
                                Admin admin = new Admin(firstName, lastName, userName, email, password, true);
                                adminService.AjouterAdmin(admin);
                            }
                            case 2 -> {
                                List<Admin> admins = adminService.AfficherAdmin();
                                for (Admin a : admins) System.out.println(a);
                            }
                            case 3 -> {
                                System.out.print("ID admin modifié");
                                int id = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("New UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("New Email: ");
                                String email = scanner.nextLine();
                                System.out.print("New Password: ");
                                String password = scanner.nextLine();
                                System.out.print("New FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("New LastName: ");
                                String lastName = scanner.nextLine();
                                Admin admin = new Admin(id, firstName, lastName, userName, email, password, true, null);
                                adminService.ModifierAdmin(admin);
                            }
                            case 4 -> {
                                System.out.print("Enter Admin ID to delete: ");
                                int id = scanner.nextInt();
                                adminService.SupprimerAdmin(id);
                            }
                            case 0 -> MenuAdmin = false;
                            default -> System.out.println("Invalid choice!");
                        }
                    }
                }

                case 2 -> {
                    boolean MenuStudent = true;
                    while (MenuStudent) {
                        System.out.println("1 Ajouter Student");
                        System.out.println("2 Afficher Students");
                        System.out.println("3 Modifier Student");
                        System.out.println("4 Supprimer Student");
                        System.out.println("Quitter");

                        int choix = scanner.nextInt();
                        scanner.nextLine();

                        switch (choix) {
                            case 1 -> {
                                System.out.print("FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("LastName: ");
                                String lastName = scanner.nextLine();
                                System.out.print("UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
                                Student student = new Student(firstName, lastName, userName, email, password, true, null, null);
                                studentService.AjouterStudent(student);
                            }
                            case 2 -> {
                                List<Student> students = studentService.AfficherStudents();
                                for (Student s : students) System.out.println(s);
                            }
                            case 3 -> {
                                System.out.print("ID etudiant Modifié ");
                                int id = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("New UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("New Email: ");
                                String email = scanner.nextLine();
                                System.out.print("New Password: ");
                                String password = scanner.nextLine();
                                System.out.print("New FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("New LastName: ");
                                String lastName = scanner.nextLine();
                                Student student = new Student(id, firstName, lastName, userName, email, password, true, null, null, null);
                                studentService.ModifierStudent(student);
                            }
                            case 4 -> {
                                System.out.print("Enter Student ID to delete: ");
                                int id = scanner.nextInt();
                                studentService.SupprimerStudent(id);
                            }
                            case 0 -> MenuStudent = false;
                            default -> System.out.println("Invalid choice!");
                        }
                    }
                }

                case 3 -> {
                    boolean MenuProfessor = true;
                    while (MenuProfessor) {
                        System.out.println("1. Ajouter Professor");
                        System.out.println("2. Afficher Professors");
                        System.out.println("3. Modifier Professor");
                        System.out.println("4. Supprimer Professor");
                        System.out.println("0. Back");
                        System.out.print("Choose: ");
                        int choix = scanner.nextInt();
                        scanner.nextLine();

                        switch (choix) {
                            case 1 -> {
                                System.out.print("FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("LastName: ");
                                String lastName = scanner.nextLine();
                                System.out.print("UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
                                System.out.print("Specialty: ");
                                String specialty = scanner.nextLine();
                                System.out.print("Experience: ");
                                String experience = scanner.nextLine();
                                Professor professor = new Professor(firstName, lastName, userName, email, password, true, null, specialty, experience, null);
                                professorService.AjouterProfessor(professor);
                            }
                            case 2 -> {
                                List<Professor> professors = professorService.AfficherProfessors();
                                for (Professor p : professors) System.out.println(p);
                            }
                            case 3 -> {
                                System.out.print("ID Professor modifié");
                                int id = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("New UserName: ");
                                String userName = scanner.nextLine();
                                System.out.print("New Email: ");
                                String email = scanner.nextLine();
                                System.out.print("New Password: ");
                                String password = scanner.nextLine();
                                System.out.print("New FirstName: ");
                                String firstName = scanner.nextLine();
                                System.out.print("New LastName: ");
                                String lastName = scanner.nextLine();
                                System.out.print("New Specialty: ");
                                String specialty = scanner.nextLine();
                                System.out.print("New Experience: ");
                                String experience = scanner.nextLine();
                                Professor professor = new Professor(id, firstName, lastName, userName, email, password, true, null, null, specialty, experience, null);
                                professorService.ModifierProfessor(professor);
                            }
                            case 4 -> {
                                System.out.print("Enter Professor ID to delete: ");
                                int id = scanner.nextInt();
                                professorService.SupprimerProfessor(id);
                            }
                            case 0 -> MenuProfessor = false;
                            default -> System.out.println("Invalid choice!");
                        }
                    }
                }

                case 0 -> {
                    System.out.println("Goodbye!");
                    ok = false;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}