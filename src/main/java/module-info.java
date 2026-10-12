module ni.edu.uam.practicahilos.practicahilosjavafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.practicahilos.practicahilosjavafx to javafx.fxml;
    exports ni.edu.uam.practicahilos.practicahilosjavafx;
}