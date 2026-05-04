package com.clinic.patientappointmentfrontend;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClinicApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setPrimaryStage(stage);

        stage.setTitle(AppConfig.CLINIC_NAME + " - " + AppConfig.APP_NAME);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        SceneManager.loadLoginScene();
    }
}