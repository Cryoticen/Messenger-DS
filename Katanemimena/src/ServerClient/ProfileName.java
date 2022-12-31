package ServerClient;

import Assets.FileReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ProfileName {
    String projectDir = Paths.get("").toAbsolutePath().toString();
    private final String PATH  = projectDir + "\\src";


    FileReader profileReader;
    String profileName;

    public ProfileName(){
    }

    public String getUserName(){
        return profileName;
    }

    public void setUserName(){
        Scanner input = new Scanner(System.in);
        System.out.println("Give Username: ");
        this.profileName = input.nextLine();
        profileReader = new FileReader(PATH + "\\Users\\" + this.profileName + "\\history.txt");
    }
}