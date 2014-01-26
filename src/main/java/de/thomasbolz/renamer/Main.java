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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class. Loads the main gui and starts the FX application.
 */
public class Main extends Application {

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/de/thomasbolz/renamer/renamer.fxml"));
        final Properties properties = readPropertiesFromClasspath("renamer.properties");
        primaryStage.setTitle(properties.getProperty("app.name", "app.name") + " v" + properties.getProperty("app.version", "app.version"));
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.getIcons().add(new Image("icon-16.png"));
        primaryStage.getIcons().add(new Image("icon-32.png"));
        primaryStage.getIcons().add(new Image("icon-64.png"));
        primaryStage.getIcons().add(new Image("icon-128.png"));
        primaryStage.getIcons().add(new Image("icon-256.png"));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Reads properties from a property file that is on the classpath.
     *
     * @param filename
     * @return
     */
    private Properties readPropertiesFromClasspath(String filename) {
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);


        try {
            if (inputStream != null) {
                props.load(inputStream);
            } else {
                log.info("did not find file " + filename);
            }
        } catch (IOException e) {
            log.error("IOException while reading properties from file " + filename, e);
        }

        return props;
    }

}
