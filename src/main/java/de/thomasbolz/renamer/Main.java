/*
 * Copyright 2014 Thomas Bolz
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.thomasbolz.renamer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/de/thomasbolz/renamer/renamer.fxml"));
        primaryStage.setTitle("RenamerFX");
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.getIcons().add(new Image("icon-16.png"));
        primaryStage.getIcons().add(new Image("icon-32.png"));
        primaryStage.getIcons().add(new Image("icon-64.png"));
        primaryStage.getIcons().add(new Image("icon-128.png"));
        primaryStage.getIcons().add(new Image("icon-256.png"));
        primaryStage.getIcons().add(new Image("icon-512.png"));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
