package com.example.socialmediaapp.Notification;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AuthToken {

    private static final String messageScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAuthToken() {
        try {
            String json = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"socialmediaapp-a4ca5\",\n" +
                    "  \"private_key_id\": \"286ad73853224672453ebea69ada2ae991c69a4e\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCiWjbkgQpNKJ1g\\n5v6fd649MFwPP44IJG0lcXQ+kz7EFXBmWhj8KjALTjnBJGv+qSV4GlZVal7n0txZ\\nKTd/4maEgCYaYb7zmszabPsvL7Cip8hZuLy4y1b0zuLmHTM4171okCQvtA/uX6Q/\\nNzYUDxUanY3zvNKQTbPUKRNKnw+PJ+KzQ2IBL15373EG4WhtfeVcU31pYF9dMKBi\\nUMK0wkAlrFDxzJrFbnLA7qFzzUcdDDV59vdO1z+SbxLQhfYNt40uTwRrvXtDl13o\\ncG+o2pEp+4c74E9K9JU3kM5qOROiQ0a0Ja5CotpbVj3j8aebh0wJmqp++dLbSj9m\\nSzKJi0IPAgMBAAECggEAMNyN4L+lZgtvMhbTUxMl8Wx4iG5MRM8Ruk8m+R4/qav6\\nJBPBqULZKs3kO+jGR+KIUFk82oAlCAhCIONyvJ5a1E3tKct5OL/CH4rlHhJwSsPM\\nXJRo2AT6qrsJNtBa9iSX8LxQ57p9gPRv7kajk+3mIBiC/HQ7uwpnsNab471zZVXU\\nR9H+24ev7l43yKG587AhF2WU+XRy1fwe2rRByjEzHFgfAdKTM1KCAGyNR/acErQQ\\nhhUf24CzDe/d4QNZkBmuJvC2m3rMwOQvZZ3FWnrde8I05e0hZlHGkpiPzMX5Kc/d\\nqIiuyzO8jV+s/OmbI//MBXwWerEn0TT/6eR7ljS/gQKBgQDjTHxej4ph61VBqTXw\\n18RAD5g5CDVnEdB+LziCKW1zR0FmCzgH3Bn9ouuU+5aYBX/KEBXMKOHpjg1EZtTx\\nE3Okn8Yjgk9XgwrHEZbLZvCwpL/MZuUD80LxR0CDgxFLDwZ0M8qF3GUxF60zRiA3\\nM4cJqd5y0zQeAiVpX7cFyfjevwKBgQC22lBZgGha6BpkBivFY6aVDlhciICGQRwP\\na8yNMWzSF6O4+9h3oXSTmyY51n7LoQV/r1/RyrviGvrOdA8PAS4TBDEr7biVtncX\\nkZQWQ0K04GcSTzfxA9wLZ0P1EVcod7VhDwtEYkMq4qAiEzguWQLNi5prmm/bIcOv\\nkerbJxbAsQKBgBiPBg0Ng00rKS4MA1j/ZyPoy3C2E1cMlLNdlMRzh3DiLQ3Cfo5S\\n2LgP51ZUjKPmTDVf4YsKi8Y5Y/OVDMLzcBZEcRsbFEmHTh3OJ7XYbH8ZfeOu5mWz\\n3fLF2RGckrzCZdceVW57Lq/MOBmHJSnxtlBz+yX67fGkF7i378V/WhVvAoGBAIVe\\nPMbbMsJDkqQsIBbr9R+5A9gSM/Q4XzgcP2koiJ/AYElXX+7502ap/jqnaXoqqzET\\ngEcroNhtJ0wYOLeP+8QhBaBt1nlUSg7lvuZyDqCRIHypTTs8vm2O13FTQYghsCjn\\n6SbqqOUJwRS5gfvELpgvX+VmEnn5aZqQSoWoQB8xAoGAH0SEqT6Oe21PfREz5r/G\\n0Z4ovK4I/FeM/1nDpDlp1/dMGrYaHcRuBEHeM++Kn8VyKKpqqZ+g2zhzQHHRZN5I\\nmOt/6VWn6ALkMobt3J4lQgLQlNWazKCrXio1CaZgeNnt0MXWiq4rDYrqMU+ccLK+\\nJcRXh8BwO9hMPbDwzL3Rtak=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-iyhix@socialmediaapp-a4ca5.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"114816524922409043264\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-iyhix%40socialmediaapp-a4ca5.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";
            InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(Lists.newArrayList(messageScope));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            return null;
        }
    }
}
