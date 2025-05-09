package com.productora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializar la base de datos
        String dbPath = DatabaseInitializer.initializeDatabase();
        
        // Conectar a la base de datos
        AppData.getInstance().connect(dbPath);

        // Cargar la vista principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        // Configurar la escena - Tamaño más compacto
        Scene scene = new Scene(root, 800, 600);

        // Configurar y mostrar la ventana
        primaryStage.setTitle("Gestión de Series");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Cerrar la conexión a la base de datos al salir
        AppData.getInstance().close();
    }

    /**
     * Método principal que inicia la aplicación
     */
    public static void main(String[] args) {
        launch(args);
    }
}