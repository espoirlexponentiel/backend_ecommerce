package com.ecommerce.backend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UploadTest {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public static void main(String[] args) {
        try {
            // 📂 Vérifier le chemin exact
            System.out.println("UPLOAD_DIR = " + UPLOAD_DIR);

            // 📂 Vérifier si le dossier existe
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                System.out.println("❌ Dossier n'existe pas !");
            } else {
                System.out.println("✅ Dossier trouvé !");
            }

            // 📂 Essayer d'écrire un fichier test
            File testFile = new File(UPLOAD_DIR, "test.txt");
            FileWriter writer = new FileWriter(testFile);
            writer.write("Hello Espoir, ceci est un test !");
            writer.close();

            System.out.println("✅ Fichier écrit avec succès : " + testFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Impossible d'écrire dans le dossier : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
