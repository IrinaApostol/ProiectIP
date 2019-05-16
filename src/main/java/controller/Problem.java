package controller;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import netscape.javascript.JSObject;
import storage.Database;


public class Problem {

    /**
     * Aceasta functie obtine scorul unui utilizator pentru o anumita problema
     * @param userId Id-ul utilizatorului care se cauta in baza de date
     * @param problemId Id-ul problemei care se cauta in baza de date
     * @return int Numarul de puncte ale utilizatorului pentru problema
     */
    public int getUserProblemScore(int userId, int problemId) {
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
     * Aceasta functie este folosita pentru a obtine testele unei anumite probleme cu un anumit id
     * @param problemId Id-ul problemei dupa care se cauta in baza de date
     * @return HashMap Acest obiect contine toate testele problemei sub forma de Map : [id_test, [input[value], output[value]]
     */
    public HashMap<Integer, HashMap<String, String>> getProblemTests(int problemId) {
        String query = "select id, test_in as input, test_out as output from problem_test where id_problem = ?";
        HashMap<Integer, HashMap<String, String>> tests = new HashMap<>();
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query);) {
            statement.setInt(1, problemId);
            try (ResultSet rs = statement.executeQuery();) {

                HashMap<String, String> inputOutputValues = new HashMap<>();
                if (rs.next()) {
                    inputOutputValues.put("input", rs.getString("input"));
                    inputOutputValues.put("output", rs.getString("output"));
                    tests.put(rs.getInt("id"), inputOutputValues);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tests;
    }


       public int getTestPercentage(int testId) {
        try {
            Connection myConn = Database.getConnection();
            PreparedStatement statement = myConn.prepareStatement("select percentage as percent from problem_test WHERE id = ? ");
            statement.setInt(1, testId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("percent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // nu avem procent (nu exista testul)
    }

     /**
     * Aceasta functie este folosita pentru a adauga teste la o anumita problema
     * @param problemId Id-ul problemei la care se va dauga testul
      * @param obj este obiectul de tip JSON care contine testele input, output si procentajul
     * @return
     */
    public void addTestToProblem(JSONObject obj, int problemId )
    {
        String test_in = (String) obj.get("test_in");
        String test_out = (String) obj.get("test_out");
        Double percentage = (Double)  obj.get("percentage");

        String query = "insert into problem_test (id_problem , test_in, test_out, percentage) values(?,?,?,?)";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setInt(1, problemId);
            statement.setString(2, test_in);
            statement.setString(3, test_out);
            statement.setDouble(4, percentage);

            statement.executeUpdate();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public void addProblem(JSONObject problem, JSONObject tests) {
        int idProblema = 0;
        String query = "INSERT INTO `problem`(`title`, `requirement`, `solution`, `category`, `created_at`, `difficulty`) VALUES (?,?,?,?,?,?)";

        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {

            statement.setString(1, problem.getString("tilte"));
            statement.setString(2, problem.getString("requiremets"));
            statement.setString(3, problem.getString("solution"));
            statement.setString(4, problem.getString("category"));
            statement.setString(5, "sysdate()");
            statement.setString(6, problem.getString("difficulty"));

            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        query = "SELECT `id` FROM `problem` WHERE `title` = ?, `requirement` = ?, `solution` = ?, `category` = ?, `difficulty` = ?";
        try (Connection myConn = Database.getConnection();
             PreparedStatement statement = myConn.prepareStatement(query)) {
            statement.setString(1, problem.getString("tilte"));
            statement.setString(2, problem.getString("requiremets"));
            statement.setString(3, problem.getString("solution"));
            statement.setString(4, problem.getString("category"));
            statement.setString(5, problem.getString("difficulty"));

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) idProblema = resultSet.getInt("id");
        } catch(SQLException e) {
            e.printStackTrace();
        }

        JSONArray testsArray = tests.getJSONArray("test");
        JSONObject test;

        if(idProblema > 0) // daca am gasit problema incepem sa punem testele
            for(int i = 0; i < testsArray.length(); i++){
                test = testsArray.getJSONObject(i);
                addTestToProblem(test, idProblema);
            } else System.out.println("Eroare la inserarea problemei in tabela");
    }
}