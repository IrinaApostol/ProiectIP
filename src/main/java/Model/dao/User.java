package Model.dao;

import Model.dao.storage.Database;
import com.mysql.cj.xdevapi.JsonArray;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.ArrayList;

import static com.sun.org.apache.bcel.internal.classfile.Utility.toHexString;


public class User {

    private static final int SALT_BYTES = 24;
    private static final int HASH_BYTES = 24;
    private static final int PBKDF2_ITERATIONS = 1000;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    public static boolean checkIfUserExists(String username) {
        boolean exist = false;
        String query = "select * from users where username=? ";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);) {
            statement.setString(1, username);
            statement.executeQuery();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                exist = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exist;
    }

    /**
     * Aceasta funtie adauga in baza de date utilizatorul inregistrat
     * @param username sub forma de string
     * @param password sub forma de string
     * @param email sub forma de string
     */

    public static void addUser(String username, String password, String email) {
        String query = "INSERT INTO users(username, password, email, salt, solved_problems_no, uploaded_problems_no, points_no) VALUES (?,?,?,?,0,0,0) ";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);) {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTES);

            String stringSalt = toHexString(salt);
            String passwordHash = toHexString(hash);

            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, email);
            statement.setString(4, stringSalt);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aceasta functie genereaza un hash pe baza parolei si a saltului (este apelata de addUser)
     * @param password parola sub forma de char[]
     * @param salt un sir de biti pentru criptare
     * @param iterations
     * @param bytes numarul de biti al hashului final
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    //Vine modificata mai tarziua
    /*
    public boolean verifyUserPassword(String username, String password) {
        //returneaza 1 daca username-ul si parola exista in baza de date
        //returneaza -1 daca cele doua nu exista.
        String query = "select count(id) as valid from users WHERE username= ?  and password = ?";
        try(Connection myConn = Database.getConnection();
            PreparedStatement statement = myConn.prepareStatement(query);) {
            int availableId = 0;

            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                availableId = Integer.parseInt(rs.getString("valid"));
            }

            if (availableId == 1) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // nu avem in db userul si parola dorita

    }*/

    /**
     * Aceasta functie obtine scorul unui utilizator pentru o anumita problema
     *
     * @param userId    Id-ul utilizatorului care se cauta in baza de date
     * @param problemId Id-ul problemei care se cauta in baza de date
     * @return int Numarul de puncte ale utilizatorului pentru problema
     */
    public static int getUserProblemScore(int userId, int problemId) {
        String query = "select score from points WHERE id_user = ? and id_problem = ?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);) {
            statement.setInt(1, userId);
            statement.setInt(2, problemId);
            try (ResultSet rs = statement.executeQuery();) {
                if (rs.next()) return rs.getInt("score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // nu avem punctaj pentru problema data
    }

    /**
     * Aceasta functie este folosita pentru a adauga punctajul unui utilizator la o anumita problema
     *
     * @param userId    utilizatorul care a rezolvat problema
     * @param problemId Id-ul problemei la care se va adauga punctajul
     * @param points    punctajul obtinut de utilizator
     * @return
     */
    public static void setPoints(int userId, int problemId, int points) {

        try {

            Connection myConn = Database.getConnection();
            PreparedStatement statement = myConn.prepareStatement("insert into points (id_user,id_problem,points) values (?,?,?)");
            statement.setInt(1, userId);
            statement.setInt(2, problemId);
            statement.setInt(3, points);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * Aceasta functie este folosita pentru a cauta email-ul unui user pe baza id-ului acestuia
     *
     * @param userId este id-ul user-ului pentru care se va cauta email-ul
     * @return email Acesta este email-ul user-ului.    Returneaza null in cazul in care id-ul nu exista in baza de date
     */
    public String getEmail(int userId) {
        String email = null;
        String query = "select email from users where id=? ";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) email = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  email;
    }

    /**
     * Aceasta functie este folosita pentru a cauta parola unui user pe baza id-ului acestuia
     *
     * @param username este username-ul user-ului pentru care se va cauta parola
     * @return password Aceasta este parola user-ului.    Returneaza null in cazul in care id-ul nu exista in baza de date
     */
    public String getPassword(String username) {
        String password = null;
        String query = "select password from users where username=? ";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) password = rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return password;
    }


    /**
     * Aceasta functie este folosita pentru a cauta punctajul unui user pe baza id-ului acestuia
     *
     * @param userId este id-ul user-ului pentru care se va cauta punctajul
     * @return points Acesta este punctajul user-ului.   Returneaza -1 in cazul in care id-ul nu exista in baza de date
     */
    public int getPoints(int userId) {
        int points = -1;
        String query = "select points_no from users where id=? ";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) points = rs.getInt("points_no");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    /**
     * Aceasta functie este folosita pentru a returna id-ul unui utilizator
     * @param username este username-ul user-ului pentru care se va cauta id-ul
     * @return id Acesta este id-ul user-ului.    Returneaza -1 in cazul in care username-ul nu exista in baza de date
     */
    public int getId(String username){
        int id = -1 ;
        String query="select id from users where username=?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)){
            statement.setString(1,username);
            try (ResultSet rs = statement.executeQuery()){
                if(rs.next())  id= rs.getInt("id");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Aceasta functie este folosita pentru a returna numarul de probleme incarcate de utilizator
     * @param userId este id-ul user-ului pentru care se va cauta numarul de probleme incarcate
     * @return count Acesta este numarul de probleme incarcate.    Returneaza 0 in cazul in care user-ul nu are nicio problema incarcata
     */
    public int getNrOfUploadedProblems(int userId){
        int count = 0;
        String query="select uploaded_problems_no from users where id = ?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)){
            statement.setInt(1,userId);
            try (ResultSet rs = statement.executeQuery()){
                if(rs.next())  count= rs.getInt("uploaded_problems_no");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return count;

    }

    /**
     * Aceasta functie returneaza un JSONObject care contine toate detaliile despre user
     * @param userId este id-ul utilizatorului
     * @return JSONObject ce reprezinta user-ul si contine(id, username, email, nr. de probleme rezolvate, nr. de probleme incarcate si numarul de puncte)
     */
    public JSONObject getUserPublicData(int userId){
        JSONObject user = new JSONObject();
        String query ="SELECT id,username,email,solved_problems_no,uploaded_problems_no,points_no from users where id=?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                user.put("id", resultSet.getInt("id"));
                user.put("username", resultSet.getString("username"));
                user.put("email", resultSet.getString("email"));
                user.put("solved_problems_no", resultSet.getInt("solved_problems_no"));
                user.put("uploaded_problems_no", resultSet.getInt("uploaded_problems_no"));
                user.put("points_no", resultSet.getInt("points_no"));

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Aceasta functie returneaza un JSONArray cu toti utilizatorii in ordinea punctajelor
     * @return JSONArray ce contine utilizatorii fiecare element dinarray avand id-ul, username-ul si numarul de puncte al fiecarui utilizator
     */
    public JSONArray getUsersOrderedByScore(){
        JSONArray array = new JSONArray();
        String query ="SELECT id,username,points_no from users order by points_no desc";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                JSONObject user = new JSONObject();
                user.put("id", resultSet.getInt("id"));
                user.put("username", resultSet.getString("username"));
                user.put("points_no", resultSet.getInt("points_no"));
                array.put(user);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }
    /**
     * Aceasta functie este folosita pentru a returna numarul de probleme rezolvate de un anumit utilizator
     * @param userId este id-ul user-ului pentru care se va cauta numarul de probleme rezolvate
     * @return count Acesta este numarul de probleme rezolvate.    Returneaza 0 in cazul in care user-ul nu are nicio problema rezolvata
     */
    public int getSolvedProblemsNr(int userId){
        int count = 0;
        String query="select solved_problems_no from users where id = ?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)){
            statement.setInt(1,userId);
            try (ResultSet rs = statement.executeQuery()){
                if(rs.next())  count= rs.getInt("solved_problems_no ");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return count;

    }

    /**
     * Aceasta functie este folosita pentru criptarea parolei
     * @param userName este numele de utilizator al user-ului
     * @return salt
     */
    public String getUserSalt(String userName)
    {
        String userSalt = null;
        String query="select salt from users where username = ?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)){
            statement.setString(1,userName);
            try (ResultSet rs = statement.executeQuery()){
                if(rs.next())  userSalt= rs.getString("salt");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return userSalt;

    }
    /**
     * Aceasta functie este folosita pentru a returna id ul problemelor care
     * au fost rezolvate de catre un utilizator primind puntaj maxim
     * @param userId este  id-ul utilizatorului
     * @return salt
     */
    public JSONArray getSolvedProblems(int userId){
        ArrayList<Integer> arr = new ArrayList<Integer>();
        String query="select id_problem from points where id_user = ? and points=?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)){
            statement.setInt(1,userId);
            statement.setInt(2,100);
            try (ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    arr.add(rs.getInt("id_problem"));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        JSONArray jsArray = new JSONArray(arr);
        return jsArray;
    }
}




