import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class LibraryManagementSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    static class Book implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String title;
        String author;
        boolean isAvailable;
        String borrower;
        LocalDate dueDate;

        Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isAvailable = true;
            this.borrower = "";
            this.dueDate = null;
        }

        @Override
        public String toString() {
            String info = String.format("ID: %d | Title: %s | Author: %s | Available: %s",
                    id, title, author, isAvailable ? "Yes" : "No");
            if (!isAvailable) {
                info += String.format(" | Borrower: %s | Due: %s", borrower, dueDate.format(DateTimeFormatter.ISO_DATE));
            }
            return info;
        }
    }

    private List<Book> books;
    private transient Scanner scanner;
    private static final String DATA_FILE = "library.dat";
    private static final int LOAN_DAYS = 7;
    private static final int FINE_PER_DAY = 10;

    public LibraryManagementSystem() {
        scanner = new Scanner(System.in);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) {
            books = new ArrayList<>();
            books.add(new Book(1, "Introduction to Algorithms", "Cormen"));
            books.add(new Book(2, "Effective Java", "Joshua Bloch"));
            books.add(new Book(3, "Clean Code", "Robert C. Martin"));
            saveData();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                books = (List<Book>) obj;
            } else {
                books = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Failed to load data. Starting fresh. (" + e.getMessage() + ")");
            books = new ArrayList<>();
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(books);
        } catch (IOException e) {
            System.out.println("Failed to save data: " + e.getMessage());
        }
    }

    private int nextBookId() {
        int max = 0;
        for (Book b : books) if (b.id > max) max = b.id;
        return max + 1;
    }

    private void addBook() {
        System.out.println("Enter book title:");
        String title = scanner.nextLine().trim();
        System.out.println("Enter author name:");
        String author = scanner.nextLine().trim();
        int id = nextBookId();
        books.add(new Book(id, title, author));
        saveData();
        System.out.println("Book added with ID " + id);
    }

    private void listBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in library.");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private void searchBook() {
        System.out.println("Search by (1) ID, (2) Title, (3) Author?");
        String choice = scanner.nextLine().trim();
        boolean found = false;
        switch (choice) {
            case "1":
                System.out.println("Enter book ID:");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    for (Book b : books) if (b.id == id) { System.out.println(b); return; }
                    System.out.println("Book not found.");
                } catch (NumberFormatException e) { System.out.println("Invalid ID."); }
                break;
            case "2":
                System.out.println("Enter title (partial allowed):");
                String t = scanner.nextLine().trim().toLowerCase();
                for (Book b : books) if (b.title.toLowerCase().contains(t)) { System.out.println(b); found = true; }
                if (!found) System.out.println("No matching books.");
                break;
            case "3":
                System.out.println("Enter author (partial allowed):");
                String a = scanner.nextLine().trim().toLowerCase();
                for (Book b : books) if (b.author.toLowerCase().contains(a)) { System.out.println(b); found = true; }
                if (!found) System.out.println("No matching books.");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void issueBook() {
        System.out.println("Enter book ID to issue:");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            for (Book b : books) {
                if (b.id == id) {
                    if (!b.isAvailable) { System.out.println("Book already issued."); return; }
                    System.out.println("Enter borrower's name:");
                    String borrower = scanner.nextLine().trim();
                    b.isAvailable = false;
                    b.borrower = borrower;
                    b.dueDate = LocalDate.now().plusDays(LOAN_DAYS);
                    saveData();
                    System.out.println("Book issued to " + borrower + ". Due date: " + b.dueDate);
                    return;
                }
            }
            System.out.println("Book not found.");
        } catch (NumberFormatException e) { System.out.println("Invalid ID."); }
    }

    private void returnBook() {
        System.out.println("Enter book ID to return:");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            for (Book b : books) {
                if (b.id == id) {
                    if (b.isAvailable) { System.out.println("Book is not issued."); return; }
                    LocalDate today = LocalDate.now();
                    long lateDays = 0;
                    if (today.isAfter(b.dueDate)) {
                        lateDays = java.time.temporal.ChronoUnit.DAYS.between(b.dueDate, today);
                    }
                    b.isAvailable = true;
                    b.borrower = "";
                    b.dueDate = null;
                    saveData();
                    if (lateDays > 0) {
                        System.out.println("Book returned. Late by " + lateDays + " days. Fine: ₹" + (lateDays * FINE_PER_DAY));
                    } else {
                        System.out.println("Book returned on time. Thank you!");
                    }
                    return;
                }
            }
            System.out.println("Book not found.");
        } catch (NumberFormatException e) { System.out.println("Invalid ID."); }
    }

    private void deleteBook() {
        System.out.println("Enter book ID to delete:");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Iterator<Book> it = books.iterator();
            while (it.hasNext()) {
                Book b = it.next();
                if (b.id == id) {
                    it.remove();
                    saveData();
                    System.out.println("Book deleted.");
                    return;
                }
            }
            System.out.println("Book not found.");
        } catch (NumberFormatException e) { System.out.println("Invalid ID."); }
    }

    private void exportBooks() {
        System.out.println("Exporting book list to books_export.txt ...");
        try (PrintWriter pw = new PrintWriter(new FileWriter("books_export.txt"))) {
            for (Book b : books) pw.println(b);
            System.out.println("Export completed.");
        } catch (IOException e) {
            System.out.println("Failed to export: " + e.getMessage());
        }
    }

    private void showMenu() {
        System.out.println("\n--- Library Management System ---");
        System.out.println("1. Add Book");
        System.out.println("2. List Books");
        System.out.println("3. Search Book");
        System.out.println("4. Issue Book");
        System.out.println("5. Return Book");
        System.out.println("6. Delete Book");
        System.out.println("7. Export Book List");
        System.out.println("8. Exit");
        System.out.print("Choose an option: ");
    }

    public void run() {
        while (true) {
            showMenu();
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": addBook(); break;
                case "2": listBooks(); break;
                case "3": searchBook(); break;
                case "4": issueBook(); break;
                case "5": returnBook(); break;
                case "6": deleteBook(); break;
                case "7": exportBooks(); break;
                case "8": System.out.println("Exiting..."); saveData(); return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    public static void main(String[] args) {
        LibraryManagementSystem app = new LibraryManagementSystem();
        app.run();
    }
}
